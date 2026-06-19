package org.apptolast.menuadmin.platform

import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.apptolast.menuadmin.domain.platform.AllergenMenuPdf
import org.apptolast.menuadmin.domain.platform.MenuPdfExporter
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsString
import kotlin.js.Promise
import kotlin.js.js

/**
 * Renders the allergen menu PDF in the browser with jsPDF (loaded on demand from a CDN, so the build
 * stays untouched) and triggers a download. The whole [AllergenMenuPdf] is serialized to JSON and
 * handed to a single JS routine that lays out the A4-landscape document by hand: this gives full
 * control over the variable row heights (wrapped ingredient lists) and the rotated allergen headers.
 *
 * Logos are loaded through an `<img crossOrigin="anonymous">` + canvas; if the URL is not
 * CORS-accessible the canvas read throws and that logo is silently skipped (the rest of the PDF still
 * renders).
 */
@OptIn(ExperimentalWasmJsInterop::class)
class WebMenuPdfExporter : MenuPdfExporter {
    override suspend fun exportAllergenMenu(document: AllergenMenuPdf) {
        val payload = Json.encodeToString(AllergenMenuPdf.serializer(), document)
        generateAllergenPdf(payload).await()
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun generateAllergenPdf(json: String): Promise<JsString> =
    js(
        """
        (function() {
            function ensureLib() {
                return new Promise(function(resolve, reject) {
                    if (window.jspdf && window.jspdf.jsPDF) { resolve(); return; }
                    function ok() {
                        if (window.jspdf && window.jspdf.jsPDF) { resolve(); }
                        else { reject(new Error('jspdf cargó pero window.jspdf no está definido')); }
                    }
                    var existing = document.getElementById('jspdf-lib');
                    if (existing) {
                        existing.addEventListener('load', ok);
                        existing.addEventListener('error', function() { reject(new Error('No se pudo cargar jspdf.umd.min.js')); });
                        return;
                    }
                    // Vendored locally in webMain/resources -> served same-origin (no CDN/CORS/CSP).
                    var s = document.createElement('script');
                    s.id = 'jspdf-lib';
                    s.src = 'jspdf.umd.min.js';
                    s.onload = ok;
                    s.onerror = function() { reject(new Error('No se pudo cargar jspdf.umd.min.js')); };
                    document.head.appendChild(s);
                });
            }

            function loadImage(url) {
                return new Promise(function(resolve) {
                    if (!url) { resolve(null); return; }
                    var img = new Image();
                    img.crossOrigin = 'anonymous';
                    img.onload = function() {
                        try {
                            var canvas = document.createElement('canvas');
                            canvas.width = img.naturalWidth || img.width;
                            canvas.height = img.naturalHeight || img.height;
                            var ctx = canvas.getContext('2d');
                            ctx.drawImage(img, 0, 0);
                            var dataUrl = canvas.toDataURL('image/png');
                            resolve({ dataUrl: dataUrl, w: canvas.width, h: canvas.height });
                        } catch (e) {
                            resolve(null);
                        }
                    };
                    img.onerror = function() { resolve(null); };
                    img.src = url;
                });
            }

            var data = JSON.parse(json);

            return ensureLib().then(function() {
                return Promise.all([loadImage(data.restaurantLogoUrl), loadImage(data.companyLogoUrl)]);
            }).then(function(logos) {
                var restLogo = logos[0];
                var compLogo = logos[1];
                var JsPDF = window.jspdf.jsPDF;
                var doc = new JsPDF({ orientation: 'landscape', unit: 'mm', format: 'a4' });

                var pageW = doc.internal.pageSize.getWidth();
                var pageH = doc.internal.pageSize.getHeight();
                var margin = 10;
                var contentW = pageW - margin * 2;

                var cols = data.columns;
                var nCols = cols.length;
                var productW = 95;
                var allergenW = (contentW - productW) / nCols;

                var headerHeight = 38;
                var footerHeight = 26;

                var pink = [254, 226, 226];
                var red = [220, 38, 38];
                var gray = [120, 120, 120];
                var dark = [17, 24, 39];
                var line = [210, 214, 220];
                function txt(c) { doc.setTextColor(c[0], c[1], c[2]); }

                var dateStr = new Date().toLocaleDateString('es-ES');

                function drawFirstPageHeader() {
                    var top = margin;
                    if (restLogo) {
                        var rh = 18;
                        var rw = rh * (restLogo.w / restLogo.h);
                        if (rw > 70) { rw = 70; rh = rw * (restLogo.h / restLogo.w); }
                        try { doc.addImage(restLogo.dataUrl, 'PNG', margin, top, rw, rh); } catch (e) {}
                    }
                    if (compLogo) {
                        var ch = 16;
                        var cw = ch * (compLogo.w / compLogo.h);
                        if (cw > 60) { cw = 60; ch = cw * (compLogo.h / compLogo.w); }
                        try { doc.addImage(compLogo.dataUrl, 'PNG', pageW - margin - cw, top, cw, ch); } catch (e) {}
                    }
                    doc.setFont('helvetica', 'bold');
                    doc.setFontSize(13);
                    txt(dark);
                    doc.text((data.restaurantName || '').toUpperCase(), margin, top + 26);
                    doc.setFontSize(22);
                    doc.text('MENÚ DE ALÉRGENOS', margin, top + 36);
                    doc.setFont('helvetica', 'normal');
                    doc.setFontSize(9);
                    txt(gray);
                    doc.text(data.regulationText, pageW - margin, top + 30, { align: 'right' });
                    doc.setFont('helvetica', 'bold');
                    doc.text('Fecha: ' + dateStr, pageW - margin, top + 35, { align: 'right' });
                    return top + 44;
                }

                function drawColumnHeader(y) {
                    var h = headerHeight;
                    doc.setDrawColor(line[0], line[1], line[2]);
                    doc.setLineWidth(0.2);
                    doc.rect(margin, y, contentW, h);
                    doc.setFont('helvetica', 'bold');
                    doc.setFontSize(9);
                    txt(dark);
                    doc.text('PRODUCTO / PLATO', margin + 3, y + h - 4);
                    var x = margin + productW;
                    doc.line(x, y, x, y + h);
                    doc.setFontSize(7.5);
                    for (var i = 0; i < nCols; i++) {
                        var cx = x + i * allergenW;
                        if (i > 0) doc.line(cx, y, cx, y + h);
                        doc.text(cols[i], cx + allergenW / 2 + 1.5, y + h - 3, { angle: 90 });
                    }
                    return y + h;
                }

                var ROW_TOP_PAD = 2.5;
                var ROW_NAME_GAP = 1.0;
                var ROW_BOT_PAD = 2.5;

                // Measures the actual wrapped text height (jsPDF getTextDimensions) so the row grows to
                // fit name + every ingredient line; nothing gets clipped on 3+ line recipes.
                function rowLayout(row) {
                    doc.setFont('helvetica', 'bold');
                    doc.setFontSize(8.5);
                    var nameH = doc.getTextDimensions(row.name || '').h;
                    doc.setFont('helvetica', 'normal');
                    doc.setFontSize(7);
                    var lines = doc.splitTextToSize('Ing: ' + (row.ingredients || ''), productW - 6);
                    var ingH = doc.getTextDimensions(lines).h;
                    return {
                        lines: lines,
                        nameH: nameH,
                        height: Math.max(11, ROW_TOP_PAD + nameH + ROW_NAME_GAP + ingH + ROW_BOT_PAD),
                    };
                }

                function drawFooter() {
                    var y = pageH - footerHeight + 4;
                    doc.setDrawColor(line[0], line[1], line[2]);
                    doc.setLineWidth(0.2);
                    doc.line(margin, y - 2, pageW - margin, y - 2);
                    doc.setFont('helvetica', 'bold');
                    doc.setFontSize(8);
                    txt(dark);
                    doc.text('LEYENDA', margin, y + 4);
                    doc.setFillColor(pink[0], pink[1], pink[2]);
                    doc.setDrawColor(red[0], red[1], red[2]);
                    doc.rect(margin, y + 7, 4, 4, 'FD');
                    txt(red);
                    doc.setFontSize(7);
                    doc.text('X', margin + 1.1, y + 10);
                    doc.setFont('helvetica', 'normal');
                    txt(gray);
                    doc.setFontSize(8);
                    doc.text('Contiene el alergeno         -  No contiene', margin + 7, y + 10);
                    var notaX = pageW * 0.45;
                    doc.setFont('helvetica', 'bold');
                    txt(dark);
                    doc.text('Nota Informativa:', notaX, y + 4);
                    doc.setFont('helvetica', 'normal');
                    txt(gray);
                    var notaLines = doc.splitTextToSize(data.notaText, pageW - margin - notaX);
                    doc.text(notaLines, notaX, y + 8);
                }

                var firstPage = true;
                var y = 0;
                function startPage() {
                    if (!firstPage) doc.addPage();
                    y = firstPage ? drawFirstPageHeader() : margin;
                    y = drawColumnHeader(y);
                    firstPage = false;
                }

                startPage();
                var bottomLimit = pageH - footerHeight - 2;

                for (var r = 0; r < data.rows.length; r++) {
                    var row = data.rows[r];
                    var lay = rowLayout(row);
                    var rh = lay.height;
                    if (y + rh > bottomLimit) {
                        drawFooter();
                        startPage();
                    }
                    var x0 = margin + productW;
                    for (var i = 0; i < nCols; i++) {
                        if (row.present[i]) {
                            doc.setFillColor(pink[0], pink[1], pink[2]);
                            doc.rect(x0 + i * allergenW, y, allergenW, rh, 'F');
                        }
                    }
                    doc.setDrawColor(line[0], line[1], line[2]);
                    doc.setLineWidth(0.1);
                    doc.rect(margin, y, contentW, rh);
                    doc.line(x0, y, x0, y + rh);
                    for (var i = 1; i < nCols; i++) {
                        var cx = x0 + i * allergenW;
                        doc.line(cx, y, cx, y + rh);
                    }
                    doc.setFont('helvetica', 'bold');
                    doc.setFontSize(8.5);
                    txt(dark);
                    doc.text(row.name, margin + 3, y + ROW_TOP_PAD, { baseline: 'top' });
                    doc.setFont('helvetica', 'normal');
                    doc.setFontSize(7);
                    txt(gray);
                    doc.text(lay.lines, margin + 3, y + ROW_TOP_PAD + lay.nameH + ROW_NAME_GAP, { baseline: 'top' });
                    for (var i = 0; i < nCols; i++) {
                        var ccx = x0 + i * allergenW + allergenW / 2;
                        var ccy = y + rh / 2;
                        if (row.present[i]) {
                            doc.setFont('helvetica', 'bold');
                            doc.setFontSize(8);
                            txt(red);
                            doc.text('X', ccx, ccy, { align: 'center', baseline: 'middle' });
                        } else {
                            doc.setFont('helvetica', 'normal');
                            doc.setFontSize(8);
                            txt(gray);
                            doc.text('-', ccx, ccy, { align: 'center', baseline: 'middle' });
                        }
                    }
                    y += rh;
                }
                drawFooter();

                doc.save(data.fileName);
                return data.fileName;
            });
        })()
        """,
    )
