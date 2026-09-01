# Scripts

In this directory, you will find various scripts helpful for maintaining user-interface project.

## Find Unused Keys

This script is used to find unused keys in the sources.json. Avoiding unused keys helps to keep the translation files clean and maintainable.

The script will output a report of unused keys in a specified format.

**Prerequisites:**
- Python 3.x installed on your system.
- The `sources.json` file should be present in the project directory.
- The script need to be run from the root directory of the project.

```bash
python3 scripts/find_unused_keys.py --format compact --output unused_keys_report.txt 2>&1
```

## Find hardcoded strings

This script is used to find hardcoded strings in the source code. Hardcoded strings can be problematic for internationalization (i18n) because they are not easily translatable. 

The script will output a report of hardcoded strings in a specified format.

**Prerequisites:**
- Python 3.x installed on your system.
- The `sources.json` file should be present in the project directory.
- The script need to be run from the root directory of the project.

```bash
python3 scripts/find_hardcoded_texts.py --format compact --min-length 4 --output hardcoded_compact.txt
```