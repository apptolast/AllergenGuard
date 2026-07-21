# Spec 003: Subelaboraciones (sub-recetas) de primera clase

> Rama: `feature/003-subelaboraciones-primera-clase` · Proyecto: `allergenguard` (MenuAdmin) · Estado: draft
> Corresponde a la **Tarea 3** (opción Y elegida por el usuario: modelar subelaboraciones como concepto de
> primera clase). Es la spec de mayor alcance → se estructura en **Fase A (datos/dominio)** y **Fase B (UI)**,
> pensadas como **dos PRs** bajo esta spec.

## Contexto y objetivo
Una subelaboración (salsa, mayonesa, aliño, caldo, rub, bechamel…) es una receta que se usa como componente
de otra receta. Hoy el modelo **no** tiene ese concepto y aparecen varios problemas (verificados contra
Firestore real de "De Cobre y Picón", proyecto `menusmati`, DB `(default)`):

1. **Naming roto:** el `ImportMapper` aplana las sub-recetas en ingredientes y, cuando la referencia no
   resuelve a un ingrediente del catálogo, persiste el placeholder `"Ingrediente <id>"`
   (`ImportMapper.kt:91`). **Confirmado: 8 entradas "Ingrediente N" persistidas, apuntando a 2 ids de
   receta** en De Cobre y Picón. La info real no se pierde: el nombre está en el dump y las sub-recetas ya
   existen como documentos `Recipe` (mismo id que queda en `ingredientId`), pero se ignora.
2. **Riesgo de alérgenos:** `FirestoreRecipeRepository.computeAllergens` (líneas 89-102) recalcula los
   alérgenos **solo** desde el catálogo de ingredientes. Una subelaboración referenciada por su id-de-receta
   **no aporta sus alérgenos** al recomputar (p.ej. al editar el plato) → **infra-declaración de alérgenos**,
   que es justo lo que esta app debe evitar.
3. **Modelo actual (real, persistido):** receta en `restaurants/{restaurantId}/recipes/{id}`; cada componente
   se guarda como `{ingredientId, name, qty?, unit?}` (campo `name`; se lee a `RecipeIngredient.ingredientName`
   — `FirestoreRecipeRepository.kt:104-135` y `.toFields()` 137-154). `RecipeIngredient` no tiene tipo ni
   marca de "sub-receta". Las sub-recetas y los platos finales conviven mezclados en la misma colección
   `recipes` (los platos suelen ir en MAYÚSCULAS).

Objetivo: modelar las subelaboraciones como recetas referenciables por otras recetas, con su **nombre
correcto**, **agregación recursiva de alérgenos**, UI para crearlas/mostrarlas, y **reparación** de los datos
ya importados.

## Alcance
- **Dentro (Fase A – datos/dominio):**
  - `RecipeIngredient` gana un `type` (INGREDIENT | SUB_RECIPE); persistencia del campo `type` (default
    `ingredient`, retrocompatible).
  - `Recipe` gana `isSubRecipe: Boolean` (marca la receta como subelaboración); persistencia `isSubRecipe`.
  - Extraer el cálculo de alérgenos a una función **pura** en `commonMain` que agregue recursivamente los
    alérgenos de los componentes de tipo SUB_RECIPE (con guardia de ciclos).
  - `ImportMapper`: dejar de aplanar sub-recetas; crear componentes SUB_RECIPE con el **nombre real**
    (vía `recipeLookup`), marcar la receta referida `isSubRecipe=true`, y arreglar el fallback de nombre.
  - **Reparación de datos existentes** (De Cobre y Picón): transform puro + runner que reasigna
    `type`/`name` de los componentes "Ingrediente N"/id-de-receta y recomputa alérgenos.
- **Dentro (Fase B – UI):** añadir una subelaboración a una receta desde el editor; mostrarla como línea con
  nombre y marca visual (con sus alérgenos agregados) en tarjeta/detalle/PDF; distinguir platos vs
  subelaboraciones en el listado.
- **Fuera:** cantidades/escalado de sub-recetas, versión/histórico de recetas, cambios en el consumerApp más
  allá de que lea el modelo corregido, editor de menús.

## Conocimiento reutilizable
- engram **obs #25** (root-cause: `ImportMapper.kt:91`, `computeAllergens` solo-catálogo, esquema real
  `{ingredientId, name}`), **obs #24** (gates wasmJs), memoria `[[import_legacy_format]]`,
  `[[multitenant_refactor]]`, `[[active_menu]]`.
