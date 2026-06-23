#!/usr/bin/env bash
# Runtime verification of the multi-tenant Firestore security rules against a real id token.
# Usage:  scripts/test-firestore-rules.sh [debug|default]      (default arg: debug)
# Reads FIREBASE_API_KEY (web key) from local.properties. Assumes the target DB is seeded as per
# docs/multitenant-refactor-handoff.md (accounts acc_alacor + acc_test2, invitations owner2@/manager@).
# Test users are created on demand with password Test1234!.
set -uo pipefail
cd "$(dirname "$0")/.."

ARG="${1:-debug}"
if [ "$ARG" = "default" ]; then DBID="(default)"; else DBID="$ARG"; fi
KEY=$(grep -E '^FIREBASE_API_KEY=' local.properties | head -1 | cut -d= -f2)
if [ -z "$KEY" ]; then echo "FIREBASE_API_KEY not found in local.properties"; exit 1; fi
PW="Test1234!"
IT="https://identitytoolkit.googleapis.com/v1/accounts"
DB="https://firestore.googleapis.com/v1/projects/menusmati/databases/$DBID/documents"
echo "### Testing rules on database: $DBID ###"
echo "public restaurants list -> $(curl -s -o /dev/null -w '%{http_code}' "$DB/restaurants")  (expect 200 if rules served)"

signin() { # email -> sets TOKEN, USERID
  local email="$1"
  local r
  r=$(curl -s -X POST "$IT:signUp?key=$KEY" -H 'Content-Type: application/json' -d "{\"email\":\"$email\",\"password\":\"$PW\",\"returnSecureToken\":true}")
  if echo "$r" | jq -e '.error' >/dev/null 2>&1; then
    r=$(curl -s -X POST "$IT:signInWithPassword?key=$KEY" -H 'Content-Type: application/json' -d "{\"email\":\"$email\",\"password\":\"$PW\",\"returnSecureToken\":true}")
  fi
  TOKEN=$(echo "$r" | jq -r '.idToken // empty'); USERID=$(echo "$r" | jq -r '.localId // empty')
}
req() { # method path [data]
  local m="$1" p="$2" d="${3:-}"
  if [ -n "$d" ]; then curl -s -o /dev/null -w "%{http_code}" -X "$m" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "$DB/$p" -d "$d"
  else curl -s -o /dev/null -w "%{http_code}" -X "$m" -H "Authorization: Bearer $TOKEN" "$DB/$p"; fi
}

echo "--- owner2 = ACCOUNT_ADMIN of acc_test2 (cross-account isolation) ---"
signin "owner2@test.com"
if [ "$(req GET "memberships/$USERID")" = "404" ]; then
  echo "forge acc_alacor    -> $(req POST "memberships?documentId=$USERID" '{"fields":{"accountId":{"stringValue":"acc_alacor"},"role":{"stringValue":"ACCOUNT_ADMIN"},"restaurantIds":{"arrayValue":{}}}}')  (expect 403)"
  echo "materialize acc_test2   -> $(req POST "memberships?documentId=$USERID" '{"fields":{"accountId":{"stringValue":"acc_test2"},"role":{"stringValue":"ACCOUNT_ADMIN"},"restaurantIds":{"arrayValue":{}}}}')  (expect 200)"
fi
echo "read OWN acc_test2 ingr -> $(req GET "accounts/acc_test2/ingredients")  (expect 200)"
echo "read OTHER acc_alacor-> $(req GET "accounts/acc_alacor/ingredients")  (expect 403)"
echo "create restaurant other -> $(req POST "restaurants?documentId=zz-hack" '{"fields":{"accountId":{"stringValue":"acc_alacor"},"name":{"stringValue":"x"}}}')  (expect 403)"

echo "--- manager = RESTAURANT_MANAGER of acc_alacor, scoped to la-brava-piconera ---"
signin "manager@apptolast.com"
if [ "$(req GET "memberships/$USERID")" = "404" ]; then
  echo "forge extra restaurant  -> $(req POST "memberships?documentId=$USERID" '{"fields":{"accountId":{"stringValue":"acc_alacor"},"role":{"stringValue":"RESTAURANT_MANAGER"},"restaurantIds":{"arrayValue":{"values":[{"stringValue":"el-rincon-del-mar"},{"stringValue":"la-brava-piconera"}]}}}}')  (expect 403)"
  echo "materialize (assigned)  -> $(req POST "memberships?documentId=$USERID" '{"fields":{"accountId":{"stringValue":"acc_alacor"},"role":{"stringValue":"RESTAURANT_MANAGER"},"restaurantIds":{"arrayValue":{"values":[{"stringValue":"la-brava-piconera"}]}}}}')  (expect 200)"
fi
echo "read acct ingredients   -> $(req GET "accounts/acc_alacor/ingredients")  (expect 200: member)"
echo "WRITE ingredient        -> $(req POST "accounts/acc_alacor/ingredients?documentId=zz-x" '{"fields":{"name":{"stringValue":"x"}}}')  (expect 403: not admin)"
echo "recipe ASSIGNED rest    -> $(req PATCH "restaurants/la-brava-piconera/recipes/zz-test" '{"fields":{"name":{"stringValue":"t"}}}')  (expect 200)"
echo "recipe OTHER rest       -> $(req PATCH "restaurants/el-rincon-del-mar/recipes/zz-test" '{"fields":{"name":{"stringValue":"x"}}}')  (expect 403)"
echo "cleanup test recipe     -> $(req DELETE "restaurants/la-brava-piconera/recipes/zz-test")  (expect 200)"
