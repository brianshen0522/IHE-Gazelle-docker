#!/usr/bin/env bash
# Removed -e to avoid aborting; keep -u for undefined var protection
set -u
trap 'echo "WARN: command failed at line $LINENO (continuing)" >&2' ERR

# Minimal migration/import script: fetch profiles from DB, copy files, build index.json

usage() { echo "Usage: $0 --src <schematron_root> --dest <xml_validation_root> --indexPath <index.json> [--tabProfilesPath <TABProfilesConfiguration.json>] --db <db_name> --host <db_host> --port <db_port> --user <db_user> --password <db_password>" >&2; }

SCHEMATRONVALPATH=""
INDEXPATH=""
XMLVALPATH=""
TABPROFILES_PATH=""
DBname=""
DBhost=""
DBport=""
DBuser=""
DBpwd=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --src) SCHEMATRONVALPATH="$2"; shift 2;;
    --dest) XMLVALPATH="$2"; shift 2;;
    --indexPath) INDEXPATH="$2"; shift 2;;
    --tabProfilesPath) TABPROFILES_PATH="$2"; shift 2;;
    --db) DBname="$2"; shift 2;;
    --host) DBhost="$2"; shift 2;;
    --port) DBport="$2"; shift 2;;
    --user) DBuser="$2"; shift 2;;
    --password) DBpwd="$2"; shift 2;;
    --help|-h) usage; echo "WARN: Help requested - skipping migration"; exit 0;;
    *) echo "WARN: Unknown option: $1 - skipping migration" >&2; exit 0;;
  esac
done

for v in SCHEMATRONVALPATH XMLVALPATH INDEXPATH DBname DBhost DBport DBuser DBpwd; do
  if [ -z "${!v}" ]; then
    echo "WARN: Missing required option: $v - skipping migration" >&2
    usage
    exit 0
  fi
done

export PGPASSWORD="$DBpwd"

# Validate DB exists (skip if not reachable)
if ! psql -h "$DBhost" -p "$DBport" -U "$DBuser" -d postgres -Atqc "SELECT 1 FROM pg_database WHERE datname='${DBname}'" | grep -qx "1"; then
  echo "WARN: Database '$DBname' not found or not accessible - skipping migration" >&2
  exit 0
fi

schematron_dir="$SCHEMATRONVALPATH/bin/schematron"
compilations_dir="$SCHEMATRONVALPATH/bin/compilations"

# If schematron directory missing, skip migration gracefully
if [ ! -d "$schematron_dir" ]; then
  echo "WARN: Missing directory $schematron_dir - skipping migration" >&2
  exit 0
fi

index_parent_dir="$(dirname "$INDEXPATH")"
if [[ ! -d "$index_parent_dir" || ! -w "$index_parent_dir" ]]; then
  echo "WARN: Index parent directory $index_parent_dir does not exist or is not writable" >&2
  exit 1
fi
if [ -z "${TABPROFILES_PATH:-}" ]; then
  TABPROFILES_PATH="${index_parent_dir}/TABProfilesConfiguration.json"
  echo "WARN: TABProfilesConfiguration path not provided; defaulting to $TABPROFILES_PATH" >&2
fi
tab_profiles_parent_dir="$(dirname "$TABPROFILES_PATH")"
if [[ ! -d "$tab_profiles_parent_dir" || ! -w "$tab_profiles_parent_dir" ]]; then
  echo "WARN: TABProfilesConfiguration parent directory $tab_profiles_parent_dir does not exist or is not writable" >&2
  exit 1
fi

# Prepare uncompleted index output next to main index
uncomp_enabled=true
uncomp_file="${index_parent_dir}/index_uncompeleted.json"
uncomp_tmp="${uncomp_file}.tmp"
first_uncomp=true
nbUncompleted=0
echo "[" > "$uncomp_tmp"
tab_profiles_file="$TABPROFILES_PATH"
tab_profiles_tmp="${tab_profiles_file}.tmp"
first_tab_profile=true
nbTabProfiles=0
echo "[" > "$tab_profiles_tmp"

