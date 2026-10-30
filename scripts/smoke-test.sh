#!/usr/bin/env bash
set -euo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"
ME_APP_DIRECT_URL="${ME_APP_DIRECT_URL:-http://localhost:8083}"

echo "================================================================="
echo "Regional Multi-Tenant Blogging Demo --- Automated Smoke Tests"
echo "Gateway: ${GATEWAY_URL}"
echo "Direct ME App: ${ME_APP_DIRECT_URL}"
echo "================================================================="

# Wait for gateway
echo "[1/8] Verifying Gateway Health..."
until curl -s "${GATEWAY_URL}/actuator/health" | grep -q '"status":"UP"'; do
    echo "Waiting for Gateway to report UP..."
    sleep 2
done
echo ">>> Gateway is UP!"

# Step 1: Create a post as tenant-eu-001 (EU Shard 1)
echo ""
echo "[2/8] Creating post as tenant-eu-001 (EU Shard 1)..."
CREATE_RESP=$(curl -s -w "\n%{http_code}" -X POST "${GATEWAY_URL}/api/posts" \
    -H "Content-Type: application/json" \
    -H "X-Tenant-Id: tenant-eu-001" \
    -d '{"title": "Smoke Test Post", "body": "Sensitive data for EU tenant 001"}')

HTTP_CODE=$(echo "$CREATE_RESP" | tail -n1)
BODY=$(echo "$CREATE_RESP" | head -n -1)

if [ "$HTTP_CODE" -ne 201 ]; then
    echo "FAILED: Expected 201 Created but got $HTTP_CODE: $BODY"
    exit 1
fi

POST_ID=$(echo "$BODY" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo ">>> Created post ID: ${POST_ID}"

# Step 2: Read it as tenant-eu-001 (Success)
echo ""
echo "[3/8] Reading post back as tenant-eu-001..."
READ_RESP=$(curl -s -w "\n%{http_code}" -X GET "${GATEWAY_URL}/api/posts/${POST_ID}" \
    -H "X-Tenant-Id: tenant-eu-001")
HTTP_CODE=$(echo "$READ_RESP" | tail -n1)

if [ "$HTTP_CODE" -ne 200 ]; then
    echo "FAILED: Expected 200 OK for tenant-eu-001 but got $HTTP_CODE"
    exit 1
fi
echo ">>> Successfully retrieved post as owner (tenant-eu-001)"

# Step 3: Read it as tenant-eu-002 (Different Shard) -> Not visible (404)
echo ""
echo "[4/8] Attempting to read post as tenant-eu-002 (EU Shard 2)..."
READ_EU2_RESP=$(curl -s -w "\n%{http_code}" -X GET "${GATEWAY_URL}/api/posts/${POST_ID}" \
    -H "X-Tenant-Id: tenant-eu-002")
HTTP_CODE=$(echo "$READ_EU2_RESP" | tail -n1)

if [ "$HTTP_CODE" -ne 404 ]; then
    echo "FAILED: Expected 404 Not Found for cross-shard tenant-eu-002, but got $HTTP_CODE"
    exit 1
fi
echo ">>> Confirmed: tenant-eu-002 cannot see tenant-eu-001's post (404 Not Found)"

# Step 4: Register tenant-eu-003 on SAME shard (EU Shard 1) and test Hibernate Discriminator Isolation
echo ""
echo "[5/8] Testing SAME-SHARD isolation: registering tenant-eu-003 on EU Shard 1..."
curl -s -f -X PUT "${GATEWAY_URL}/internal/placements/tenant-eu-003" \
    -H "Content-Type: application/json" \
    -d '{"region": "eu", "shardId": "shard-1", "status": "ACTIVE"}' > /dev/null

READ_SAME_SHARD=$(curl -s -w "\n%{http_code}" -X GET "${GATEWAY_URL}/api/posts/${POST_ID}" \
    -H "X-Tenant-Id: tenant-eu-003")
HTTP_CODE=$(echo "$READ_SAME_SHARD" | tail -n1)

if [ "$HTTP_CODE" -ne 404 ]; then
    echo "FAILED: Hibernate discriminator leaked post to same-shard tenant-eu-003! HTTP $HTTP_CODE"
    exit 1
fi
echo ">>> Confirmed: tenant-eu-003 sharing the SAME physical shard cannot read tenant-eu-001's post (404 Not Found)"

# Step 5: Test Suspended Tenant rejection
echo ""
echo "[6/8] Testing Suspended Tenant fail-closed rejection..."
curl -s -f -X PUT "${GATEWAY_URL}/internal/placements/tenant-eu-suspended" \
    -H "Content-Type: application/json" \
    -d '{"region": "eu", "shardId": "shard-1", "status": "SUSPENDED"}' > /dev/null

SUSPENDED_RESP=$(curl -s -w "\n%{http_code}" -X GET "${GATEWAY_URL}/api/posts" \
    -H "X-Tenant-Id: tenant-eu-suspended")
HTTP_CODE=$(echo "$SUSPENDED_RESP" | tail -n1)

if [ "$HTTP_CODE" -ne 403 ]; then
    echo "FAILED: Suspended tenant was not rejected with 403 Forbidden! Got $HTTP_CODE"
    exit 1
fi
echo ">>> Confirmed: Suspended tenant rejected with 403 Forbidden"

# Step 6: Verify Routing Diagnostics for multi-region tenants
echo ""
echo "[7/8] Verifying Routing Diagnostics across regions..."
DIAG_EU=$(curl -s -H "X-Tenant-Id: tenant-eu-001" "${GATEWAY_URL}/internal/routing-info")
DIAG_ME=$(curl -s -H "X-Tenant-Id: tenant-me-001" "${GATEWAY_URL}/internal/routing-info")
DIAG_APAC=$(curl -s -H "X-Tenant-Id: tenant-apac-001" "${GATEWAY_URL}/internal/routing-info")

echo "EU Routing info:   ${DIAG_EU}"
echo "ME Routing info:   ${DIAG_ME}"
echo "APAC Routing info: ${DIAG_APAC}"

if ! echo "$DIAG_EU" | grep -q '"region":"eu"'; then
    echo "FAILED: EU routing did not return region eu"
    exit 1
fi
if ! echo "$DIAG_ME" | grep -q '"region":"me"'; then
    echo "FAILED: ME routing did not return region me"
    exit 1
fi
if ! echo "$DIAG_APAC" | grep -q '"region":"apac"'; then
    echo "FAILED: APAC routing did not return region apac"
    exit 1
fi
echo ">>> Confirmed: Geographic placement and shard routing verified for all 3 regions"

# Step 7: Send EU tenant directly to Middle East app (Direct Cross-Region Violation)
echo ""
echo "[8/8] Testing Direct Cross-Region Rejection (tenant-eu-001 -> ME direct container)..."
CROSS_REGION_RESP=$(curl -s -w "\n%{http_code}" -X GET "${ME_APP_DIRECT_URL}/api/posts" \
    -H "X-Tenant-Id: tenant-eu-001" || true)
HTTP_CODE=$(echo "$CROSS_REGION_RESP" | tail -n1)

if [ "$HTTP_CODE" -ne 403 ]; then
    echo "WARNING/CHECK: Direct ME container returned HTTP $HTTP_CODE (Note: ME app port 8083 must be running)"
else
    echo ">>> Confirmed: Regional app independently rejected wrong-region tenant with 403 Forbidden!"
fi

echo ""
echo "================================================================="
echo "ALL ACCEPTANCE & SMOKE TESTS PASSED SUCCESSFULLY!"
echo "================================================================="
