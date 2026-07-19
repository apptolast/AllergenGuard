# Spec 005: consumerApp (Android + iOS) — impacto y actualización por las specs 001-004

> Rama: `feature/005-consumerapp-impacto` · Proyecto: `allergenguard` (MenuAdmin, módulo `:consumerApp`) · Estado: draft
> Corresponde a la **Tarea 5** solicitada: evaluar cómo impactan los cambios 001-004 en el consumer móvil y
> actualizar su código. **Depende de la Spec 003** (contrato de datos).

## Contexto y objetivo
El consumerApp (`com.apptolast.menufrontend`, Android+iOS) lee del **mismo** Firestore (proyecto `menusmati`).
Se analizó su capa de datos y UI para medir el impacto de las specs 001-004. **Hallazgo central: la
arquitectura del consumer lo protege casi por completo** — nada es bloqueante ni crítico de seguridad, porque:

- **Alérgenos:** el consumer **LEE** el campo persistido `computedAllergens` (`{code, level}`) y **NO
  recalcula** (`consumerApp/.../data/firebase/ConsumerMappers.kt:66-68`). → Cuando la Spec 003 corrija la
  agregación (incl. subelaboraciones) y reescriba `computedAllergens`, el consumer refleja el dato correcto
  **solo**, sin release móvil. Además los alérgenos tras el fix solo pueden **aumentar**, nunca desaparecer →
  clientes móviles antiguos quedan igual de seguros o más.
- **Parseo schemaless:** el consumer decodifica Firestore a `Map<String,Any?>` (`FirestoreCodec`); los campos
  nuevos `type`/`isSubRecipe` se **ignoran sin romper** (también en apps ya publicadas en la store).
- **Modelos propios:** el consumer tiene `Dish` y su propio enum `Allergen` (no usa `:shared`); solo reutiliza
  infraestructura (FirestoreClient, Auth, Config). Los ingredientes son `List<String>` de nombres.
- **No lee el catálogo de ingredientes** (`accounts/{id}/ingredients`) → `restaurantIds` de la Spec 002 no le
  afecta. **No tiene PDF** → la Spec 001 no le afecta.

Lo que **sí** requiere el consumer (todo no-bloqueante y de bajo/medio esfuerzo):
1. Su iconografía de alérgenos tiene un problema **distinto** al del admin: CRUSTÁCEOS colisiona con **PESCADO**
   (ambos `Icons.Outlined.SetMeal`, `AllergenIcon.kt:18,25`), no con moluscos.
2. El menú del consumer solo filtra por pertenencia a `recipeIds`; **no** filtra `isSubRecipe` → una
   subelaboración metida por error en un menú publicado aparecería como plato (defensa en profundidad).
3. El mapper descarta en silencio componentes sin `name` y **códigos de alérgeno desconocidos**
   (`ConsumerMappers.kt:63-68`, `allergenFromCode` → `null` → `mapNotNull`) → un código no reconocido puede
   hacer que un plato parezca "seguro". Bug latente de seguridad, independiente de las 4 specs.

Objetivo: aplicar esos ajustes en el consumer y verificar la no-regresión sobre datos reales tras la Spec 003.

## Alcance
- **Dentro:** icono de crustáceos del consumer (resolver colisión con pescado); filtro defensivo de
  subelaboraciones (e inactivas) en el menú; robustez del mapper ante `name`/códigos ausentes;
  (opcional) distinguir visualmente subelaboraciones en el detalle; verificación de regresión.
- **Fuera:** recalcular alérgenos en el consumer (no lo hace ni debe); migrar el consumer a los modelos de
  `:shared`; mostrar niveles `contains`/`traces` (preexistente, `ConsumerMappers.kt` aplana el `level`);
  cambios de la Spec 001 (PDF, inexistente en consumer) y 002 (catálogo, no lo lee).

## Conocimiento reutilizable
- engram (análisis de impacto consumer, esta sesión): consumer lee `computedAllergens`, modelos propios,
  parseo schemaless, icono `AllergenIcon.kt`, filtro de menú por `recipeIds`.
- Memorias `[[consumer_auth_social]]` (el consumer usa REST en `:shared`, no GitLive),
  `[[active_menu]]` (menú publicado = `published==true`), `[[sdd_harness_setup]]`.
- **Contrato de la Spec 003** (produce `computedAllergens` correcto + `isSubRecipe`).

## Criterios de aceptación (Gherkin)
```gherkin
Scenario [AC-01]: tolerancia a los campos nuevos (regresión)
  Given una receta en Firestore con los campos nuevos `type` (en componentes) e `isSubRecipe`
  When  el consumer la lee y muestra el plato
  Then  los ignora sin error y el plato se muestra con normalidad

Scenario [AC-02]: los alérgenos corregidos se reflejan sin release (verificación)
  Given que la Spec 003 recomputó y persistió `computedAllergens` incluyendo los de las subelaboraciones
  When  el consumer muestra el plato y aplica el filtro por perfil de alérgenos del usuario
  Then  el resaltado/ocultado usa el conjunto de alérgenos corregido (incluye los de las subelaboraciones)

Scenario [AC-03]: nombres de subelaboración correctos (verificación)
  Given que la Spec 003 reparó los `name` de los componentes ("Ingrediente N" → nombre real)
  When  el consumer muestra la lista de ingredientes del detalle del plato
  Then  muestra el nombre real de la subelaboración y nunca "Ingrediente N"

Scenario [AC-04]: las subelaboraciones no fugan al menú (defensa en profundidad)
  Given una receta con `isSubRecipe == true`
  When  el consumer construye la lista de platos del menú publicado
  Then  esa receta no aparece como plato aunque su id estuviera en `recipeIds`

Scenario [AC-05]: el icono de crustáceos es inequívoco
  Given el consumer mostrando iconos de alérgenos
  When  se pintan crustáceos, pescado y moluscos
  Then  crustáceos usa un icono distinto del de pescado y del de moluscos

Scenario [AC-06]: parseo robusto (seguridad)
  Given un componente sin `name`, o un `computedAllergens` con un código de alérgeno no reconocido
  When  el consumer parsea el documento
  Then  no descarta el ingrediente/plato de forma peligrosa y silenciosa
  And   un código de alérgeno desconocido NUNCA hace que un plato se presente como "seguro"
        (se conserva/registra en lugar de desaparecer)

Scenario [AC-07] (opcional): distinguir la subelaboración en el detalle
  Given un plato con una subelaboración
  When  el usuario ve el detalle del plato
  Then  la subelaboración se muestra diferenciada de un ingrediente normal
```

