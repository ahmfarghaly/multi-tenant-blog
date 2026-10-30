#!/usr/bin/env bash
set -euo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"

echo "================================================================="
echo "Seeding Demo Blog Posts via Gateway: ${GATEWAY_URL}"
echo "================================================================="

seed_post() {
    local tenant_id="$1"
    local title="$2"
    local body="$3"

    echo "Creating post for [${tenant_id}]..."
    curl -s -f -X POST "${GATEWAY_URL}/api/posts" \
        -H "Content-Type: application/json" \
        -H "X-Tenant-Id: ${tenant_id}" \
        -d "{\"title\": \"${title}\", \"body\": \"${body}\"}" \
        | grep -o '"id":"[^"]*"' || true
    echo ""
}

# EU Tenants
seed_post "tenant-eu-001" "Welcome to EU Blog 1" "This post is stored in EU Shard 1 located in Frankfurt."
seed_post "tenant-eu-002" "Insights from EU Blog 2" "This post is stored in EU Shard 2 located in Frankfurt."

# Middle East Tenants
seed_post "tenant-me-001" "Middle East Tech Trends" "This post is stored in ME Shard 1 located in Dubai."
seed_post "tenant-me-002" "Cloud Innovations in the Gulf" "This post is stored in ME Shard 2 located in Dubai."

# APAC Tenants
seed_post "tenant-apac-001" "APAC Regional Growth" "This post is stored in APAC Shard 1 located in Singapore."
seed_post "tenant-apac-002" "Software Architecture in Asia" "This post is stored in APAC Shard 2 located in Singapore."

echo "================================================================="
echo "Demo seeding completed successfully!"
echo "================================================================="