# Copy XSD directories (fail on errors)
if [ -d "$SCHEMATRONVALPATH/xsd" ]; then
  mkdir -p "$XMLVALPATH/xsd"
  cp -r "$SCHEMATRONVALPATH/xsd/." "$XMLVALPATH/xsd/"
fi
if [ -d "$SCHEMATRONVALPATH/bin/xsd" ]; then
  mkdir -p "$XMLVALPATH/xsd"
  cp -r "$SCHEMATRONVALPATH/bin/xsd/." "$XMLVALPATH/xsd/"
fi

# Fetch profiles
profiles=$(psql -h "$DBhost" -p "$DBport" -U "$DBuser" -d "$DBname" -AtF '|' \
  -c "SELECT s.id, s.name, ot.keyword, s.path, s.xsd_path, s.version, s.xsd_version, s.available, s.dfdl_schema_keyword, s.dfdl_transformation_needed \
       FROM sch_validator_schematron s \
       LEFT JOIN sch_validator_object_type ot ON s.object_type_id = ot.id \
       ORDER BY s.id")

nbProfiles=0
index_file="$INDEXPATH"
# Write to a temp file for atomic completeness
tmp_file="${index_file}.tmp"
# Reset file with opening bracket
echo "[" > "$tmp_file"
nbProfiles=0
first=true
# prepare pretty printing: write prior object with trailing comma
prev_obj=""
COPIED_TOP_DIRS=""

# Build JSON object (pretty formatted)
escape() { printf '%s' "$1" | sed -e 's/\\/\\\\/g' -e 's/"/\\"/g'; }

# Render a profile JSON object to stdout
# Args: name keyword schematronVersion xsdVersion xsd_rel sch_rel xslt_rel available_json
render_profile() {
  local name="$1" keyword="$2" schematron_version="$3" xsd_version="$4" xsd_rel="$5" sch_rel="$6" xslt_rel="$7" available_json="$8"
  local name_esc keyword_esc schematron_version_esc xsd_version_esc
  name_esc="$(escape "$name")"
  keyword_esc="$(escape "$keyword")"
  schematron_version_esc="$(escape "$schematron_version")"
  xsd_version_esc="$(escape "$xsd_version")"
  cat <<EOF
  {
    "profileID": "$name_esc",
    "profileName": "$name_esc",
    "domain": "$keyword_esc",
    "standards": [
      "$keyword_esc"
    ],
    "xsdPath": "$xsd_rel",
    "schematronPath": "$sch_rel",
    "xsltPath": "$xslt_rel",
    "schematronVersion": "$schematron_version_esc",
    "xsdVersion": "$xsd_version_esc",
    "available": $available_json,
    "cacheEnabled": false
  }
EOF
}

# Append one profile object to the temporary index file
# Args: name keyword schematronVersion xsdVersion xsd_rel sch_rel xslt_rel available_json
write_profile() {
  local name="$1" keyword="$2" schematron_version="$3" xsd_version="$4" xsd_rel="$5" sch_rel="$6" xslt_rel="$7" available_json="$8"
  if [ "$first" = false ]; then
    echo "," >> "$tmp_file"
  fi
  first=false
  render_profile "$name" "$keyword" "$schematron_version" "$xsd_version" "$xsd_rel" "$sch_rel" "$xslt_rel" "$available_json" >> "$tmp_file"
  nbProfiles=$((nbProfiles+1))
}

# Append one uncompleted profile object to the uncompleted index
# Args: name keyword schematronVersion xsdVersion xsd_rel sch_rel xslt_rel available_json
write_uncompleted_profile() {
  local name="$1" keyword="$2" schematron_version="$3" xsd_version="$4" xsd_rel="$5" sch_rel="$6" xslt_rel="$7" available_json="$8"
  if [ "$first_uncomp" = false ]; then
    echo "," >> "$uncomp_tmp"
  fi
  first_uncomp=false
  render_profile "$name" "$keyword" "$schematron_version" "$xsd_version" "$xsd_rel" "$sch_rel" "$xslt_rel" "$available_json" >> "$uncomp_tmp"
  nbUncompleted=$((nbUncompleted+1))
}