- Test existente `adminApp/src/commonTest/.../data/mapper/ImportMapperTest.kt` (cubre solo el caso
  `type:"recipe"` y verifica ids/alérgenos, **nunca el nombre mostrado ni el caso sin `type:"recipe"`**) → se
  **amplía** en `/test`.
- kmp-recipes: `testing/kotlin-test-turbine-fakes`.

## Criterios de aceptación (Gherkin)
```gherkin
# --- Fase A: datos / dominio (Kotlin puro, testeable) ---
Scenario [AC-01]: el import conserva el nombre de la subelaboración
  Given un dump donde un plato referencia una sub-receta con type:"recipe" (p.ej. "Salsa mayonesa casera")
  When  se ejecuta ImportMapper.mapAll
  Then  el plato tiene un componente de tipo SUB_RECIPE con el nombre real de la sub-receta
  And   no aparece "Ingrediente <id>" ni se aplana en sus ingredientes hoja

Scenario [AC-02]: el import resuelve referencias sin type:"recipe"
  Given un plato que referencia una sub-receta mediante un id "pelado" (sin type) que coincide con el id de
        una receta del dump
  When  se ejecuta ImportMapper.mapAll
  Then  ese componente es de tipo SUB_RECIPE con el nombre real de la receta (nunca "Ingrediente <id>")

Scenario [AC-03]: el fallback de nombre consulta también las recetas
  Given una referencia cuyo id no está en el catálogo de ingredientes pero sí es una receta
  When  se mapea el componente
  Then  su nombre es el de la receta (jamás el placeholder "Ingrediente <id>")

Scenario [AC-04]: agregación recursiva de alérgenos
  Given un plato con un componente SUB_RECIPE cuya sub-receta contiene el alérgeno "MOLLUSCS"
  When  se calculan los alérgenos del plato
  Then  el conjunto resultante incluye "MOLLUSCS" (deduplicado, con el nivel de contención más fuerte)
  And   agrega recursivamente los alérgenos de sub-recetas anidadas

Scenario [AC-05]: guardia de ciclos
  Given recetas que se referencian mutuamente en ciclo
  When  se agregan sus alérgenos
  Then  el cálculo termina (cada receta se visita una sola vez) sin desbordar la pila

Scenario [AC-06]: reparación de datos ya importados
  Given una receta persistida con un componente cuyo name es "Ingrediente <id>" y <id> es una receta hermana
  When  se ejecuta la reparación sobre el restaurante
  Then  ese componente pasa a type=recipe y name=<nombre de la receta referida>
  And   los computedAllergens del plato se recomputan con la agregación de la sub-receta

Scenario [AC-07]: marcado de subelaboración
  Given una receta que es referenciada como componente SUB_RECIPE por otra receta
  When  se importa o se repara
  Then  esa receta queda marcada isSubRecipe=true

# --- Fase B: UI (visual / design-check) ---
Scenario [AC-08]: añadir una subelaboración a una receta
  Given el editor de una receta del restaurante R
  When  el usuario añade otra receta de R como subelaboración
  Then  se agrega como componente de tipo SUB_RECIPE, visualmente distinguible de un ingrediente

Scenario [AC-09]: mostrar la subelaboración con su nombre y alérgenos
  Given un plato con una subelaboración
  When  se ve su tarjeta/detalle y se exporta el PDF
  Then  la subelaboración aparece con su nombre real y una marca visual
  And   sus alérgenos agregados se reflejan en el plato (nunca "Ingrediente N")

Scenario [AC-10]: distinguir platos de subelaboraciones en el listado
  Given la lista de recetas del restaurante
  When  el usuario la consulta
  Then  puede distinguir/filtrar los platos finales de las subelaboraciones (isSubRecipe)
```

## Desglose de tareas (ligero)
- [ ] T1 (A) Modelo: `RecipeIngredient.type: RecipeComponentType = INGREDIENT`; `Recipe.isSubRecipe = false`.
- [ ] T2 (A) Persistencia: `FirestoreRecipeRepository` lee/escribe `type` e `isSubRecipe` (retrocompatible).
- [ ] T3 (A) Extraer `RecipeAllergenAggregator` puro a `commonMain` (resuelve SUB_RECIPE contra un mapa de
  recetas, recursivo + cycle-guard); `computeAllergens` lo usa. **Testeable con fakes.**
