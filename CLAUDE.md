# MenuAdmin (AllergenGuard) - Project Instructions

## Backend API Reference

- **GitHub Repo**: `apptolast/menus-backend` (access via `gh api repos/apptolast/menus-backend/...`)
- **Swagger UI**: https://menus-api-dev.apptolast.com/swagger-ui/index.html#/
- **API Docs (Redocly)**: https://apptolast.github.io/menus-backend/
- **OpenAPI Spec (JSON)**: https://menus-api-dev.apptolast.com/v3/api-docs
- **Base URL (dev)**: https://menus-api-dev.apptolast.com

### API Endpoint Summary

All admin endpoints require Bearer JWT. Prefix: `/api/v1`

| Module             | Endpoints                                                                                                                                                                                      | Notes                                                                           |
|--------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------|
| Auth               | `POST /auth/register`, `POST /auth/register-admin`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/oauth2/google/callback`                                                              | JWT-based, register-admin whitelist                                             |
| Admin Restaurants  | `GET/POST /admin/restaurants`, `GET/PUT/DELETE /admin/restaurants/{id}`                                                                                                                        | Multi-tenant, paginated GET                                                     |
| Admin Whitelist    | `GET/POST /admin/whitelist`, `DELETE /admin/whitelist/{email}`                                                                                                                                 | Admin registration whitelist                                                    |
| Admin Ingredients  | `GET/POST /admin/ingredients`, `GET/PUT/DELETE /admin/ingredients/{id}`, `GET /admin/ingredients/search`, `GET/PUT /admin/ingredients/{id}/allergens`                                          | Global catalog                                                                  |
| Admin Recipes      | `GET/POST /admin/restaurants/{restaurantId}/recipes`, `GET/PUT/DELETE /admin/recipes/{id}`, `GET /admin/recipes/{id}/allergens`                                                                | List/create scoped to restaurant                                                |
| Admin Menus        | `GET/POST /admin/restaurants/{restaurantId}/menus`, `PUT/DELETE /admin/menus/{id}`, `PUT /admin/menus/{id}/publish`                                                                            | List/create scoped to restaurant                                                |
| Menu Sections      | `POST /admin/menus/{menuId}/sections`, `PUT/DELETE /admin/menus/{menuId}/sections/{sectionId}`                                                                                                 | Nested under menu                                                               |
| Admin Dishes       | `GET /admin/restaurants/{restaurantId}/dishes`, `POST /admin/dishes`, `PUT/DELETE /admin/dishes/{id}`, `POST /admin/dishes/{id}/allergens`, `DELETE /admin/dishes/{id}/allergens/{allergenId}` | GET scoped to restaurant, POST flat. Price is read-only (inherited from recipe) |
| Menu Digital Cards | `POST /admin/menu-digital-cards`, `GET /admin/menu-digital-cards/{menuId}`, `PUT/DELETE /admin/menu-digital-cards/{id}`                                                                        | Pivot table linking menus to dishes for digital card composition                |
| Allergens          | `GET /allergens`, `GET /allergens/{code}`                                                                                                                                                      | Public, 14 EU allergens                                                         |
| Public             | `GET /restaurants`, `GET /restaurants/{id}`, `GET /restaurants/{restaurantId}/menu`, `GET /restaurants/{restaurantId}/sections/{sectionId}/dishes`                                             | Consumer-facing                                                                 |
| Users              | `GET/PUT/DELETE /users/me/allergen-profile`, `GET/POST/DELETE /users/me/favorites/{restaurantId}`                                                                                              | Profile + favorites                                                             |

**Not yet in API**: Dashboard stats, Menu-Recipe associations, GDPR endpoints.

### Key API Schemas

- **IngredientRequest (create)**:
  `{name, description?, brand?, labelInfo?, allergens[{allergenCode, containmentLevel}]}`
- **IngredientRequest (update)**:
  `{name?, description?, brand?, labelInfo?, allergens?[{allergenCode, containmentLevel}]}`
- **RecipeRequest (create)**:
  `{restaurantId, name, description?, category?, price?, ingredients[{ingredientId, quantity?, unit?}]}`
- **RecipeRequest (update)**:
  `{name?, description?, category?, price?, active?, ingredients?[{ingredientId, quantity?, unit?}]}`
- **MenuRequest**: `{name, description?, displayOrder?}`
- **DishRequest**:
  `{name, sectionId, description?, imageUrl?, available?, displayOrder?, allergens?[{allergenCode, containmentLevel?, notes?}]}`
- **MenuDigitalCardRequest (create)**: `{menuId, dishId}`
- **MenuDigitalCardRequest (update)**: `{dishId}`

## Build & Run

```bash
./gradlew compileKotlinWasmJs    # Compile check
./gradlew wasmJsTest             # Run tests
./gradlew ktlintFormat           # Format code
./gradlew wasmJsBrowserRun       # Run in browser (dev server)
```

## Guardarraíles KMP (SDD harness · build · test · convenciones)

> Valores REALES auditados de este repo. El harness SDD (`sdd-flow`: `tdd-test-writer` /
> `implementer` / `reviewer`) lee esta sección. Proyecto **web-only** (target principal `wasmJs`;
> `:consumerApp` es móvil aparte). Estado de fase en `.claude/.sdd-state.json`.

### Comandos build/test (valores reales)
| Propósito | Comando |
|---|---|
| Compile check (gate rápido) | `./gradlew :adminApp:compileKotlinWasmJs` |
| Tests de la feature (canónico, gate) | `./gradlew :adminApp:wasmJsTest` |
| Tests en navegador (karma+Chrome headless) | `./gradlew :adminApp:wasmJsBrowserTest` |
| Tests JS (target secundario) | `./gradlew :adminApp:jsTest` |
| Todos los targets del módulo | `./gradlew :adminApp:allTests` |
| Lint (gate) | `./gradlew :adminApp:ktlintCheck` |
| Autoformat (uso local, NO gate) | `./gradlew :adminApp:ktlintFormat` |

- **detekt**: no configurado → gate de `/validate` = **ktlintCheck + wasmJsTest + trazabilidad**.
- **Gate de lint scoped a `:adminApp`**, nunca `./gradlew ktlintCheck` repo-wide: existe deuda ktlint
  preexistente en `:shared`/`:consumerApp` (backing-property-naming, generados) que NO es de la feature.
- `:adminApp` ktlint ya excluye `/generated/` (Compose `Res.kt`). Validar con `ktlintCheck` (respeta el
  filtro), **nunca** con `ktlintFormat` (no lo respeta en ktlint-gradle 14.x).
- CI: sin gate de calidad automático en PR todavía (validación local vía este flujo).

### Stack de test
- `kotlin.test` + `kotlinx-coroutines-test` (`runTest`) + `koin-test`. **Fakes a mano.**
- **No hay Turbine ni ktor-client-mock aún** → si un `Scenario` necesita testear un `Flow`, añade
  `libs.turbine` a `commonTest` como parte del `/plan` (no lo asumas disponible).
- Los tests SDD viven en **`adminApp/src/commonTest`** (paquete `org.apptolast.menuadmin.*`), se ejecutan
  con `:adminApp:wasmJsTest`. Ejemplos existentes: `ImportMapperTest`, `JsonExporterTest`, `AllergenFilterTest`.
- `:shared` **no tiene** source-set de test todavía; si una feature necesita testear `:shared`, crear
  `shared/src/commonTest` + deps es una tarea explícita del `/plan`.
- Gherkin→test: cada `Scenario [AC-xx]` → una función `@Test` (nombre en backticks, Given/When/Then).

### expect/actual
- Común en `Foo.kt`; actuals en `Foo.wasmJs.kt` / `Foo.js.kt` (adminApp) y `Foo.android.kt` /
  `Foo.ios.kt` (consumerApp/shared). Nombres lowercase-camelCase.
- Sin `runBlocking`/`GlobalScope` en producción; wasmJs no tiene `String.format()` (formateo manual).

### Arquitectura / DI / UI
- Clean Architecture (domain/data/presentation), state hoisting, `StateFlow` en el ViewModel/root.
- Koin (constructor injection). Módulos data/presentation/platform.
- Previews `@Preview` de `androidx.compose.ui.tooling.preview` en commonMain.
- Errores de UI vía `SnackbarController` + `ErrorSnackbarEffect` (no `Text` rojo inline).
