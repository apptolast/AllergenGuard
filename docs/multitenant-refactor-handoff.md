# Multi-tenant + Roles + Entornos — Handoff (para continuar tras reinicio)

> Estado a 2026-06-22. Rama: `feature/improve-multitenant`. Plan original:
> `~/.claude/plans/a-continuacion-necesito-que-virtual-moler.md`. Memoria de proyecto (se carga
> sola cada sesión): `~/.claude/projects/.../memory/multitenant_refactor.md`.

## 1. Objetivo y decisiones

Convertir el admin single-tenant en **multi-tenant** para poder vender el producto a otras empresas
tipo Mati o a restaurantes que se autogestionen (incl. UK), con **cambios mínimos de UI** y baja
complejidad. Decisiones tomadas con el usuario:

1. **Roles dentro de una cuenta**: `ACCOUNT_ADMIN` (ve todos los restaurantes + catálogo de
   ingredientes de su cuenta) y `RESTAURANT_MANAGER` (solo restaurantes asignados). El dueño de la
   plataforma provisiona cuentas manualmente (sin UI in-app en MVP).
2. **Enforcement sin Cloud Functions**: documentos de **membresía** + reglas Firestore con `get()`.
   Onboarding por **invitación** (email→cuenta+rol); en el primer login el cliente **materializa su
   propia membresía** y las reglas validan que coincide con la invitación (no puede auto-promocionarse).
3. **Alérgenos**: se mantienen los **14 compartidos** (UE = UK). Se añaden `region`+`language` a la
   cuenta. Vegano/intolerancias/filtros extendidos → **aplazado** (modelo listo para añadir como datos).
4. **Entornos**: un proyecto Firebase (`menusmati`) con **2 bases Firestore**: `(default)` = **release**
   (reutilizada) y `debug` = **nueva**. Selección por compilación vía `FIRESTORE_DATABASE_ID`.

Restricción técnica clave: el `FirestoreClient` (REST, wasmJs) **solo lista por ruta** (no hay
`runQuery`/`where`) → todo el scoping es por subcolección o filtrado en cliente.

## 2. Modelo de datos final (idéntico en ambas bases)

```
accounts/{accountId}                         {name, region("EU"|"UK"), language("es"|"en")}
accounts/{accountId}/ingredients/{id}        catálogo PRIVADO por cuenta (subcolección)
restaurants/{restaurantId}                   top-level + campo accountId (consumer los lee global)
    /recipes/{recipeId}                       (sin cambios de ruta; heredan cuenta del restaurante)
    /menus/{menuId}
    /reviews/{reviewId}
memberships/{uid}                            {accountId, role, restaurantIds[]} (auto-creada en 1er login)
invitations/{email}                          {accountId, role, restaurantIds[]} (provisión manual)
allergens/{code}                             14 compartidos (público)
users/{uid}                                  perfiles consumer (sin cambios)
```

`whitelist` queda reemplazado por `invitations` (la colección vieja sigue ahí, inerte).

## 3. Estado: TODO HECHO Y VERIFICADO

### Código (compila + ktlint limpio salvo `Res.kt` generado = deuda preexistente)

Verificado: `./gradlew :adminApp:compileKotlinWasmJs` ✓ y `:consumerApp:compileDebugKotlinAndroid` ✓.

**Nuevos** (en `:shared`):

- `domain/model/AccountRole.kt`, `AccountSession.kt`, `AccountInvitation.kt`
- `data/CurrentAccountHolder.kt` (singleton, calcado de `SelectedRestaurantHolder`)
- `domain/repository/MembershipRepository.kt` + `data/repository/FirestoreMembershipRepository.kt`

**Modificados**:

- `shared/build.gradle.kts` — `FIRESTORE_DATABASE_ID` (BuildKonfig, default `(default)`, override `-P`/local.properties)
- `shared/.../data/remote/firebase/FirebaseConfig.kt` — `var databaseId` + `firestoreDocuments` usa la base
- `shared/.../data/repository/FirestoreIngredientRepository.kt` — ruta a `accounts/{accountId}/ingredients`
- `shared/.../data/repository/FirestoreRestaurantRepository.kt` — filtro cliente por cuenta/rol + estampa accountId del
  holder
- `shared/.../data/util/JsonExporter.kt` — `accountId` en `BackupData` + param en `exportAllData`
- `adminApp/.../di/DataModule.kt` — registra `CurrentAccountHolder` + `MembershipRepository`
- `adminApp/.../di/PresentationModule.kt` — `BackupViewModel` ahora con 8 deps
- `adminApp/.../screens/auth/AuthViewModel.kt` + `AuthUiState.kt` — bootstrap de sesión (gate por invitación,
  materialización, `isResolvingSession`)
- `adminApp/.../App.kt` — rama de carga mientras resuelve sesión
- `adminApp/.../screens/backup/BackupViewModel.kt` — `accountId` en export + validación al importar app-native
- `adminApp/.../screens/menus/MenusViewModel.kt` — **fix preexistente**: faltaba `import ...RestaurantRepository` (la
  rama no compilaba)
- `consumerApp/.../MenuFrontendApplication.kt` —
  `FirebaseConfig.databaseId = if (BuildConfig.DEBUG) "debug" else "(default)"`
- `firestore.rules`, `storage.rules` — reescritas (membresía+roles); validadas OK
- `firebase.json` — `firestore` = array de targets `[(default), debug]`
- `local.properties.example` — documenta `FIRESTORE_DATABASE_ID`

### Infra en vivo (proyecto `menusmati`)

