# Firebase Storage CORS — dish & logo images

## Symptom

In the admin web app (`https://menusadmin.apptolast.com`) Firebase Storage
images (dish photos, restaurant/company logos) fail to load. The browser
DevTools **Network** tab shows the requests to

```
https://firebasestorage.googleapis.com/v0/b/menusmati.firebasestorage.app/o/dish-images%2F…
```

with status **(failed) CORS error**, `0.0 kB` transferred. The same failure
prevents the images from being embedded in the **allergen PDF** export.

## Root cause

The app is built with Compose Multiplatform / Kotlin-Wasm. Unlike a plain
`<img>` tag, it renders remote images by **reading their bytes** and drawing
them onto a Skia/`<canvas>` surface (and, for the PDF, into a `<canvas>` →
`toDataURL()` for jsPDF). Any cross-origin read of image pixels requires the
image response to carry an `Access-Control-Allow-Origin` header that matches the
page origin — otherwise the browser blocks the read (the canvas would be
"tainted").

Google Cloud Storage / Firebase Storage **only emits that header when the bucket
has a CORS policy** whose `origin` and `method` match the request. The
`menusmati.firebasestorage.app` bucket had **no CORS policy**, so the download
responses (`?alt=media`) came back without `Access-Control-Allow-Origin`.

Empirically (June 2026), a normal download succeeded, but the same request with
an `Origin: https://menusadmin.apptolast.com` header returned **no**
`access-control-allow-origin` header — confirming the bucket lacked a policy.
(The `OPTIONS` preflight returns `access-control-allow-origin: *`, but that is
the Storage upload/preflight default and does **not** apply to the actual
`GET ?alt=media` download.)

The application code already does the right thing on its side (it requests the
image with `crossOrigin="anonymous"`); the missing piece is purely the bucket
policy.

## Fix

Apply a CORS policy to the bucket. The policy lives in version control at
[`storage.cors.json`](../storage.cors.json) and is applied with
[`scripts/apply-storage-cors.sh`](../scripts/apply-storage-cors.sh).

```jsonc
// storage.cors.json — a BARE ARRAY (no top-level "cors": wrapper)
[
  {
    "origin": ["https://menusadmin.apptolast.com", "http://localhost:8080", "http://localhost:8081"],
    "method": ["GET", "HEAD"],
    "responseHeader": ["Content-Type"],
    "maxAgeSeconds": 3600
  }
]
```

### Apply it

```shell
# Authenticate once against the Firebase project:
gcloud auth login
gcloud config set project menusmati

# Apply + verify (modern, recommended CLI):
./scripts/apply-storage-cors.sh
```

Under the hood that runs:

```shell
gcloud storage buckets update gs://menusmati.firebasestorage.app --cors-file=storage.cors.json
```

The legacy equivalent (still valid) is:

```shell
gsutil cors set storage.cors.json gs://menusmati.firebasestorage.app
```

### Verify

```shell
gcloud storage buckets describe gs://menusmati.firebasestorage.app --format="default(cors_config)"
# or: gsutil cors get gs://menusmati.firebasestorage.app
```

You should see the three origins, `GET`/`HEAD`, and `maxAgeSeconds: 3600`.

## Notes & gotchas

- **Required IAM:** `storage.buckets.update` + `storage.buckets.get`
  (e.g. `roles/storage.admin`, or Firebase project Owner/Editor).
- **`.firebasestorage.app` bucket id:** pass the full string
  `menusmati.firebasestorage.app` as the `gs://` id (not just `menusmati`). New
  default buckets (post-Oct-2024) use `.firebasestorage.app`; they are ordinary
  Cloud Storage buckets and are configured for CORS exactly like legacy
  `.appspot.com` buckets.
- **Public read:** `crossOrigin="anonymous"` sends no `Authorization` header, so
  the objects must remain publicly readable (they already are via download
  tokens / public read).
- **Preflight caching:** browsers cache the result for `maxAgeSeconds` (1 h). If
  a tab was already open before applying, hard-reload or use a fresh session.
- **Add an origin:** edit `storage.cors.json` and re-run the script.

## References (official)

- Set up and view CORS configurations — <https://docs.cloud.google.com/storage/docs/using-cors>
- CORS configuration examples (array-only file; camelCase fields) — <https://docs.cloud.google.com/storage/docs/cors-configurations>
- Download files on Web / CORS — <https://firebase.google.com/docs/storage/web/download-files>
- Use cross-origin images in a canvas (tainting) — <https://developer.mozilla.org/en-US/docs/Web/HTML/How_to/CORS_enabled_image>
