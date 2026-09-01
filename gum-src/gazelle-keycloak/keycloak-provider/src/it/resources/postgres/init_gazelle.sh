#!/bin/bash
set -e

cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")"

echo "Create gazelle database and import test data..."
psql -q --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    create database gazelle;
EOSQL

psql -q --username "$POSTGRES_USER" --dbname gazelle < ./sql/schema_gazelle.sql >/dev/null
psql -q --username "$POSTGRES_USER" --dbname gazelle < ./sql/init_gazelle_test_data.sql >/dev/null

set +e