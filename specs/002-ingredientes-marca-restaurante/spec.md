# Spec 002: Ingredientes — desambiguación por marca y relación opcional con restaurante

> Rama: `feature/002-ingredientes-marca-restaurante` · Proyecto: `allergenguard` (MenuAdmin) · Estado: draft
> Corresponde a la **Tarea 2**. Se estructura en **Fase A** (marca) y **Fase B** (relación restaurante).

## Contexto y objetivo
El catálogo de ingredientes es global por cuenta (`accounts/{accountId}/ingredients`); el modelo `Ingredient`
tiene `brand` pero **no** tiene ninguna relación con restaurante (`shared/.../domain/model/Ingredient.kt`).

Problemas (engram obs #25):
1. **Desambiguación por marca:** al crear una receta, el selector de ingredientes
   (`RecipesScreen.kt:577-640`) solo muestra `name` y filtra por `name` (línea 607, `DropdownMenuItem`
   línea 619). Dos "mayonesa" (casera vs marca X, con alérgenos distintos) aparecen **idénticas** y no se
   puede saber cuál se elige. El dato `brand` ya existe y se persiste, pero no se muestra ni filtra en el
   picker. La integridad del dato es correcta (`ingredientId` es único); es un problema de **presentación**.
2. **Relación con restaurante:** el usuario admin indica que algunos ingredientes pueden ser **específicos
   de un restaurante** (relación **opcional, no obligatoria**). Hoy no es posible expresarlo.

**Delineación con la Spec 003:** las "subrecetas de un restaurante" que el usuario mencionó junto a esto se
resuelven en la **Spec 003** (subelaboraciones de primera clase). Esta Spec 002-Fase B cubre solo
ingredientes de **catálogo** específicos de un restaurante (p.ej. un producto de la casa), **no** subrecetas.

Objetivo: (A) permitir distinguir ingredientes por marca en la lista y en el picker de receta; (B) permitir
etiquetar opcionalmente un ingrediente como propio de uno o varios restaurantes y filtrar por ello.

## Alcance
- **Dentro (Fase A):** mostrar `marca` y chips de alérgeno en el picker de receta; filtrar el picker por
  `name` OR `brand`; añadir un filtro estructurado de marca en la lista de ingredientes.
- **Dentro (Fase B):** campo opcional `restaurantIds` en `Ingredient`; UI para asignarlo; filtro por
  restaurante en la lista; el picker de receta prioriza globales + específicos del restaurante actual.
- **Fuera:** subrecetas/subelaboraciones (Spec 003), mover el catálogo fuera de la cuenta, hacer la relación
  con restaurante obligatoria, paginación del catálogo.

## Conocimiento reutilizable
- engram **obs #24/#25**; memoria `[[multitenant_refactor]]` (ingredientes = subcolección de cuenta).
- `IngredientsViewModel` (líneas 30-52) ya combina `searchQuery` (name|brand) + `filterAllergens` → se
  **reutiliza** ese patrón para añadir el filtro de marca (y de restaurante en Fase B).
- kmp-recipes: `testing/kotlin-test-turbine-fakes` (para testear la lógica de filtrado con un fake repo).

## Criterios de aceptación (Gherkin)
```gherkin
# --- Fase A: marca ---
Scenario [AC-01]: el picker de receta muestra la marca
  Given dos ingredientes con el mismo nombre "Mayonesa" y marcas distintas ("Casera" y "Hellmann's")
  When  el usuario abre el selector de ingredientes al editar una receta
  Then  cada opción muestra el nombre junto a su marca (p.ej. "Mayonesa — Casera") y son distinguibles

Scenario [AC-02]: el picker filtra por nombre o marca
  Given el selector de ingredientes abierto
  When  el usuario escribe el texto de una marca
  Then  la lista de candidatos se filtra por coincidencia en nombre O en marca

Scenario [AC-03]: la lista de ingredientes filtra por marca
  Given la pantalla de Ingredientes con ingredientes de varias marcas
  When  el usuario selecciona un filtro de marca
  Then  la lista muestra solo los ingredientes de esa marca (combinable con el filtro de alérgenos existente)

# --- Fase B: relación opcional con restaurante ---
Scenario [AC-04]: etiquetar un ingrediente como propio de un restaurante (opcional)
  Given el editor de un ingrediente
  When  el usuario asigna 0..N restaurantes al ingrediente y guarda
  Then  el ingrediente persiste esos restaurantes; una lista vacía significa "disponible para todos"

Scenario [AC-05]: filtrar la lista de ingredientes por restaurante
  Given ingredientes globales y otros específicos de "De Cobre y Picón"
  When  el usuario filtra por el restaurante "De Cobre y Picón"
  Then  la lista muestra los globales MÁS los específicos de ese restaurante, y oculta los específicos de otros

Scenario [AC-06]: el picker prioriza los ingredientes relevantes al restaurante
  Given una receta del restaurante R en edición
  When  el usuario abre el selector de ingredientes
  Then  ofrece los ingredientes globales y los específicos de R
  And   los específicos de OTRO restaurante no aparecen (o quedan claramente separados al final)
```

## Desglose de tareas (ligero)
- [ ] T1 (A) Extraer a `commonMain` una función pura `filterIngredientsForPicker(query, ingredients)` que
  case por `name` OR `brand`; usarla en el picker. **Testeable en `commonTest`.**
- [ ] T2 (A) `RecipesScreen` picker: `DropdownMenuItem` muestra `name` + `brand` (+ chips de alérgeno).
- [ ] T3 (A) `IngredientsUiState`/`ViewModel`: añadir `filterBrand` (o set de marcas) + handler; UI de filtro
  en `IngredientsScreen`. **Lógica de filtrado testeable.**
- [ ] T4 (B) `Ingredient`: añadir `restaurantIds: List<String> = emptyList()`; mapper Firestore read/write
  (`FirestoreIngredientRepository.toFields`/parse).
- [ ] T5 (B) Editor de ingrediente: multi-selector opcional de restaurantes.
- [ ] T6 (B) Filtro por restaurante en la lista + priorización en el picker (funciones puras testeables).

## Notas no funcionales
- **Plataforma:** web admin (wasmJs). i18n: nuevas cadenas (etiquetas de filtro/campo) en `values/` y
  `values-es/` (defecto ES). **Firebase:** Fase B añade el campo `restaurantIds` (aditivo, retrocompatible:
  ausente = lista vacía = global). Sin cambio de reglas (mismo path de cuenta).
- **Testabilidad:** la lógica de filtrado (picker y lista) se extrae a funciones puras en `commonMain` →
  tests rojos en `commonTest` con fakes. El cableado de UI se valida con `/design-check` + visual.

## Decisiones a confirmar antes de `/plan`
- **Cardinalidad de la relación restaurante:** se propone `restaurantIds: List<String>` (0..N; vacío =
  global) por flexibilidad y retrocompatibilidad. ¿Un ingrediente puede pertenecer a **varios** restaurantes
  (recomendado) o solo a uno?
- ¿Fase B se implementa junto a la A (una sola PR) o como PR posterior? (Se recomienda A primero.)

## Trazabilidad
| AC | Test(s) que lo cubren | ¿Rojo antes de implementar? |
|----|-----------------------|------------------------------|
| AC-01 | Visual + `PickerLabelTest` (formato "name — brand") | parcial (label sí, UI visual) |
| AC-02 | `FilterIngredientsForPickerTest` | sí |
| AC-03 | `IngredientsViewModelBrandFilterTest` | sí |
| AC-04 | `IngredientRestaurantMappingTest` (round-trip) + visual | sí (mapper) |
| AC-05 | `IngredientsViewModelRestaurantFilterTest` | sí |
| AC-06 | `PickerRestaurantScopeTest` | sí |
