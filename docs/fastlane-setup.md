# Fastlane — Despliegue de `consumerApp` (Android → Play Internal · iOS → TestFlight)

Réplica del setup AppToLast (FamilyFilm/Municion) adaptada a la estructura de MenuAdmin
(`:consumerApp` = módulo único que produce app Android + framework iOS, con el wrapper Xcode en
`consumerApp/iosApp`).

> **Modelo tag-driven:** haces `git push` de un tag `vX.Y.Z` → GitHub Actions construye y sube a
> **Play Internal testing (DRAFT)** y **TestFlight**. Ninguno se publica solo: revisas y publicas a mano.
> `versionName` = el tag; `versionCode`/`buildNumber` se calculan consultando la store en runtime
> (no se commitea nada).

---

## 1. Qué se añadió al repo

```
Gemfile                                  # gem "fastlane"
keystore.properties.example              # plantilla de firma Android (copiar a keystore.properties)
consumerApp/fastlane/Appfile|Fastfile    # lanes Android (Play)
consumerApp/iosApp/fastlane/
  Appfile | Matchfile | Fastfile         # lanes iOS (match + TestFlight)
  .env.example                           # plantilla de credenciales iOS (copiar a .env)
.github/workflows/deploy-android.yml     # CI tag-driven Android
.github/workflows/deploy-ios.yml         # CI tag-driven iOS
```

Además: `consumerApp/build.gradle.kts` ahora lee `versionCode/versionName` de propiedades Gradle
(`-PappVersionCode/-PappVersionName`) y firma release desde `keystore.properties` (si existe).

Identificadores: **package/bundle** `com.apptolast.menufrontend` · **Apple Team** `3NXH5U7C5A` ·
**scheme** `ConsumerApp` · **AAB** `consumerApp/build/outputs/bundle/release/consumerApp-release.aab`.

---

## 2. Requisitos previos (una vez)

```bash
cd /ruta/MenuAdmin
bundle install                                   # instala fastlane (usa el Gemfile raíz)
cp keystore.properties.example keystore.properties
cp consumerApp/iosApp/fastlane/.env.example consumerApp/iosApp/fastlane/.env
```

### 2.1 Firma Android (keystore)

```bash
keytool -genkeypair -v -keystore /ruta/segura/menufrontend-release.jks \
  -alias menufrontend -keyalg RSA -keysize 2048 -validity 10000
```

Rellena `keystore.properties` con la ruta y las contraseñas. **Añade su SHA-1/256 a Firebase**
(ver `auth-google-apple-setup.md` §2) o Google Sign-In fallará en builds release.

### 2.2 Google Play Console

- Crea la app `com.apptolast.menufrontend` y haz **una primera subida manual** (Play exige el primer
  AAB subido a mano antes de permitir la API).
- **Service account** con permiso *Release manager* → descarga el JSON → ruta en `.env`
  (`SUPPLY_JSON_KEY`) o secret de CI.

### 2.3 Apple App Store Connect

