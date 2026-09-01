#!/usr/bin/env python3
"""
Script to find unused translation keys in sources.json

This script:
1. Extracts all translation keys from sources.json
2. Scans all source files for usage of t('key')
3. Reports unused keys that can be safely removed

Usage:
    python3 scripts/find_unused_keys.py [options]

Options:
    --sources FILE       Path to sources.json (default: locales/sources.json)
    --scan-path PATH     Base path to scan for usage (default: src)
    --output FILE        Output file for results (default: stdout)
    --format FORMAT      Output format: text, json, compact (default: compact)
    --show-used          Also show used keys (default: only unused)
"""

import json
import re
import argparse
from pathlib import Path
from typing import Set, Dict, List
from collections import defaultdict


class TranslationKeyAnalyzer:
    def __init__(self, sources_file: Path, scan_path: Path):
        self.sources_file = sources_file
        self.scan_path = scan_path
        self.all_keys: Set[str] = set()
        self.used_keys: Set[str] = set()
        self.key_locations: Dict[str, List[str]] = defaultdict(list)

    def extract_keys_from_json(self, obj: dict, prefix: str = "") -> Set[str]:
        """Recursively extract all keys from nested JSON structure"""
        keys = set()

        for key, value in obj.items():
            current_key = f"{prefix}.{key}" if prefix else key

            if isinstance(value, dict):
                # Recurse into nested objects
                keys.update(self.extract_keys_from_json(value, current_key))
            else:
                # Leaf node - this is a translation key
                keys.add(current_key)

        return keys

    def load_translation_keys(self) -> Set[str]:
        """Load all translation keys from sources.json"""
        try:
            with open(self.sources_file, 'r', encoding='utf-8') as f:
                data = json.load(f)

            self.all_keys = self.extract_keys_from_json(data)
            print(f"✓ Loaded {len(self.all_keys)} translation keys from {self.sources_file}")
            return self.all_keys
        except Exception as e:
            print(f"✗ Error loading {self.sources_file}: {e}")
            return set()

    def find_key_usage_in_file(self, file_path: Path) -> Set[str]:
        """Find translation key usage in a single file"""
        found_keys = set()

        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
        except Exception as e:
            # Skip files that can't be read
            return found_keys

        # Patterns to match t('key') or t("key")
        patterns = [
            r't\(["\']([^"\']+)["\'][\)\,]',  # t('key') or t("key")
            r't\(`([^`]+)`\)',  # t(`key`)
            r'useTranslations?\(["\']([^"\']+)["\']\)',  # useTranslation('namespace')
        ]

        for pattern in patterns:
            matches = re.findall(pattern, content)
            for match in matches:
                # Handle interpolation - extract base key
                base_key = match.split(',')[0].strip()
                found_keys.add(base_key)
                self.key_locations[base_key].append(str(file_path))

        return found_keys

    def scan_for_usage(self) -> Set[str]:
        """Scan all source files for translation key usage"""
        used_keys = set()
        file_count = 0

        # File extensions to scan
        extensions = ['.tsx', '.ts', '.jsx', '.js']

        # Directories to exclude
        exclude_dirs = ['node_modules', '.next', 'coverage', 'dist', 'build']

        for root, dirs, files in self.scan_path.walk():
            # Exclude directories
            dirs[:] = [d for d in dirs if d not in exclude_dirs]

            for file in files:
                if any(file.endswith(ext) for ext in extensions):
                    file_path = root / file
                    file_keys = self.find_key_usage_in_file(file_path)
                    used_keys.update(file_keys)
                    file_count += 1

        self.used_keys = used_keys
        print(f"✓ Scanned {file_count} files")
        print(f"✓ Found {len(used_keys)} unique keys in use")

        return used_keys

    def find_unused_keys(self) -> Set[str]:
        """Find keys that are defined but never used"""
        unused = self.all_keys - self.used_keys
        print(f"\n{'='*80}")
        print(f"Analysis: {len(unused)} unused keys found")
        print(f"  Total keys: {len(self.all_keys)}")
        print(f"  Used keys: {len(self.used_keys)}")
        print(f"  Unused keys: {len(unused)}")
        print(f"  Usage rate: {(len(self.used_keys)/len(self.all_keys)*100):.1f}%")
        print(f"{'='*80}\n")

        return unused


    def generate_report(self, format_type='compact', show_used=False) -> str:
        """Generate report of unused keys"""
        unused_keys = self.find_unused_keys()

        if format_type == 'json':
            return json.dumps({
                'total_keys': len(self.all_keys),
                'used_keys': len(self.used_keys),
                'unused_keys': sorted(list(unused_keys)),
                'usage_rate': f"{(len(self.used_keys)/len(self.all_keys)*100):.1f}%"
            }, indent=2)

        # Text/Compact format
        report = []
        report.append("=" * 120)
        report.append("UNUSED TRANSLATION KEYS REPORT")
        report.append("=" * 120)
        report.append("")
        report.append(f"Summary:")
        report.append(f"  Total keys in sources.json: {len(self.all_keys)}")
        report.append(f"  Keys in use: {len(self.used_keys)} ({(len(self.used_keys)/len(self.all_keys)*100):.1f}%)")
        report.append(f"  Unused keys: {len(unused_keys)} ({(len(unused_keys)/len(self.all_keys)*100):.1f}%)")

        report.append("")
        report.append("=" * 120)

        # Unused keys section
        if unused_keys:
            report.append("\n🗑️  UNUSED KEYS (Safe to remove)")
            report.append("-" * 120)
            report.append("")

            # Group by namespace
            grouped = defaultdict(list)
            for key in sorted(unused_keys):
                namespace = '.'.join(key.split('.')[:2]) if '.' in key else 'other'
                grouped[namespace].append(key)

            for namespace in sorted(grouped.keys()):
                keys_in_namespace = grouped[namespace]
                report.append(f"\n📦 {namespace} ({len(keys_in_namespace)} unused keys)")
                for key in keys_in_namespace:
                    if format_type == 'compact':
                        report.append(f"   - {key}")
                    else:
                        report.append(f"   - {key}")
                        report.append(f"     (Never referenced in source code)")
        else:
            report.append("\n✅ No unused keys found! All translation keys are being used.")

        report.append("\n" + "=" * 120)
        report.append("END OF REPORT")
        report.append("=" * 120)

        return '\n'.join(report)

    def generate_cleanup_script(self) -> str:
        """Generate a Python script to remove unused keys from sources.json"""
        unused_keys = self.find_unused_keys()

        script = [
            "#!/usr/bin/env python3",
            '"""Auto-generated script to remove unused translation keys"""',
            "import json",
            "",
            "# Keys to remove",
            f"KEYS_TO_REMOVE = {sorted(list(unused_keys))}",
            "",
            "def remove_key(obj, key_path):",
            '    """Remove a key from nested dict structure"""',
            "    parts = key_path.split('.')",
            "    current = obj",
            "    ",
            "    # Navigate to parent",
            "    for part in parts[:-1]:",
            "        if part not in current:",
            "            return False",
            "        current = current[part]",
            "    ",
            "    # Remove the final key",
            "    if parts[-1] in current:",
            "        del current[parts[-1]]",
            "        return True",
            "    return False",
            "",
            "# Load sources.json",
            "with open('locales/sources.json', 'r', encoding='utf-8') as f:",
            "    data = json.load(f)",
            "",
            "# Remove unused keys",
            "removed = 0",
            "for key in KEYS_TO_REMOVE:",
            "    if remove_key(data, key):",
            "        removed += 1",
            "        print(f'Removed: {key}')",
            "",
            "# Save back",
            "with open('locales/sources.json', 'w', encoding='utf-8') as f:",
            "    json.dump(data, f, indent=2, ensure_ascii=False)",
            "    f.write('\\n')",
            "",
            f"print(f'\\n✓ Removed {{removed}}/{len(KEYS_TO_REMOVE)} unused keys')",
        ]

        return '\n'.join(script)


