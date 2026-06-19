# Login: Email/Contraseña + Google (Android) + Apple (iOS) — Guía paso a paso

Guía para configurar y mantener la autenticación de **`consumerApp`** (app móvil Android + iOS).
Alcance implementado: **Google solo en Android**, **Apple solo en iOS**, **email/contraseña en ambas**.

> Proyecto Firebase: **`menusmati`** (número `151008360567`) · Bundle/package: **`com.apptolast.menufrontend`** · Apple
> Team: **`3NXH5U7C5A`**

---

## 0. Decisión de arquitectura (por qué NO usamos el SDK GitLive)

A diferencia de FamilyFilm/Municion (que usan `dev.gitlive:firebase-auth` + `signInWithCredential`),
MenuAdmin autentica **vía REST contra Identity Toolkit** desde `:shared`, porque `:shared` también
compila a **wasmJs** (el panel web `adminApp`), donde GitLive no existe. El email/contraseña ya
funcionaba así con un `TokenManager` propio.

Para Google/Apple seguimos el **mismo camino REST** para tener **una única fuente de verdad de sesión**:

```
[Android] Credential Manager ─┐                          ┌─ Identity Toolkit REST
                              ├─ idToken ─> :shared ─────┤   accounts:signInWithIdp ─> idToken+refreshToken
[iOS] Sign in with Apple ─────┘   (+nonce)  FirebaseAuthService                       └─> TokenManager (igual que email/pass)
```

El código nativo (Credential Manager / ASAuthorization) **solo obtiene el idToken del proveedor**; el
canje por sesión Firebase lo hace `FirebaseAuthService.signInWithIdp(...)` en `:shared`.

---

## 1. Qué se implementó en el repo (referencia)

| Capa          | Archivo                                                                  | Rol                                                               |
|---------------|--------------------------------------------------------------------------|-------------------------------------------------------------------|
| `:shared`     | `data/remote/firebase/FirebaseAuthService.kt`                            | nuevo `signInWithIdp(providerId, idToken, rawNonce?)`             |
| `:shared`     | `data/remote/firebase/FirebaseAuthDto.kt`                                | `FirebaseSignInWithIdpRequest` + `displayName` en la respuesta    |
| `commonMain`  | `data/auth/SocialAuthClient.kt`                                          | interfaz `SocialAuthClient` + `SocialSignInResult` + excepciones  |
| `commonMain`  | `data/firebase/FirebaseAuthRepository.kt`                                | `loginWithGoogle()` / `loginWithApple()` → `exchangeIdp()`        |
| `commonMain`  | `di/PlatformModule.kt` (`expect`) + `AppModule.kt`                       | carga `platformModule` en Koin                                    |
| `androidMain` | `data/auth/AndroidSocialAuthClient.kt`                                   | Google con Credential Manager + `SocialAuthActivityHolder`        |
| `androidMain` | `di/PlatformModule.android.kt`, `MainActivity.kt`, `AndroidManifest.xml` | binding, holder de Activity, permiso INTERNET                     |
| `iosMain`     | `data/auth/IosSocialAuthClient.kt`                                       | bridge `IosAppleAuthBridge` (Apple)                               |
| `iosMain`     | `di/PlatformModule.ios.kt`                                               | binding                                                           |
| Swift         | `iosApp/iosApp/SocialAuthCoordinator.swift`                              | `ASAuthorizationController` + nonce, registra el bridge           |
| Swift         | `iosApp/iosApp/iOSApp.swift`                                             | `SocialAuthCoordinator.shared.registerBridges()`                  |
| iOS config    | `iosApp/iosApp/ConsumerApp.entitlements` + `project.yml`                 | capability *Sign in with Apple*                                   |
| UI            | `features/login/...`                                                     | botones Google/Apple condicionados por plataforma                 |
| Build         | `consumerApp/build.gradle.kts`, `gradle/libs.versions.toml`              | libs `credentials`+`googleid`, `BuildConfig.GOOGLE_WEB_CLIENT_ID` |

Los botones se muestran según `isGoogleAvailable` / `isAppleAvailable`: Android reporta Google (si hay
`GOOGLE_WEB_CLIENT_ID`), iOS reporta Apple (si el bridge Swift está registrado). Cada uno se oculta
en la plataforma que no le corresponde.

---

## 2. Firebase Console (una vez)

Consola → proyecto **`menusmati`** → **Authentication → Sign-in method**:

1. **Email/Password** → *Enable*.
2. **Google** → *Enable*. Verás los OAuth clients autogenerados (Android + Web `client_type 3` + iOS).
3. **Apple** → *Enable*. Requiere la config de Apple Developer del paso 4:
    - **Services ID** (identificador del servicio web de Apple)
    - **Apple Team ID** = `3NXH5U7C5A`
    - **Key ID** + **clave privada `.p8`** (Sign in with Apple Key)
    - **OAuth code flow** → copia la *Callback URL* que te da Firebase y pégala en el Services ID de Apple.

### Android — huellas SHA

Firebase necesita las **SHA-1 y SHA-256** de cada keystore que firme la app (debug, release y el de CI),
o Google Sign-In falla con `DEVELOPER_ERROR` / no devuelve idToken.

```bash
# Debug (keystore por defecto de Android Studio)
keytool -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore -storepass android -keypass android | grep -E 'SHA1|SHA256'

# Release (el .jks que crearás en la guía de Fastlane)
keytool -list -v -alias menufrontend -keystore /ruta/menufrontend-release.jks | grep -E 'SHA1|SHA256'
```