## Desglose de tareas (ligero)
- [ ] T1 `features/components/AllergenIcon.kt`: asignar a `CRUSTACEANS` un icono distinto de `FISH`
  (y de `MOLLUSKS`). Propaga a los 4 consumidores (chip, grid, DishCard, DishDetail).
- [ ] T2 `data/firebase/FirestoreRestaurantRepository.kt` (~línea 41): filtrar del menú las recetas con
  `isSubRecipe == true` (y opcionalmente `active == false`). Función pura → testeable.
- [ ] T3 `data/firebase/ConsumerMappers.kt:63-68`: no descartar en silencio componentes sin `name`
  (fallback razonable); manejar códigos de alérgeno desconocidos sin perderlos silenciosamente (log/fallback
  seguro). **Testeable.**
- [ ] T4 (opcional) `domain/model/Dish.kt` + `ConsumerMappers.kt` + `DishDetailScreen.kt:219-238`: enriquecer
  el componente con su `type` y diferenciar visualmente la subelaboración en el detalle.
- [ ] T5 Verificación de regresión sobre "De Cobre y Picón" **después** de ejecutar la reparación de la
  Spec 003 (AC-02, AC-03): alérgenos correctos, nombres correctos, sin fugas.

## Notas no funcionales
- **Plataformas:** Android + iOS (módulo `:consumerApp`). **La app móvil aún NO está en producción (sin
  usuarios)** → no hay clientes antiguos que proteger ni ventana de rollout; los cambios de datos de la
  Spec 003 pueden aplicarse con total libertad y esta spec puede hacerse en **un solo paso al final**, sin
  secuenciación defensiva. (Los cambios de la 003 siguen siendo aditivos por el bien del **admin web**, que sí
  opera sobre la BBDD real.) El requisito real es que todo esté verificado **antes de lanzar el móvil a
  producción**.
- **Gates SDD distintos al resto:** este módulo NO es wasmJs. Test host = `./gradlew
  :consumerApp:testDebugUnitTest`. **El `:consumerApp` no tiene tarea ktlint** (ver `[[pending_share_deeplinks]]`),
  así que el gate de `/validate` para esta spec = **tests** (+ compilación Android/iOS), sin ktlint.
- **Testabilidad:** la lógica de filtrado (T2) y del mapper (T3) es Kotlin en `commonMain` del consumer →
  tests rojos en `commonTest`/host. El icono (T1) y la UX (T4) se validan **visualmente** en el dispositivo.
- **Seguridad:** el filtrado/resaltado por perfil del usuario depende 100% de `computedAllergens`; AC-06 cierra
  el fallo silencioso por código desconocido (bug preexistente que conviene resolver aquí).

## Secuenciación (cross-spec)
- **Depende de la Spec 003** (contrato: `computedAllergens` correcto + `isSubRecipe`). AC-02/AC-03 se verifican
  tras la reparación de la 003.
- **Timing recomendado:** al **final**, en un solo paso tras la Spec 003. Como el móvil **no está en
  producción**, no hay secuenciación defensiva ni precauciones de rollout; la reparación de la 003 puede
  correr cuando esté lista. El único requisito es que todo (incl. el hardening AC-06 y la agregación correcta
  de alérgenos) esté hecho y verificado **antes del lanzamiento del móvil a producción**.
- **La Spec 004 queda admin-only:** su AC-05 opcional (consumer) se **traslada aquí** (T1), porque el problema
  del consumer es otro (CRUSTÁCEOS≡PESCADO) y usa otro sistema de iconos (Material Icons, no Lucide).

## Decisiones a confirmar antes de `/plan`
- ¿Incluir la **UX de distinguir subelaboraciones** en el detalle (T4/AC-07), o dejar la lista plana (el
  nombre ya sale bien tras la 003)?
- ¿Arreglar **solo** crustáceos, o de paso las otras colisiones de iconos del consumer (FilterVintage ×3,
  Grass ×2, LocalFlorist ×2)?
- ¿Incluir el **hardening de seguridad** del mapper (AC-06)? (Recomendado — es un fallo silencioso real.)

## Trazabilidad
| AC | Test(s) que lo cubren | ¿Rojo antes de implementar? |
|----|-----------------------|------------------------------|
| AC-01 | `ConsumerRecipeParseIgnoresNewFieldsTest` | sí |
| AC-02 | Verificación en dispositivo sobre datos reales (post-003) | n/a (verificación) |
| AC-03 | Verificación en dispositivo (post-003) | n/a (verificación) |
| AC-04 | `MenuExcludesSubRecipesTest` | sí |
| AC-05 | Visual (chip/grid/DishCard/DishDetail) | n/a (visual) |
| AC-06 | `ConsumerAllergenMapperRobustnessTest` | sí |
| AC-07 | Visual (detalle del plato) | n/a (opcional) |
