# Runbook: purga del backend VPS (cuando se decida)

El app ya corre 100% sobre **Firestore + Firebase Auth**. El código del backend VPS
(Spring Boot/PostgreSQL: cliente HTTP + servicios + DTOs + repos REST) se mantiene
**inerte** detrás del feature-flag `USE_FIRESTORE` como *fallback* de pruebas.

Cuando ya no se necesite el fallback, esta es la purga exacta. **Verificado a 2026-06-14**;
revisa contra el código actual antes de ejecutar (los paths pueden moverse).

> Estado actual: con `USE_FIRESTORE=true` **no hay ninguna llamada al VPS en runtime**
> (admin y consumer). El consumer (`:consumerApp`) nunca tuvo código backend → **no le afecta**.

---

## ⚠️ NO borrar (Firebase reutiliza estas piezas)

- `data/remote/auth/TokenManager.kt` — guarda idToken/refreshToken de Firebase Auth.
- `data/remote/ApiClient.kt` → **función `createAuthHttpClient`** — la usa `FirebaseAuthService`
  (Identity Toolkit). Solo se EDITA (ver abajo), no se borra.
- `data/remote/ApiClient.kt` → `ApiException` + `parseErrorResponse` — los usa `createAuthHttpClient`.
- Todo `data/remote/firebase/**` — la capa Firestore/Auth REST.
- `Json`, `ThemePreferences`, `SelectedRestaurantHolder`.

## 1. Fijar Firestore como único modo

En `local.properties` y `shared/build.gradle.kts` deja de depender del flag:
- `shared/build.gradle.kts` (bloque `buildkonfig`): borra las líneas
  `buildConfigField(STRING, "API_BASE_URL", ...)` y `buildConfigField(STRING, "USE_FIRESTORE", ...)`.
  **Conserva** `FIREBASE_API_KEY` y `FIREBASE_PROJECT_ID`.
- `local.properties`: borra `API_BASE_URL=` y `USE_FIRESTORE=` (cosmético, ya no se leen).
- `data/remote/firebase/FirebaseConfig.kt`: borra la propiedad `useFirestore`
  (y su lectura de `BuildKonfig.USE_FIRESTORE`).

## 2. Borrar ficheros (todo backend-only)

**Servicios + DTOs + cliente REST** (`shared/.../data/remote/`):
- `ApiConstants.kt`
- `PageResponseDto.kt`
- `mapper/ApiMapper.kt`
- `auth/AuthDto.kt`, `auth/AuthService.kt`  *(NO `auth/TokenManager.kt`)*
- `dish/`, `ingredient/`, `menu/`, `menudigitalcard/`, `recipe/`, `restaurant/`
  (cada uno = su `*Dto.kt` + `*Service.kt`)

**Repositorios REST** (`shared/.../data/repository/`):
- `RemoteAuthRepository.kt`, `RemoteIngredientRepository.kt`, `RemoteRestaurantRepository.kt`,
  `RemoteRecipeRepository.kt`, `RemoteMenuRepository.kt`, `RemoteDishRepository.kt`,
  `RemoteMenuDigitalCardRepository.kt`, `ApiDashboardRepository.kt`

**Interfaces de dominio sin impl Firestore ni UI** (`shared/.../domain/repository/`):
- `DishRepository.kt`, `MenuDigitalCardRepository.kt`
  *(verifica antes: `grep -rn "DishRepository\|MenuDigitalCardRepository" --include=*.kt adminApp consumerApp`
  no debe devolver nada en `presentation/`)*

## 3. Editar `ApiClient.kt` (conservar solo el cliente de auth)

- Borra la función `createHttpClient(...)` completa (es el cliente autenticado contra el backend,
  con interceptor de refresh + `ApiConstants.BASE_URL`).
- En `createAuthHttpClient`, **quita** el bloque `defaultRequest { url(ApiConstants.BASE_URL); ... }`
  (deja `contentType(ContentType.Application.Json)` si lo quieres). Firebase pasa URLs absolutas,
  así que `BASE_URL` no se usa — esto es lo que permite borrar `ApiConstants.kt`.

## 4. Editar `adminApp/.../di/DataModule.kt`

- Borra `single { createHttpClient(get(), get()) }`.
- Borra los bindings de servicios backend: `AuthService`, `RestaurantService`, `MenuService`,
  `DishService`, `IngredientService`, `RecipeService`, `MenuDigitalCardService`.
- Sustituye el `if (FirebaseConfig.useFirestore) { … } else { … }` por **solo** la rama Firestore
  (sin condicional): `FirebaseAuthRepository`, `FirestoreIngredientRepository`,
  `FirestoreRestaurantRepository`, `FirestoreRecipeRepository`, `FirestoreMenuRepository`,
  `FirestoreDashboardRepository`.
- Borra los bindings `RemoteDishRepository` y `RemoteMenuDigitalCardRepository`.
- **Conserva**: `Json`, `TokenManager`, `createAuthHttpClient(named("auth"))`, `FirebaseAuthService`,
  `createFirestoreHttpClient(named("firestore"))`, `FirestoreClient`, los `Firestore*Repository`,
  `ThemePreferences`, `SelectedRestaurantHolder`.
- Limpia los imports que queden sin usar.

## 5. Tests

- Borra/ajusta cualquier test que referencie clases backend eliminadas
  (`grep -rln "Remote.*Repository\|.*Service(" adminApp/src/commonTest`).
- `ImportMapperTest` / `JsonExporterTest` usan `data/dto` + `data/mapper` (formato de import
  externo, **no** VPS) → no se tocan aquí.

## 6. Verificación

```bash
./gradlew :adminApp:compileKotlinWasmJs    # admin compila sin backend
./gradlew :consumerApp:assembleDebug       # consumer intacto
./gradlew wasmJsTest                        # tests verdes
./gradlew ktlintFormat
```

Luego apaga la VPS (Docker/servidor) — el código ya no la conoce.

---

## Limpieza relacionada (independiente del VPS, opcional)

`JsonExporter.exportExternalData` / `importExternalData` + `data/dto/ImportDto.kt` +
`data/mapper/ImportMapper.kt` son un **formato de import externo** que el backup actual ya no usa
(el `BackupViewModel` usa `exportAllData`/`importAllData` con `BackupData`). Solo lo cubren sus tests.
Si se confirma que ninguna pantalla lo invoca, se puede retirar aparte.