Pégalas en **Project settings → Your apps → Android app `com.apptolast.menufrontend` → Add fingerprint**.
Después **descarga el `google-services.json` actualizado** → `consumerApp/google-services.json`.

---

## 3. Apple Developer (una vez, para Apple en iOS)

[developer.apple.com](https://developer.apple.com) → **Certificates, Identifiers & Profiles**:

1. **Identifiers → App IDs →** `com.apptolast.menufrontend` → marca la capability **Sign in with Apple**.
2. **Identifiers → Services IDs →** crea uno (p. ej. `com.apptolast.menufrontend.signin`), habilítalo para
   *Sign in with Apple*, vincúlalo al App ID y añade la **Return URL** que te dio Firebase en el paso 2.3.
3. **Keys →** crea una **Sign in with Apple Key**, descarga el `.p8`, apunta el **Key ID**.
4. Vuelca **Services ID + Team ID + Key ID + `.p8`** en el proveedor **Apple** de Firebase (paso 2.3).

> El entitlement `com.apple.developer.applesignin` ya está en `ConsumerApp.entitlements` y referenciado
> en `project.yml`. Tras cualquier cambio en `project.yml`: `cd consumerApp/iosApp && xcodegen generate`.

---

## 4. Archivos de configuración (locales / no versionados)

| Archivo                                              | Cómo obtenerlo                             | Notas                                                              |
|------------------------------------------------------|--------------------------------------------|--------------------------------------------------------------------|
| `consumerApp/google-services.json`                   | Firebase → Android app                     | gitignored. Web client `client_type 3` = el `GOOGLE_WEB_CLIENT_ID` |
| `consumerApp/iosApp/iosApp/GoogleService-Info.plist` | Firebase → iOS app                         | gitignored                                                         |
| `local.properties` → `GOOGLE_WEB_CLIENT_ID`          | del `google-services.json` (client_type 3) | **ya añadido** para este proyecto                                  |
| `consumerApp/iosApp/iosApp/ConsumerApp.entitlements` | en el repo                                 | capability Apple                                                   |

El valor actual ya configurado en `local.properties`:

```properties
GOOGLE_WEB_CLIENT_ID=151008360567-63uvts5qa5i41mhlb999uf8h7ab57tna.apps.googleusercontent.com
```

> **Importante (Android):** `serverClientId` debe ser el cliente **Web** (`client_type 3`), NO el de
> Android. Si pones el de Android, Credential Manager devuelve `idToken` pero Firebase lo rechaza.

---

## 5. Probar

### Email/contraseña (ambas plataformas)

Ya funcionaba. Login con un usuario existente de Firebase Auth (Email/Password).

### Google (Android)

```bash
./gradlew :consumerApp:installDebug   # o ejecuta desde Android Studio
```

1. Abre la app → pantalla de login → botón **“Continuar con Google”** (solo visible en Android).
2. Elige una cuenta → vuelve autenticado a Home.
3. Si no aparece el botón: falta `GOOGLE_WEB_CLIENT_ID` en `local.properties`.
4. Si falla con `DEVELOPER_ERROR`/sin idToken: falta la **SHA-1** del keystore de debug en Firebase.

### Apple (iOS)

```bash
cd consumerApp/iosApp && xcodegen generate && open ConsumerApp.xcodeproj
```

1. Ejecuta en un **dispositivo real o simulador** con sesión de iCloud iniciada.
2. Login → botón **“Continuar con Apple”** (solo visible en iOS) → hoja del sistema → Face ID/contraseña.
3. Vuelve autenticado a Home.
4. Si falla con `MISSING_OR_INVALID_NONCE`: revisa que el `rawNonce` viaje hasta `signInWithIdp` (ver §6).

---

## 6. Detalle técnico del nonce (Apple)

Sign in with Apple usa nonce anti-replay:

1. Swift genera `rawNonce` aleatorio y firma la petición con `request.nonce = sha256(rawNonce)`.
2. Apple devuelve un `identityToken` cuyo claim `nonce` = `sha256(rawNonce)`.
3. Swift entrega a Kotlin `"idToken|||rawNonce|||displayName"`.
4. Kotlin manda a Identity Toolkit `postBody = id_token=<JWT>&providerId=apple.com&nonce=<rawNonce>`.
5. Firebase re-hashea el `rawNonce` y lo compara con el claim del token. Coincide → sesión válida.

(Para Google no hay nonce: `signInWithIdp("google.com", idToken, rawNonce = null)`.)

---

## 7. Pendiente / opcional (fuera del alcance pedido)

- **Registro email/pass con pantalla propia**: `register()` existe en el repo pero `onNavigateToRegister`
  sigue siendo un TODO en `Navigation.kt`. (Google/Apple crean la cuenta automáticamente vía `signInWithIdp`.)
- **“¿Olvidaste tu contraseña?”**: el handler `ForgotPasswordClicked` está sin implementar.
- **Google en iOS / Apple en Android**: no pedido. Si algún día se quiere, FamilyFilm tiene el patrón
  (GoogleSignIn-iOS por SPM; Apple en Android vía `OAuthProvider` + Custom Tabs).
- Quita las credenciales de desarrollo `a@a.com`/`abcd1234` de `LoginState.kt` antes de publicar.
