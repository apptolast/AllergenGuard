#!/usr/bin/env bash
#
# apply-storage-cors.sh — apply (and verify) the CORS policy on the Firebase
# Storage bucket so the web admin (and any browser client) can fetch dish/logo
# images cross-origin.
#
# WHY THIS EXISTS
#   The admin app is a Compose Multiplatform / Kotlin-Wasm site served from
#   https://menusadmin.apptolast.com. It renders remote Firebase Storage images
#   onto a Skia/<canvas> surface and embeds them into the allergen PDF (jsPDF).
#   Both paths read the image bytes cross-origin, so the browser requires the
#   bucket to return an `Access-Control-Allow-Origin` header. Google Cloud
#   Storage only emits that header when the bucket has a CORS policy whose
#   `origin` + `method` match the request. Without it the image load fails with
#   a "CORS error" and the image neither renders nor reaches the PDF.
#
# WHAT IT DOES
#   Applies storage.cors.json to the bucket using the modern `gcloud storage`
#   CLI (falling back to legacy `gsutil`), then prints the active policy back.
#
# REQUIREMENTS
#   - gcloud CLI authenticated against the Firebase project `menusmati`
#     (`gcloud auth login` and `gcloud config set project menusmati`).
#   - The account needs `storage.buckets.update` + `storage.buckets.get`
#     (e.g. roles/storage.admin or Firebase project Owner/Editor).
#
# USAGE
#   ./scripts/apply-storage-cors.sh                 # apply + verify
#   ./scripts/apply-storage-cors.sh --verify-only   # just print the live policy
#
# Verified against the official docs (June 2026):
#   https://docs.cloud.google.com/storage/docs/using-cors
#   https://docs.cloud.google.com/storage/docs/cors-configurations
#   https://firebase.google.com/docs/storage/web/download-files
set -euo pipefail

BUCKET="gs://menusmati.firebasestorage.app"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CORS_FILE="${SCRIPT_DIR}/../storage.cors.json"

# Resolve a gcloud/gsutil binary: prefer PATH, then a user-space install in $HOME.
resolve() {
  if command -v "$1" >/dev/null 2>&1; then command -v "$1"; return 0; fi
  if [ -x "${HOME}/google-cloud-sdk/bin/$1" ]; then echo "${HOME}/google-cloud-sdk/bin/$1"; return 0; fi
  return 1
}

GCLOUD="$(resolve gcloud || true)"
GSUTIL="$(resolve gsutil || true)"

if [ -z "${GCLOUD}" ] && [ -z "${GSUTIL}" ]; then
  echo "ERROR: neither 'gcloud' nor 'gsutil' found on PATH or in \$HOME/google-cloud-sdk/bin." >&2
  echo "       Install the Google Cloud CLI: https://docs.cloud.google.com/sdk/docs/install" >&2
  exit 1
fi

verify() {
  echo "==> Active CORS policy on ${BUCKET}:"
  if [ -n "${GCLOUD}" ]; then
    "${GCLOUD}" storage buckets describe "${BUCKET}" --format="default(cors_config)" \
      || "${GCLOUD}" storage buckets describe "${BUCKET}"
  else
    "${GSUTIL}" cors get "${BUCKET}"
  fi
}

if [ "${1:-}" = "--verify-only" ]; then
  verify
  exit 0
fi

if [ ! -f "${CORS_FILE}" ]; then
  echo "ERROR: CORS file not found: ${CORS_FILE}" >&2
  exit 1
fi

echo "==> Applying CORS policy from ${CORS_FILE} to ${BUCKET}"
if [ -n "${GCLOUD}" ]; then
  # Modern, recommended CLI.
  "${GCLOUD}" storage buckets update "${BUCKET}" --cors-file="${CORS_FILE}"
else
  # Legacy fallback (argument order is reversed vs. gcloud).
  "${GSUTIL}" cors set "${CORS_FILE}" "${BUCKET}"
fi

echo "==> Applied. Verifying…"
verify

cat <<'EOF'

Done. Notes:
  - Browsers cache the CORS preflight for maxAgeSeconds (3600s). If a tab was
    already open, hard-reload or use a fresh session to pick up the change.
  - The policy only affects cross-origin reads; the images must remain publicly
    readable for crossOrigin="anonymous" fetches (they already are).
EOF
