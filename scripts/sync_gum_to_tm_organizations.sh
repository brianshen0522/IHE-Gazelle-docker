#!/usr/bin/env bash
#
# Copyright 2026 IHE International.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -euo pipefail

# Sync organizations from GUM to TM.
# Goal: ensure every organization present in GUM exists in TM.
# Behavior:
# - create missing TM organizations
# - update existing TM organizations (name)
# - never delete anything from TM
# Usage: sync_gum_to_tm_organizations.sh
# Requires DB_PASSWORD env var to connect to databases.

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing command: $1" >&2
    exit 1
  }
}

need_env() {
  local name="$1"
  [[ -n "${!name:-}" ]] || {
    echo "Missing env var: $name" >&2
    exit 1
  }
}

need_cmd psql

need_env DB_PASSWORD

DB_TM_HOST=localhost
DB_TM_PORT=5432
DB_TM_NAME=gazelle
DB_TM_USER=gazelle
DB_TM_PASSWORD=${DB_PASSWORD}

# GUM connection
DB_GUM_HOST=localhost
DB_GUM_PORT=5432
DB_GUM_NAME=gum
DB_GUM_USER=gazelle
DB_GUM_PASSWORD=${DB_PASSWORD}

tmp_file="$(mktemp)"
trap 'rm -f "$tmp_file"' EXIT

confirm_action() {
  local message="$1"
  local answer
  if [[ -t 0 ]]; then
    read -r -p "$message [y/N]: " answer
  else
    read -r -p "$message [y/N]: " answer < /dev/tty
  fi
  [[ "$answer" =~ ^[Yy]$ ]]
}

sql_escape() {
  printf "%s" "$1" | sed "s/'/''/g"
}

tm_select_org() {
  local keyword="$1"
  local kw_escaped
  kw_escaped="$(sql_escape "$keyword")"
  PGPASSWORD="$DB_TM_PASSWORD" psql \
    -h "$DB_TM_HOST" -p "$DB_TM_PORT" -U "$DB_TM_USER" -d "$DB_TM_NAME" \
    -v ON_ERROR_STOP=1 -X -t -A -F $'\t' -P footer=off \
    -c "SELECT COALESCE(name, ''), COALESCE(keyword, '') FROM usr_institution WHERE keyword = '$kw_escaped' LIMIT 1;" \
    | sed '/^[[:space:]]*$/d' | head -n 1
}

tm_insert_org() {
  local keyword="$1"
  local name="$2"
  local kw_escaped
  local nm_escaped
  local ur_escaped
  kw_escaped="$(sql_escape "$keyword")"
  nm_escaped="$(sql_escape "$name")"
  PGPASSWORD="$DB_TM_PASSWORD" psql \
    -h "$DB_TM_HOST" -p "$DB_TM_PORT" -U "$DB_TM_USER" -d "$DB_TM_NAME" \
    -v ON_ERROR_STOP=1 -X \
    -c "INSERT INTO usr_institution (id, keyword, name, institution_type_id) VALUES (nextval('usr_institution_id_seq'), '$kw_escaped', NULLIF('$nm_escaped', ''), 2);" \
    >/dev/null
}

tm_update_org() {
  local keyword="$1"
  local name="$2"
  local kw_escaped
  local nm_escaped
  local ur_escaped
  kw_escaped="$(sql_escape "$keyword")"
  nm_escaped="$(sql_escape "$name")"
  PGPASSWORD="$DB_TM_PASSWORD" psql \
    -h "$DB_TM_HOST" -p "$DB_TM_PORT" -U "$DB_TM_USER" -d "$DB_TM_NAME" \
    -v ON_ERROR_STOP=1 -X \
    -c "UPDATE usr_institution SET name = NULLIF('$nm_escaped', ''), WHERE keyword = '$kw_escaped';" \
    >/dev/null
}

 echo "[1/2] Export organizations from GUM..."
 PGPASSWORD="$DB_GUM_PASSWORD" psql \
   -h "$DB_GUM_HOST" -p "$DB_GUM_PORT" -U "$DB_GUM_USER" -d "$DB_GUM_NAME" \
   -v ON_ERROR_STOP=1 -X -t -A -F $'\t' -P footer=off \
   -c "SELECT id, shortname, name FROM gum.organization WHERE id IS NOT NULL AND id <> '';" \
   > "$tmp_file"

echo "[2/2] Sync organizations into TM with per-item confirmation..."
created=0
updated=0
unchanged=0
skipped_create=0
skipped_update=0

while IFS=$'\t' read -r id shortname name; do
  if [[ -z "${id:-}" ]]; then
    continue
  fi

  tm_row="$(tm_select_org "$shortname")"
  if [[ -z "$tm_row" ]]; then
    if confirm_action "Create TM organization (name='${name:-}', keyword='${shortname:-}')?"; then
      tm_insert_org "${shortname}" "${name}"
      created=$((created + 1))
    else
      skipped_create=$((skipped_create + 1))
    fi
    continue
  fi

  IFS=$'\t' read -r tm_name <<< "$tm_row"
  if [[ "$tm_name" == "${name:-}" ]]; then
    unchanged=$((unchanged + 1))
    continue
  fi

  if confirm_action "Update TM organization '$shortname' (name: '$tm_name' -> '${name:-}')?"; then
    tm_update_org "${shortname}" "${name:-}"
    updated=$((updated + 1))
  else
    skipped_update=$((skipped_update + 1))
  fi
done < "$tmp_file"

echo "Done. created=$created, updated=$updated, unchanged=$unchanged, skipped_create=$skipped_create, skipped_update=$skipped_update"

echo "Done: TM organizations are now aligned with GUM (create/update only)."
