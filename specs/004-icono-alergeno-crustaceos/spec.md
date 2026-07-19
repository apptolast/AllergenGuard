# Spec 004: Iconografía de alérgenos — crustáceos ≠ moluscos

> Rama: `feature/004-icono-alergeno-crustaceos` · Proyecto: `allergenguard` (MenuAdmin) · Estado: draft
> Corresponde a la **Tarea 4**.

## Contexto y objetivo
El icono de **crustáceos** se confunde con el de **moluscos**. Diagnóstico (engram obs #25): el admin usa la
fuente de iconos **Lucide** (`adminApp/src/commonMain/composeResources/font/lucide.ttf`, ~800 KB) y cada
alérgeno lleva su codepoint en el enum `AllergenType` (`shared/.../domain/model/Allergen.kt`). Crustáceos =
`0xE4F7` (línea 15) y moluscos = `0xE4F8` (línea 27) son **codepoints contiguos** en Lucide → dibujos casi
iguales. El glifo se propaga a 4 composables del admin vía `AllergenType.icon` (`AllergenBadge`,
`AllergenSelector`, `AllergenSummaryCard`, `RecipeCard`). El **PDF no usa iconos** (texto + "X") → no le
afecta. El **consumerApp** usa un sistema aparte (Material Icons, `AllergenIcon.kt:25` → `SetMeal`).

**Verificado:** el `lucide.ttf` incluido **ya contiene** los glifos `shrimp`, `shell` y `snail` (y también
`bean`, `egg`, `leaf`, `milk`, `nut`, `wheat`) → el remapeo es **factible sin añadir ningún asset**.

**Investigación de alternativas libres** (para la decisión de diseño):
- **Opción A — remapear en Lucide (recomendada como acción inmediata):** crustáceos → `shrimp`, moluscos →
  `shell` (o `snail`). Cero assets nuevos, riesgo mínimo, y de paso permite mejorar otros alérgenos hoy
  aproximados (gluten→`wheat`, huevos→`egg`, lácteos→`milk`, frutos secos→`nut`, soja/altramuces→`bean`,
  apio→`leaf`). Mantiene los ~800 KB de la fuente (mitigable con *subset*). Licencia Lucide: **ISC**.
- **Opción B — migrar a SVGs dedicados de alérgenos (mayor calidad, futura):** set **Erudus** (licencia
  **MIT**, `github.com/Erudus/erudus-icons`), 14 iconos hechos a propósito con crustáceo/molusco
  inequívocos, ~25 KB en SVG, y **consistencia web↔móvil** (mismos SVG en el consumerApp). Esfuerzo medio
  (14 drawables en `composeResources/drawable` + mapa alérgeno→recurso + sustituir el render por fuente).

Objetivo de esta spec: **Opción A** (arreglo factible e inmediato). La Opción B queda documentada como mejora
posterior (podría abrirse como Spec 005 si se prioriza la calidad).

## Alcance
- **Dentro:** remapear el `iconCode` de `CRUSTACEANS` (y `MOLLUSKS` si procede) a glifos claros presentes en
  `lucide.ttf`; opcionalmente mejorar los demás alérgenos aproximados; test-guardia de codepoints;
  opcionalmente alinear el consumerApp.
- **Fuera:** migración a Erudus/SVG (Opción B, futura), cambios en el PDF (no usa iconos), rediseño del
  sistema de theming de color de alérgenos.

## Conocimiento reutilizable
- engram **obs #25** (mapeo alérgeno→icono en `Allergen.kt`; 4 consumidores vía `AllergenType.icon`; el PDF
  no usa iconos; consumerApp separado). Fuente de iconos y codepoints de Lucide: `lucide.dev/icons`.

## Criterios de aceptación (Gherkin)
```gherkin
Scenario [AC-01]: crustáceos y moluscos se distinguen
  Given la UI admin mostrando iconos de alérgenos
  When  se pintan crustáceos y moluscos
  Then  crustáceos usa un glifo de gamba/langostino y moluscos uno de concha/caracola, claramente distintos

Scenario [AC-02]: el cambio se propaga a toda la UI admin
  Given los composables AllergenBadge, AllergenSelector, AllergenSummaryCard y RecipeCard
  When  se renderizan tras el cambio
  Then  todos reflejan el nuevo icono de crustáceos (leen AllergenType.iconCode)

Scenario [AC-03]: sin romper el resto de alérgenos
  Given los 14 alérgenos EU
  When  se renderizan sus iconos
  Then  ninguno muestra un glifo vacío/tofu; los codepoints usados existen en lucide.ttf

Scenario [AC-04]: codepoints únicos (guardia)
  Given el enum AllergenType
  When  se listan los 14 iconCode
  Then  no hay dos alérgenos con el mismo iconCode

Scenario [AC-05] (opcional): consistencia en el consumerApp
  Given el consumerApp móvil
  When  muestra el icono de crustáceos
  Then  usa un icono de crustáceo distinto del de moluscos (AllergenIcon.kt)
```

## Desglose de tareas (ligero)
- [ ] T1 Resolver los codepoints reales de `shrimp` y `shell`/`snail` en `lucide.ttf` (fontTools o el mapa de
  codepoints de Lucide).
- [ ] T2 Actualizar `Allergen.kt`: `CRUSTACEANS.iconCode` → `shrimp`; `MOLLUSKS.iconCode` → `shell`/`snail`
  si mejora. (Opcional: mejorar gluten/huevos/lácteos/frutos secos/soja/apio/altramuces con los glifos
  disponibles.)
- [ ] T3 Test-guardia en `commonTest`: los 14 `iconCode` son distintos (AC-04). **Testeable, rojo si hay
  colisión.**
- [ ] T4 Verificación **visual** en la UI admin (AC-01..AC-03).
- [ ] T5 (opcional) `consumerApp/.../AllergenIcon.kt`: icono de crustáceos distinto del de moluscos.

## Notas no funcionales
- **Plataforma:** web admin (Opción A). El consumerApp es opcional (T5) y usa otro sistema de iconos.
- **Testabilidad:** AC-04 (codepoints únicos) es un test puro y real en `commonTest`. La claridad visual
  (AC-01..03, AC-05) se valida **visualmente**. "El glifo existe en la fuente" no se testea fácil en
  `commonTest` (requeriría leer el .ttf) → verificación visual.
- **Bundle:** Opción A no cambia el tamaño; si se quiere reducir, *subset* de `lucide.ttf` a los ~14 glifos.

## Decisiones a confirmar antes de `/plan`
- ¿Aprobar **Opción A** (remap Lucide) ahora, dejando **Opción B (Erudus SVG)** como Spec 005 futura para
  máxima calidad y consistencia web/móvil? (Recomendado.)
- ¿Aprovechar para mejorar los otros ~7 alérgenos aproximados (T2 opcional) o tocar **solo** crustáceos?
- ¿Incluir el consumerApp (T5) en esta spec o dejarlo aparte?

## Trazabilidad
| AC | Test(s) que lo cubren | ¿Rojo antes de implementar? |
|----|-----------------------|------------------------------|
| AC-01 | Visual (UI admin) | n/a (visual) |
| AC-02 | Visual (4 composables) | n/a (visual) |
| AC-03 | Visual (14 alérgenos) | n/a (visual) |
| AC-04 | `AllergenIconCodesDistinctTest` | sí |
| AC-05 | Visual (consumerApp) | n/a (opcional) |
