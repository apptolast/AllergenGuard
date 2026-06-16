# AllergenGuard

**Allergen-aware digital menus — a restaurant admin platform and a diner mobile app, built from a single Kotlin
Multiplatform codebase.**

Eating out with food allergies is stressful: menus rarely make it clear which dishes are safe. AllergenGuard fixes both
sides of that problem. Restaurants describe their dishes once (ingredients, the 14 EU allergens, photos) and publish a
menu; diners set their allergy profile once and then see every restaurant's menu **pre-filtered to what they can safely
eat**.

The whole product — a web admin panel and a mobile app for Android & iOS — shares one Kotlin/Compose Multiplatform
codebase backed by Firebase.

---

## Repository layout

A Gradle monorepo with three modules:

```
.
├── shared/        # Domain models + Firebase data layer (Auth/Firestore/Storage over REST). Reused by both apps.
├── adminApp/      # Restaurant management platform — Compose Multiplatform for Web (Wasm/JS).
├── consumerApp/   # Diner app — Compose Multiplatform for Android & iOS.
├── firestore.rules / storage.rules   # Firebase security rules
└── k8s/           # Container deployment for the admin web app
```

---

## Admin platform (web)

The control center where restaurant staff build their catalogue and decide what diners see.

- **Restaurants** — multi-tenant; each has its own ingredients, recipes and menus.
- **Ingredients** — a catalogue where each ingredient declares its allergens with a *containment level* (Contains / May
  contain / Free of).
- **Recipes & dishes** — built from ingredients, with **allergens computed automatically**, a customer-facing
  description, and a photo (compressed client-side to WebP and stored in Firebase Storage).
- **Menus** — curated *subsets* of a restaurant's recipes (e.g. "Summer menu", "Winter menu"). **Exactly one menu is
  active (published) per restaurant** — that's the one diners see.
- **Allergen matrix & dashboard** — at-a-glance allergen coverage, recent activity, and most-common allergens.
- **Backup & restore** — JSON export/import of the whole catalogue.
- The **14 EU allergens** are first-class, with *traces* ("may contain") shown distinctly from definite allergens.

## Mobile app (Android & iOS)

The diner-facing experience.

- **Browse** nearby restaurants and keep **favorites**.
- Each restaurant shows **only its active menu**.
- A personal **allergy profile** (the 14 EU allergens) is saved to the account and **auto-applied as filters** on every
  menu.
- Dishes are **pre-filtered to what's safe**; a toggle reveals the rest, with unsafe dishes highlighted in red.
- **Sort** dishes by name, price or category (grouped with headers), ascending or descending.
- **Dish detail** with photo, ingredients, allergen badges and a clear safe / warning banner.

---

## Technical overview

- **Kotlin Multiplatform + Compose Multiplatform** — one UI and one business-logic codebase across Android, iOS and
  Web (Wasm/JS).
- **Clean architecture + MVVM** — `data` / `domain` / `presentation` layers; UI state exposed as `StateFlow`, screens
  are stateless and preview-friendly.
- **Backend: Firebase over REST.** Auth (Identity Toolkit), Firestore and Storage are accessed through **Ktor** from the
  `:shared` module instead of the Firebase SDK, so the exact same data layer runs on Wasm (where the SDK isn't
  available) as on mobile.
- **DI:** Koin · **Serialization:** kotlinx-serialization · **Images:** Coil 3 · **Navigation:** type-safe Navigation
  Compose · **Build config/secrets:** BuildKonfig (from `local.properties`) · **Design:** Material 3 with light/dark
  themes.
- Platform specifics use `expect`/`actual` (e.g. app version, file/image handling on web).
- Key versions: Kotlin 2.4 · Ktor 3.5 · Koin 4.2 · Coil 3.5 · AGP 9.

---

## Build & run

**Prerequisites:** JDK 17+, the Android SDK (for the mobile app), and a Firebase project. Provide the Firebase config in
`local.properties` (injected at build time via BuildKonfig):

```properties
FIREBASE_API_KEY=...
FIREBASE_PROJECT_ID=...
USE_FIRESTORE=true
```

**Admin (web, dev server):**

```shell
./gradlew :adminApp:wasmJsBrowserDevelopmentRun
```

**Consumer (Android):**

```shell
./gradlew :consumerApp:assembleDebug      # build the debug APK
./gradlew :consumerApp:installDebug       # install on a connected device/emulator
```

**Consumer (iOS):** the iOS targets produce the `ConsumerApp` framework; run it from an Xcode host app.

**Checks & formatting:**

```shell
./gradlew :adminApp:compileKotlinWasmJs           # web compile check
./gradlew :consumerApp:compileDebugKotlinAndroid  # android compile check
./gradlew ktlintFormat                            # format (shared + adminApp)
```

Firebase security rules live in `firestore.rules` and `storage.rules` and are deployed with `firebase deploy`.