- **`(default)` / release MIGRADO**: creado `accounts/acc_apptolast`; **144 ingredientes** movidos de
  top-level `ingredients` → `accounts/acc_apptolast/ingredients` (la colección vieja top-level quedó
  INERTE como respaldo, opcional borrarla); invitaciones para los 4 admins reales (admin@apptolast.com,
  a@a.com, senchiviarco@gmail.com, v32losam@gmail.com = ACCOUNT_ADMIN); reglas Firestore+Storage
  desplegadas. Restaurantes ya tenían `accountId`.
- **`debug`**: ⚠️ la primera versión se creó por la **API de admin** y NO servía reglas (deny-all). Se
  **borró y recreó vía Firebase CLI** (`firebase firestore:databases:create debug --location=europe-west2`)
  → ahora registrada en Firebase y sirve reglas. Re-sembrada: acc_apptolast + acc_test2 (prueba),
  14 alérgenos, ingredientes, 3 restaurantes, invitaciones (admin@/a@/manager@→la-brava-piconera/owner2@).

### Pruebas de reglas en runtime — PASADAS al 100% (en ambas bases)

Con idToken real (no solo validación sintáctica):

- Forja de membresía (auto-promoción a otra cuenta) → **403**; materialización correcta → **200**.
- Leer datos de la cuenta propia → **200**; de OTRA cuenta → **403** (aislamiento).
- Crear restaurante en otra cuenta → **403**; lista pública de restaurantes (consumer) → **200**.
- Manager: edita recetas de su restaurante asignado → **200**; de otro restaurante de la misma cuenta
  → **403**; lee catálogo (es miembro) → **200**; escribe ingrediente (no es admin) → **403**; no puede
  auto-asignarse restaurantes extra en la membresía → **403**.

## 4. PENDIENTE (próximos pasos)

1. **Desplegar el admin con el código nuevo** (web). El admin antiguo ya desplegado dejará de escribir
   contra `(default)` bajo las reglas nuevas hasta que subas el nuevo. El consumer no se ve afectado.
   Cada admin materializa su membresía al primer login.
2. **Commit** de los cambios (rama `feature/improve-multitenant`) → PR. Lista de ficheros en §3.
3. **Limpiezas opcionales** (cuando confirmes que el admin lee bien la subcolección):
    - Borrar la colección antigua top-level `ingredients` en `(default)` (144 docs, respaldo inerte).
    - Borrar el usuario de prueba `owner2@test.com` (y `manager@apptolast.com` si lo creaste) de Auth.
    - Borrar la colección `whitelist` (ya inerte) y `FirestoreWhitelistRepository` + su binding si quieres
      limpiar del todo (hoy quedan inertes para no romper el build).
4. **Diferidos (post-MVP)**: i18n completa del admin a EN (hoy usa `AllergenType.nameEs` hardcodeado;
   el seam es `AllergenType.name(language)`); filtros dietéticos genéricos (vegano/intolerancias).
5. **iOS consumer**: hoy usa la base por el default de BuildKonfig (`(default)`). Si quieres dev contra
   `debug` en iOS, hay que setear `FirebaseConfig.databaseId` en el arranque iOS (equivalente a lo que
   hace `MenuFrontendApplication` en Android con `BuildConfig.DEBUG`).

## 5. Cómo continuar / probar

- **Dev del admin contra `debug`**: añade `FIRESTORE_DATABASE_ID=debug` a `local.properties`, o
  `./gradlew :adminApp:wasmJsBrowserRun -PFIRESTORE_DATABASE_ID=debug`. Sin override → `(default)`.
- **Consumer Android**: build debug → `debug`, build release → `(default)` (automático).
- **Cuentas de prueba en `debug`** (password `Test1234!` para las creadas en el test):
    - `admin@apptolast.com` / `a@a.com` → ACCOUNT_ADMIN de acc_apptolast (ven 2 restaurantes + ingredientes).
    - `manager@apptolast.com` → RESTAURANT_MANAGER, solo `la-brava-piconera`.
    - `owner2@test.com` → ACCOUNT_ADMIN de acc_test2 (solo "Test Bistro UK" + Cheddar).
- **Re-ejecutar la prueba de reglas**: `scripts/test-firestore-rules.sh debug` (lee la API key de
  `local.properties`). Cambia `debug`→`default` para probar release.

## 6. Gotchas / aprendizajes (no repetir)

- **Bases Firestore con nombre: créalas con el Firebase CLI**, NO con la API de admin (MCP
  `firestore_create_database`) — las creadas por API no se registran en Firebase y **no sirven Security
  Rules** (todo 403, deny-all), aunque publiques reglas por Consola o CLI.
- Tras borrar una base, su id queda **no disponible ~290s** antes de poder recrearla.
- `storage.rules` comprueba membresía en `(default)` (el bucket es único por proyecto). Subir imágenes
  desde un build `debug` requiere que el admin tenga también membresía en `(default)`.
- El `FirestoreClient` REST no tiene `runQuery` → no introducir reglas/repos que filtren por campo.
- kotlinx-serialization omite defaults → `restaurantIds` se escribe SIEMPRE (vacío para admin) para que
  la regla `==` con la invitación cuadre.
- ktlint falla repo-wide solo en `Res.kt` generado (deuda preexistente) — no es regresión.
- La API key de `google-services.json` (Android) está restringida por paquete (bloquea curl); para REST
  usa la `FIREBASE_API_KEY` web de `local.properties`.
- `gcloud auth print-access-token` caduca y pide re-login interactivo; el Firebase CLI y el MCP de
  Firebase mantienen su propia auth.