# Append a TAB profile entry when the source requires DFDL transformation
write_tab_profile() {
  local profile_name="$1" keyword="$2" schematron_version="$3" transformation_schema="$4"
  if [ "$first_tab_profile" = false ]; then
    echo "," >> "$tab_profiles_tmp"
  fi
  first_tab_profile=false
  local profile_esc keyword_esc schematron_version_esc transformation_esc validationProfileId_esc
  profile_esc="$(escape "${profile_name}__DFDL__${transformation_schema}")"
  keyword_esc="$(escape "$keyword")"
  schematron_version_esc="$(escape "$schematron_version")"
  validationProfileId_esc="$(escape "$profile_name")"
  transformation_esc="$(escape "$transformation_schema")"
  cat <<EOF >> "$tab_profiles_tmp"
  {
    "profileId": "$profile_esc",
    "profileName": "$profile_esc",
    "domain": "$keyword_esc",
    "standards": [
      "$keyword_esc"
    ],
    "schematronVersion": "$schematron_version_esc",
    "transformationSchemaId": "$transformation_esc",
    "xmlValidationProfileId": "$validationProfileId_esc"
  }
EOF
  nbTabProfiles=$((nbTabProfiles+1))
}

# Copy include and _pre_compilation directories along the ancestor chain
copy_aux_ancestors() {
  local rel_path="$1"
  local dir
  dir="$(dirname "$rel_path")"
  # Walk up until empty
  while :; do
    for d in include _pre_compilation; do
      if [ -n "$dir" ]; then
        src_d="$schematron_dir/$dir/$d"
        dest_d="$XMLVALPATH/profiles/$dir/$d"
      else
        src_d="$schematron_dir/$d"
        dest_d="$XMLVALPATH/profiles/$d"
      fi
      if [ -d "$src_d" ]; then
        mkdir -p "$dest_d"
        cp -rn "$src_d/." "$dest_d/"
      fi
    done
    [ -z "$dir" ] && break
    # Go up
    dir="$(dirname "$dir")"
    [ "$dir" = "." ] && dir=""
  done
}