def main():
    parser = argparse.ArgumentParser(
        description='Find unused translation keys in sources.json'
    )
    parser.add_argument(
        '--sources',
        default='locales/sources.json',
        help='Path to sources.json (default: locales/sources.json)'
    )
    parser.add_argument(
        '--scan-path',
        default='src',
        help='Base path to scan for usage (default: src)'
    )
    parser.add_argument(
        '--output',
        help='Output file for results'
    )
    parser.add_argument(
        '--format',
        choices=['text', 'json', 'compact'],
        default='compact',
        help='Output format (default: compact)'
    )
    parser.add_argument(
        '--show-used',
        action='store_true',
        help='Also show used keys in report'
    )
    parser.add_argument(
        '--generate-cleanup',
        help='Generate cleanup script to remove unused keys'
    )

    args = parser.parse_args()

    # Validate paths
    sources_path = Path(args.sources)
    if not sources_path.exists():
        print(f"✗ Error: {sources_path} not found")
        return 1

    scan_path = Path(args.scan_path)
    if not scan_path.exists():
        print(f"✗ Error: {scan_path} not found")
        return 1

    print(f"🔍 Analyzing translation keys...")
    print(f"   Sources: {sources_path}")
    print(f"   Scanning: {scan_path}")
    print("")

    # Create analyzer
    analyzer = TranslationKeyAnalyzer(sources_path, scan_path)

    # Load keys
    analyzer.load_translation_keys()

    # Scan for usage
    analyzer.scan_for_usage()

    # Generate report
    report = analyzer.generate_report(format_type=args.format, show_used=args.show_used)

    # Output results
    if args.output:
        with open(args.output, 'w', encoding='utf-8') as f:
            f.write(report)
        print(f"\n✅ Report saved to: {args.output}")
    else:
        print(report)

    # Generate cleanup script if requested
    if args.generate_cleanup:
        cleanup_script = analyzer.generate_cleanup_script()
        with open(args.generate_cleanup, 'w', encoding='utf-8') as f:
            f.write(cleanup_script)
        print(f"\n✅ Cleanup script generated: {args.generate_cleanup}")
        print(f"   Run with: python3 {args.generate_cleanup}")

    return 0


if __name__ == '__main__':
    exit(main())