- Crea la app `com.apptolast.menufrontend`.
- **App Store Connect API Key** (rol *App Manager*/*Developer*): apunta `KEY_ID`, `ISSUER_ID`, descarga
  el `.p8` → ruta en `.env` (`ASC_API_KEY_PATH`).
- Rellena `FASTLANE_TEAM_ID=3NXH5U7C5A` y `APPLE_ID` en `.env`.

### 2.4 Firma iOS (match)

Reutiliza el repo compartido **`apptolast/ios-certificates`** (el certificado de distribución ya
existe; solo se crea un *provisioning profile* nuevo para este bundle):

```bash
# rellena MATCH_GIT_URL y MATCH_PASSWORD en .env, luego:
cd consumerApp/iosApp
bundle exec fastlane ios bootstrap_match
```

---

## 3. Comandos de los lanes

```bash
# === Android (desde consumerApp/) ===
cd consumerApp
bundle exec fastlane android version            # muestra versión por defecto
bundle exec fastlane android validate_play      # comprueba credenciales de Play
bundle exec fastlane android build              # AAB firmado, sin subir
bundle exec fastlane android internal           # build + sube a Play Internal (DRAFT)

# === iOS (desde consumerApp/iosApp/) ===
cd consumerApp/iosApp
bundle exec fastlane ios match_certificates     # sincroniza firma (readonly)
bundle exec fastlane ios build                  # .ipa Release local, sin subir
bundle exec fastlane ios beta                   # build + sube a TestFlight
bundle exec fastlane ios beta version_name:1.0.0

# === Release real (ambas plataformas) ===
git tag v1.0.0 && git push origin v1.0.0        # dispara los dos workflows de CI
```

> Hay que `cd` al directorio del módulo: fastlane no busca hacia arriba la carpeta `fastlane/`
> (Bundler sí encuentra el `Gemfile` raíz).

---

## 4. Secrets de GitHub Actions (environment `release`)

### Android (`deploy-android.yml`)

| Secret                                                                  | Qué es                                         |
|-------------------------------------------------------------------------|------------------------------------------------|
| `SIGNING_KEY_B64`                                                       | `base64 -i menufrontend-release.jks`           |
| `SIGNING_KEY_STORE_PASSWORD` · `SIGNING_ALIAS` · `SIGNING_KEY_PASSWORD` | credenciales del keystore                      |
| `SERVICE_ACCOUNT_GOOGLE_PLAY_CONSOLE_JSON`                              | contenido del JSON del service account de Play |
| `FIREBASE_JSON`                                                         | `base64 -i consumerApp/google-services.json`   |
| `FIREBASE_API_KEY` · `FIREBASE_PROJECT_ID`                              | para BuildKonfig de `:shared`                  |
| `GOOGLE_WEB_CLIENT_ID`                                                  | web OAuth client (client_type 3)               |

### iOS (`deploy-ios.yml`)

| Secret                                                                  | Qué es                                                         |
|-------------------------------------------------------------------------|----------------------------------------------------------------|
| `APP_STORE_CONNECT_API_KEY_ID` · `_ISSUER_ID`                           | de la API Key                                                  |
| `APP_STORE_CONNECT_API_KEY_BASE64`                                      | `base64 -i AuthKey_XXXX.p8`                                    |
| `MATCH_GIT_URL` · `MATCH_PASSWORD`                                      | repo de certificados + passphrase                              |
| `MATCH_GIT_BASIC_AUTHORIZATION`                                         | `echo -n "usuario:PAT" \| base64` (acceso al repo de certs)    |
| `FASTLANE_TEAM_ID` (`3NXH5U7C5A`) · `FASTLANE_ITC_TEAM_ID` · `APPLE_ID` | identidad Apple                                                |
| `GOOGLE_SERVICE_INFO_PLIST_B64`                                         | `base64 -i consumerApp/iosApp/iosApp/GoogleService-Info.plist` |
| `FIREBASE_API_KEY` · `FIREBASE_PROJECT_ID` · `GOOGLE_WEB_CLIENT_ID`     | para BuildKonfig en el build iOS                               |

---

## 5. Notas y diferencias respecto a FamilyFilm/Municion

- **Sin Firebase App Distribution** ni envío automático a revisión (igual que los proyectos hermanos):
  Android → Internal **draft**, iOS → TestFlight interno **sin** submission.
- El Xcode project (`ConsumerApp.xcodeproj`) lo genera **XcodeGen** desde `project.yml`. CI ejecuta
  `xcodegen generate` antes de compilar; en local hazlo tras tocar `project.yml`.
- El `project.yml` ya compila+embebe el framework KMP vía pre-build script
  (`:consumerApp:embedAndSignAppleFrameworkForXcode`); el lane solo añade un `linkRelease…` de fail-fast.
- `keystore.properties`, `*.jks`, `.env`, `google-services.json` y `GoogleService-Info.plist` están en
  `.gitignore`. **No los commitees**; en CI se inyectan desde secrets.
- `consumerApp` compila release **sin firmar** si falta `keystore.properties` (degradación elegante).