while IFS='|' read -r id name keyword path xsd_path schematron_version xsd_version available dfdl_schema_keyword dfdl_transformation_needed; do
  [ -z "${id:-}" ] && continue
  path=$(echo "$path" | xargs)
  path="${path#/}"          # guard against leading slash turning relative paths into absolute
  xsd_path=$(echo "$xsd_path" | xargs)
  xsd_path="${xsd_path#/}"
  schematron_version=$(echo "${schematron_version:-}" | xargs)
  xsd_version=$(echo "${xsd_version:-}" | xargs)
  dfdl_schema_keyword=$(echo "${dfdl_schema_keyword:-}" | xargs)
  dfdl_transformation_needed=$(echo "${dfdl_transformation_needed:-}" | xargs)
  # Normalize available to JSON boolean
  case "${available,,}" in
    t|true|1|yes|y) available_json=true ;;
    *) available_json=false ;;
  esac
  case "${dfdl_transformation_needed,,}" in
    t|true|1|yes|y)
      if [ -n "$dfdl_schema_keyword" ]; then
        write_tab_profile "$name" "$keyword" "$schematron_version" "$dfdl_schema_keyword"
      fi
      ;;
  esac
  # If schematron path from DB is empty/null, still add profile
  # with schematronPath empty and xsltPath empty as well
  if [ -z "${path:-}" ]; then
    xsd_rel="xsd/$xsd_path"
    sch_rel=""
    xslt_rel=""
    if [ "$available_json" = true ]; then
      write_profile "$name" "$keyword" "$schematron_version" "$xsd_version" "$xsd_rel" "$sch_rel" "$xslt_rel" "$available_json"
    else
      write_uncompleted_profile "$name" "$keyword" "$schematron_version" "$xsd_version" "$xsd_rel" "$sch_rel" "$xslt_rel" "$available_json"
    fi
    continue
  fi
  # If profile not available, record in uncompleted and skip copying
  if [ "$available_json" = false ]; then
    xsd_rel="xsd/$xsd_path"
    sch_rel="profiles/$path"
    xslt_rel="profiles/${path%.*}.xsl"
    write_uncompleted_profile "$name" "$keyword" "$schematron_version" "$xsd_version" "$xsd_rel" "$sch_rel" "$xslt_rel" "$available_json"
    continue
  fi
  # No full top-level copy to reduce IO; we will copy needed aux dirs along the ancestor chain
  src_sch="$schematron_dir/$path"
  dest_sch="$XMLVALPATH/profiles/$path"
  mkdir -p "$(dirname "$dest_sch")"
  if [ ! -f "$src_sch" ]; then
    echo "Warn: missing Schematron file $src_sch; skipping profile name $name"
    # Record uncompleted profile
    xsd_rel="xsd/$xsd_path"
    sch_rel="profiles/$path"
    xslt_rel="profiles/${path%.*}.xsl"
    write_uncompleted_profile "$name" "$keyword" "$schematron_version" "$xsd_version" "$xsd_rel" "$sch_rel" "$xslt_rel" "$available_json"
    continue
  fi
  # Copy the entire parent directory contents (includes _pre_compilation and any sibling folders/files)
  parent_dir="$(dirname "$path")"
  if [ "$parent_dir" = "." ]; then
    parent_src="$schematron_dir"
    parent_dest="$XMLVALPATH/profiles"
  else
    parent_src="$schematron_dir/$parent_dir"
    parent_dest="$XMLVALPATH/profiles/$parent_dir"
  fi
  if [ -d "$parent_src" ]; then
    mkdir -p "$parent_dest"
    cp -rn "$parent_src/." "$parent_dest/"
  fi
  cp -f "$src_sch" "$dest_sch"
  # Copy sibling helper files (e.g., _*.sch) that some schematrons reference directly
  src_dir="$(dirname "$src_sch")"
  dest_dir="$(dirname "$dest_sch")"
  if [ -d "$src_dir" ]; then
    while IFS= read -r -d '' helper; do
      cp -f "$helper" "$dest_dir/"
    done < <(find "$src_dir" -maxdepth 1 -type f -name '_*' -print0)
  fi
  # Copy required auxiliary directories from ancestor chain (include, _pre_compilation)
  copy_aux_ancestors "$path"
  sch_rel="profiles/$path"
  xsd_rel="xsd/$xsd_path"
  xslt_rel="profiles/${path%.*}.xsl"
  write_profile "$name" "$keyword" "$schematron_version" "$xsd_version" "$xsd_rel" "$sch_rel" "$xslt_rel" "$available_json"
done <<< "$profiles"
echo "]" >> "$tmp_file"
# Optional: reformat with jq if available for consistent pretty style
if command -v jq >/dev/null 2>&1; then
  raw_tmp="${tmp_file}.raw"
  mv "$tmp_file" "$raw_tmp"
  jq '.' "$raw_tmp" > "$tmp_file" 2>/dev/null || mv "$raw_tmp" "$tmp_file"
  rm -f "$raw_tmp"
fi
mv "$tmp_file" "$index_file"
echo "Index written to $index_file"
echo "Migration done - profiles added : $nbProfiles"

# Finalize optional uncompleted index
echo "]" >> "$uncomp_tmp"
if command -v jq >/dev/null 2>&1; then
  raw_tmp="${uncomp_tmp}.raw"
  mv "$uncomp_tmp" "$raw_tmp"
  jq '.' "$raw_tmp" > "$uncomp_tmp" 2>/dev/null || mv "$raw_tmp" "$uncomp_tmp"
  rm -f "$raw_tmp"
fi
mv "$uncomp_tmp" "$uncomp_file"
echo "Uncompleted index written to $uncomp_file (count=$nbUncompleted)"
echo "]" >> "$tab_profiles_tmp"
if command -v jq >/dev/null 2>&1; then
  raw_tmp="${tab_profiles_tmp}.raw"
  mv "$tab_profiles_tmp" "$raw_tmp"
  jq '.' "$raw_tmp" > "$tab_profiles_tmp" 2>/dev/null || mv "$raw_tmp" "$tab_profiles_tmp"
  rm -f "$raw_tmp"
fi
mv "$tab_profiles_tmp" "$tab_profiles_file"
echo "TAB Profiles configuration written to $tab_profiles_file (count=$nbTabProfiles)"
