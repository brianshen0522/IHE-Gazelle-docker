#!/bin/bash
# initdb stores the bootstrap password as SCRAM before the runtime
# `-c password_encryption=md5` flag matters; TM's Java 7 JDBC driver
# (postgresql-42.2.1.jre7) cannot do SCRAM (symptom: IJ000453 on a fresh
# volume). Re-hash the password now — this init script runs on the temp
# server which already has password_encryption=md5 active.
set -e
psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d postgres \
  -c "ALTER USER \"$POSTGRES_USER\" WITH PASSWORD '$POSTGRES_PASSWORD';"
