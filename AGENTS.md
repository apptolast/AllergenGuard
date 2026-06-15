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
