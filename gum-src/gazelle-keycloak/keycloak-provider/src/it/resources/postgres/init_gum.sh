#!/bin/bash
set -e

cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")"

echo "Import test data in gum database..."
PGPASSWORD=${DB_GUM_PASSWORD} psql -h ${DB_GUM_HOST} -U "${DB_GUM_USER}" -p "${DB_GUM_PORT}" -d "${DB_GUM_NAME}" < ./sql/init_gum_test_data.sql >/dev/null

set +e