- [ ] T4 (A) `ImportMapper`: sub-recetas → componentes SUB_RECIPE con nombre real; marcar `isSubRecipe`; fix
  del fallback de `ImportMapper.kt:91`. Ampliar `ImportMapperTest`.
- [ ] T5 (A) Reparación: `RecipeDataRepair` puro (transform de componentes "Ingrediente N"/id-receta) +
  runner one-shot por restaurante (acción admin o script), con **backup previo**.
- [ ] T6 (B) Editor de receta: añadir subelaboración (picker de recetas del restaurante, marcado distinto).
- [ ] T7 (B) Render: tarjeta/detalle/PDF muestran la subelaboración con nombre + marca + alérgenos agregados.
- [ ] T8 (B) Listado: distinguir/filtrar platos vs subelaboraciones.

## Notas no funcionales
- **Plataforma:** web admin (wasmJs) para crear/mostrar; el **consumerApp** (móvil) se beneficia al leer el
  modelo corregido (verificar que su lectura de recetas tolera el nuevo `type`). i18n: nuevas cadenas.
- **Firebase:** campos nuevos `type` (en componentes) e `isSubRecipe` (en receta), **aditivos y
  retrocompatibles**. La **reparación** modifica documentos existentes → exigir **backup/export previo** y
  ejecutar primero en DB `debug`, luego `(default)`. Sin cambio de reglas.
- **Contrato con el consumerApp (Spec 005):** el consumer móvil LEE el campo persistido `computedAllergens`
  (forma `{code, level}`) y **no recalcula** alérgenos, y parsea Firestore de forma schemaless. Diseño correcto
  (y lo que hace que el consumer se autocorrija): (1) seguir escribiendo `computedAllergens` con esa forma y la
  clave `code` (no renombrar); (2) **recomputar y persistir** `computedAllergens` en CADA receta tocada por la
  reparación; (3) marcar `isSubRecipe=true` en las subelaboraciones. **Urgencia:** la app móvil **aún NO está
  en producción (sin usuarios)** → esto ya no es crítico de seguridad *ahora* ni obliga a secuenciar con
  ningún release móvil; sigue siendo el contrato correcto y debe estar cumplido **antes de lanzar el móvil a
  producción**. Ver **Spec 005**.
- **Seguridad alimentaria:** AC-04 es crítico — la agregación NO debe infra-declarar alérgenos; ante duda,
  sobre-declarar (nivel más fuerte).
- **Testabilidad:** Fase A es **Kotlin puro en `:shared`/`commonMain`** (mapper, agregador, reparación) →
  camino TDD ideal (tests rojos en `commonTest`). Fase B es UI → `/design-check` + visual. **Requisito de
  diseño:** mover la agregación de alérgenos fuera del repositorio Firestore a una función pura para poder
  testearla sin backend (hoy vive en `FirestoreRecipeRepository`).

## Decisiones a confirmar antes de `/plan`
- **Forma del modelo:** `type` en el componente (recomendado, calca el `type:"recipe"` del dump legacy y la
  lista mixta actual) vs. lista separada `subRecipeIds` en `Recipe`.
- **Presentación de la subelaboración:** como **una línea con nombre** y alérgenos agregados (recomendado) vs.
  expandible para ver sus ingredientes hoja.
- **Reparación:** ¿acción in-app ("Reparar datos") disparada por el usuario, o script one-shot que ejecuto yo
  contra Firestore (debug→release) con backup? (Se recomienda script controlado, por ser puntual y sensible.)

## Trazabilidad
| AC | Test(s) que lo cubren | ¿Rojo antes de implementar? |
|----|-----------------------|------------------------------|
| AC-01 | `ImportMapperSubRecipeNameTest` | sí |
| AC-02 | `ImportMapperBareIdRefTest` | sí |
| AC-03 | `ImportMapperNameFallbackTest` | sí |
| AC-04 | `RecipeAllergenAggregatorTest` | sí |
| AC-05 | `RecipeAllergenAggregatorCycleTest` | sí |
| AC-06 | `RecipeDataRepairTest` | sí |
| AC-07 | `ImportMapper`/`RecipeDataRepair` marcado isSubRecipe (tests T4/T5) | sí |
| AC-08 | Visual + `design-check` | n/a (UI) |
| AC-09 | Visual (tarjeta/detalle/PDF) | n/a (UI) |
| AC-10 | Visual + lógica de filtrado testeable | parcial |
