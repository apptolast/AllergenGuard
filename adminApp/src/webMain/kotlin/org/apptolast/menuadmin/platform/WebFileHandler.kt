package org.apptolast.menuadmin.platform

import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
import org.apptolast.menuadmin.domain.platform.FileHandler
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.js.ExperimentalWasmJsInterop

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
