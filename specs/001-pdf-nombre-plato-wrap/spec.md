# Spec 001: Ajuste del nombre de plato en el PDF de alérgenos

> Rama: `feature/001-pdf-nombre-plato-wrap` · Proyecto: `allergenguard` (MenuAdmin) · Estado: draft
> Corresponde a la **Tarea 1** reportada por el usuario admin (menú "De Cobre y Picón").

## Contexto y objetivo
Al exportar el menú a PDF, los nombres de plato largos se dibujan **sin ajuste de línea** y se salen de su
columna, solapando la zona de iconos/columnas de alérgenos.

Diagnóstico (engram obs #25):
- El renderer **vivo** es `adminApp/src/webMain/resources/allergen-pdf.js` (jsPDF, maquetado a mano en la
  línea 20), invocado vía `window.generateAllergenPdf` desde `MenusViewModel.exportPdf()`.
- El gemelo Kotlin `adminApp/src/webMain/.../platform/WebMenuPdfExporter.kt` tiene el **mismo bug** pero es
  código muerto (inyectado por Koin y nunca llamado).
- El nombre se dibuja crudo desde x=13 **sin `splitTextToSize` ni ancho máximo**; la columna de producto
  acaba en x=105 (hardcoded) y los iconos van en X fija a partir de ahí. Los **ingredientes SÍ se envuelven**
  (`splitTextToSize` a 89mm) — al nombre simplemente le falta esa llamada. La altura de fila
  (`Math.max(11, topPad + nameH + gap + ingH + botPad)`) usa `nameH` de una sola línea.

Objetivo: que el nombre se ajuste dentro de su columna y la fila crezca para acomodarlo, sin solapar
alérgenos ni ingredientes, y sin regresión en nombres cortos.

## Alcance
- **Dentro:** envolver el nombre del plato dentro de la columna de producto en `allergen-pdf.js`;
  recalcular la altura de fila con el nombre multilínea; alinear (o marcar obsoleto) el gemelo Kotlin.
- **Fuera:** rediseño del PDF (colores/tipografías), reactivar el gemelo Kotlin como ruta real, iconos de
  alérgeno en el PDF (hoy son texto + "X"), paginación, cualquier cambio en el modelo de datos.

## Conocimiento reutilizable
- engram **obs #25** (diagnóstico root-cause del PDF) y memoria de proyecto `[[export_pdf_feature]]`
  (jsPDF vendorizado, layout manual, `getTextDimensions` + `baseline:'top'`).
- Se **reutiliza tal cual** el patrón ya presente en el mismo fichero: `splitTextToSize(texto, 89)` +
  dibujo de `lines`, que ya funciona para los ingredientes.
- Se **adapta**: aplicar ese mismo patrón al nombre y propagar la altura real al cálculo de fila.

## Criterios de aceptación (Gherkin)
```gherkin
Scenario [AC-01]: el nombre largo se ajusta dentro de su columna
  Given un plato cuyo nombre supera el ancho de la columna de producto
        (p.ej. "CROQUETAS IBERICAS DEL PUCHERO CON VELO DE PAPADA DEL ESCRIBANO")
  When  se exporta el menú a PDF
  Then  el nombre se envuelve en varias líneas dentro de la columna de producto (ancho ≈ 89mm)
  And   ninguna línea del nombre invade la zona de iconos de alérgenos (x ≥ 105mm)

Scenario [AC-02]: la fila crece para acomodar el nombre multilínea
  Given un plato con nombre de 2+ líneas y con ingredientes también de varias líneas
  When  se exporta el PDF
  Then  la altura de la fila contiene el nombre completo y los ingredientes sin solaparse entre sí
  And   sin solaparse con la fila siguiente

Scenario [AC-03]: sin regresión en nombres cortos
  Given un plato con nombre de una sola línea
  When  se exporta el PDF
  Then  la fila mantiene su altura mínima anterior (sin huecos extra ni cambios visibles)

Scenario [AC-04]: paridad del gemelo Kotlin
  Given el renderer Kotlin WebMenuPdfExporter.kt (no usado, pero mantenido en el repo)
  When  se revisa su dibujo del nombre
  Then  aplica el mismo ajuste (splitTextToSize + altura) que allergen-pdf.js
  Or    queda anotado explícitamente como obsoleto/no-usado
```

## Desglose de tareas (ligero)
- [ ] T1 `allergen-pdf.js`: envolver el nombre con `splitTextToSize` al ancho de producto (≈89mm) y dibujar
  las líneas resultantes (mismo patrón que los ingredientes).
- [ ] T2 `allergen-pdf.js` `m()` (rowLayout): usar la altura del **nombre envuelto** en el `Math.max`.
- [ ] T3 Alinear `WebMenuPdfExporter.kt` con el mismo ajuste, o marcarlo `@Deprecated`/comentado como muerto.
- [ ] T4 Verificación **visual**: regenerar el PDF de "De Cobre y Picón" y comprobar AC-01..AC-03.

## Notas no funcionales
- **Plataforma:** solo web admin (wasmJs/js). Sin i18n. Sin Firebase. Sin cambios de rendimiento.
- **Testabilidad:** el layout vive en JS y depende de las métricas de texto de jsPDF → **no hay lógica de
  dominio Kotlin pura testeable en `commonTest`**. La validación es por **inspección visual** del PDF
  generado (`/verify` + captura de pantalla). No habrá test rojo previo (spec fuera del camino TDD estricto).
- **Decisión de diseño:** se **envuelve y se crece la fila** (coherente con los ingredientes), en lugar de
  truncar con "…". Reversible si el usuario prefiere truncado.

## Trazabilidad
| AC | Test(s) que lo cubren | ¿Rojo antes de implementar? |
|----|-----------------------|------------------------------|
| AC-01 | Visual: PDF de De Cobre y Picón, plato de nombre largo | n/a (visual) |
| AC-02 | Visual: plato con nombre + ingredientes multilínea | n/a (visual) |
| AC-03 | Visual: plato de nombre corto (sin regresión) | n/a (visual) |
| AC-04 | Revisión de código de `WebMenuPdfExporter.kt` | n/a |
