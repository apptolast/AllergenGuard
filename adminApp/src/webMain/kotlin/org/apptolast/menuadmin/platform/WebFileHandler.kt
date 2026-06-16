package org.apptolast.menuadmin.platform

import kotlinx.browser.document
import kotlinx.coroutines.await
import kotlinx.coroutines.suspendCancellableCoroutine
import org.apptolast.menuadmin.domain.platform.FileHandler
import org.apptolast.menuadmin.domain.platform.PickedImage
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsString
import kotlin.js.Promise
import kotlin.js.js

@OptIn(ExperimentalWasmJsInterop::class)
class WebFileHandler : FileHandler {
    override suspend fun pickAndReadFile(): String? =
        suspendCancellableCoroutine { cont ->
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = ".json"

            input.onchange = {
                val file = input.files?.item(0)
                if (file != null) {
                    val reader = FileReader()
                    reader.onload = {
                        val result = reader.result?.toString()
                        cont.resume(result)
                        Unit
                    }
                    reader.onerror = {
                        cont.resume(null)
                        Unit
                    }
                    reader.readAsText(file)
                } else {
                    cont.resume(null)
                }
                Unit
            }

            input.click()
        }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun pickAndCompressImage(
        maxDimension: Int,
        preferredMimeType: String,
        quality: Double,
    ): PickedImage? {
        val file = pickImageFile() ?: return null
        // canvas.toDataURL gives "data:<mime>;base64,<data>"; the mime reflects what the browser
        // actually encoded (WebP where supported, otherwise its fallback), which we keep so the
        // uploaded content type always matches the bytes.
        val dataUrl = compressImageToDataUrl(file, maxDimension, preferredMimeType, quality)
            .await()
            .toString()
        val comma = dataUrl.indexOf(',')
        if (!dataUrl.startsWith("data:") || comma < 0) return null
        val mime = dataUrl.substring("data:".length, comma).substringBefore(';')
        val bytes = Base64.decode(dataUrl.substring(comma + 1))
        return PickedImage(bytes = bytes, contentType = mime)
    }

    private suspend fun pickImageFile(): File? =
        suspendCancellableCoroutine { cont ->
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = "image/*"
            input.onchange = {
                cont.resume(input.files?.item(0))
                Unit
            }
            input.click()
        }

    override suspend fun saveFile(
        content: String,
        fileName: String,
    ) {
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = buildDataUri(content)
        anchor.download = fileName
        anchor.click()
    }

    private fun buildDataUri(content: String): String {
        val encoded = buildString {
            for (char in content) {
                when (char) {
                    ' ' -> append("%20")
                    '#' -> append("%23")
                    '%' -> append("%25")
                    '&' -> append("%26")
                    '+' -> append("%2B")
                    '\n' -> append("%0A")
                    '\r' -> append("%0D")
                    '\t' -> append("%09")
                    else -> append(char)
                }
            }
        }
        return "data:application/json;charset=utf-8,$encoded"
    }
}

/**
 * Decodes [file] into an `<img>`, draws it onto a canvas scaled so its longest side is at most
 * [maxDimension], and re-encodes it via `canvas.toDataURL([mimeType], [quality])`. Returns a data URL.
 */
@OptIn(ExperimentalWasmJsInterop::class)
private fun compressImageToDataUrl(
    file: JsAny,
    maxDimension: Int,
    mimeType: String,
    quality: Double,
): Promise<JsString> =
    js(
        """
        new Promise(function(resolve, reject) {
            var url = URL.createObjectURL(file);
            var img = new Image();
            img.onload = function() {
                var w = img.naturalWidth || img.width;
                var h = img.naturalHeight || img.height;
                var scale = Math.min(1, maxDimension / Math.max(w, h));
                var cw = Math.max(1, Math.round(w * scale));
                var ch = Math.max(1, Math.round(h * scale));
                var canvas = document.createElement('canvas');
                canvas.width = cw;
                canvas.height = ch;
                var ctx = canvas.getContext('2d');
                ctx.drawImage(img, 0, 0, cw, ch);
                var dataUrl = canvas.toDataURL(mimeType, quality);
                URL.revokeObjectURL(url);
                resolve(dataUrl);
            };
            img.onerror = function() {
                URL.revokeObjectURL(url);
                reject(new Error('image load failed'));
            };
            img.src = url;
        })
        """,
    )
