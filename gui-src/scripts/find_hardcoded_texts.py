#!/usr/bin/env python3
"""
Script to find hardcoded English text in React/TypeScript files.
Helps identify strings that should be translated using i18n.

Usage:
    python3 scripts/find_hardcoded_texts.py [options]

Options:
    --path PATH          Base path to search (default: src/app)
    --output FILE        Output file for results (default: stdout)
    --json              Output in JSON format
    --min-length N      Minimum string length to report (default: 3)
    --exclude-pattern   Patterns to exclude (default: test files)
"""

import os
import re
import json
import argparse
from pathlib import Path
from typing import List, Dict, Tuple
from collections import defaultdict

class HardcodedTextFinder:
    def __init__(self, min_length=3, include_console=False):
        self.min_length = min_length
        self.include_console = include_console
        self.results = defaultdict(list)

        # Patterns to match hardcoded text
        self.patterns = [
            # JSX text content: >Text<
            (r'>\s*([A-Z][a-zA-Z\s,\.!?;:]{2,})\s*<', 'JSX content'),

            # String attributes: title="Text", placeholder="Text", etc.
            (r'(?:title|placeholder|label|alt|aria-label|message|text|description)\s*=\s*"([^"]{3,})"', 'Attribute'),

            # String attributes with single quotes
            (r"(?:title|placeholder|label|alt|aria-label|message|text|description)\s*=\s*'([^']{3,})'", 'Attribute'),

            # Template literals in attributes
            (r'(?:title|placeholder|label|alt|aria-label)\s*=\s*`([^`]{3,})`', 'Attribute (template)'),

            # Object properties: { label: "Text" }
            (r'(?:label|title|message|text|name|description):\s*"([^"]{3,})"', 'Object property'),
            (r"(?:label|title|message|text|name|description):\s*'([^']{3,})'", 'Object property'),

            # Button/Link text with common patterns
            (r'<(?:Button|button|Link|a)[^>]*>\s*([A-Z][a-zA-Z\s]{2,})\s*</(?:Button|button|Link|a)>', 'Button/Link text'),

            # Toast/Alert messages
            (r'toast\.(?:success|error|info|warning)\s*\(\s*"([^"]{3,})"', 'Toast message'),
            (r"toast\.(?:success|error|info|warning)\s*\(\s*'([^']{3,})'", 'Toast message'),
        ]

        # Console messages - excluded by default (debug messages don't need translation)
        # Use --include-console flag to include them
        if self.include_console:
            self.patterns.append(
                (r'console\.(?:log|error|warn|info)\s*\(\s*"([^"]{3,})"', 'Console message')
            )

        # Patterns to ignore (these are likely NOT hardcoded text needing translation)
        self.ignore_patterns = [
            r'^t\(',  # Already using translation
            r'className',  # CSS classes
            r'import\s+',  # Import statements
            r'from\s+',  # Import from
            r'\.tsx?$',  # File extensions
            r'\.css$',  # CSS files
            r'http[s]?://',  # URLs
            r'^\w+$',  # Single words without spaces (likely IDs or keys)
            r'^[a-z_]+$',  # snake_case (likely keys)
            r'^\d+',  # Starting with numbers
            r'^[A-Z_]+$',  # ALL_CAPS (likely constants)
            r'useTranslation',  # Hook usage
            r'gzl\.',  # Translation keys
            r'\.map\(',  # Array methods
            r'\.filter\(',
            r'\.find\(',
            r'metadata',  # Metadata objects (can't be translated in Next.js)
            r'Metadata',
            r'export\s+const',  # Exported constants
            r'^\w+\s*$',  # Single word only (likely not user-facing)
        ]

    def should_ignore(self, text: str, line: str) -> bool:
        """Check if a text match should be ignored"""
        # Ignore short strings
        if len(text.strip()) < self.min_length:
            return True

        # Ignore if line already uses translation
        if 't(' in line or 'useTranslation' in line:
            return True

        # Check ignore patterns
        for pattern in self.ignore_patterns:
            if re.search(pattern, text) or re.search(pattern, line):
                return True

        # Ignore if it's just punctuation or numbers
        if re.match(r'^[\d\s\.,!?;:]+$', text):
            return True

        return False

    def extract_context(self, lines: List[str], line_num: int, context_lines: int = 2) -> str:
        """Extract context around the line"""
        start = max(0, line_num - context_lines)
        end = min(len(lines), line_num + context_lines + 1)
        context = lines[start:end]
        return '\n'.join(f"  {start + i + 1}: {line.rstrip()}" for i, line in enumerate(context))

    def find_in_file(self, file_path: Path) -> List[Dict]:
        """Find hardcoded text in a single file"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
                lines = content.split('\n')
        except Exception as e:
            print(f"Error reading {file_path}: {e}")
            return []

        findings = []

        for pattern, pattern_type in self.patterns:
            for match in re.finditer(pattern, content, re.MULTILINE):
                text = match.group(1).strip()

                # Find line number
                line_num = content[:match.start()].count('\n')
                line = lines[line_num]

                if not self.should_ignore(text, line):
                    findings.append({
                        'file': str(file_path),
                        'line': line_num + 1,
                        'type': pattern_type,
                        'text': text,
                        'line_content': line.strip(),
                        'context': self.extract_context(lines, line_num)
                    })

        return findings

    def scan_directory(self, base_path: Path, exclude_patterns: List[str] = None) -> Dict:
        """Scan directory for hardcoded text"""
        if exclude_patterns is None:
            exclude_patterns = ['__tests__', 'node_modules', '.next', 'coverage', 'test.tsx', 'test.ts']

        for root, dirs, files in os.walk(base_path):
            # Exclude directories
            dirs[:] = [d for d in dirs if not any(pattern in d for pattern in exclude_patterns)]

            for file in files:
                if file.endswith(('.tsx', '.ts', '.jsx', '.js')):
                    # Skip test files
                    if any(pattern in file for pattern in exclude_patterns):
                        continue

                    file_path = Path(root) / file
                    findings = self.find_in_file(file_path)

                    if findings:
                        rel_path = file_path.relative_to(base_path.parent.parent if 'src/app' in str(base_path) else base_path.parent)
                        self.results[str(rel_path)].extend(findings)

        return self.results

    def generate_report(self, output_format='text') -> str:
        """Generate a report of findings"""
        if output_format == 'json':
            return json.dumps(dict(self.results), indent=2)

        # Text format
        report = []
        report.append("=" * 80)
        report.append("HARDCODED TEXT FINDER - REPORT")
        report.append("=" * 80)
        report.append("")

        total_files = len(self.results)
        total_findings = sum(len(findings) for findings in self.results.values())

        report.append(f"Summary:")
        report.append(f"  Files with hardcoded text: {total_files}")
        report.append(f"  Total findings: {total_findings}")
        report.append("")
        report.append("=" * 80)
        report.append("")

        # Group by file
        for file_path in sorted(self.results.keys()):
            findings = self.results[file_path]
            report.append(f"\n📄 File: {file_path}")
            report.append(f"   Found: {len(findings)} hardcoded text(s)")
            report.append("-" * 80)

            for i, finding in enumerate(findings, 1):
                report.append(f"\n  [{i}] Line {finding['line']} - {finding['type']}")
                report.append(f"      Text: \"{finding['text']}\"")
                report.append(f"      Code: {finding['line_content'][:100]}")
                report.append("")

        report.append("\n" + "=" * 80)
        report.append("END OF REPORT")
        report.append("=" * 80)

        return '\n'.join(report)

    def generate_compact_report(self) -> str:
        """Generate a compact report with one line per finding"""
        report = []
        report.append("=" * 120)
        report.append("HARDCODED TEXT FINDER - COMPACT REPORT")
        report.append("=" * 120)
        report.append("")

        total_files = len(self.results)
        total_findings = sum(len(findings) for findings in self.results.values())

        report.append(f"Summary: {total_files} files, {total_findings} findings")
        report.append("")
        report.append("=" * 120)
        report.append("")

        # Group by file
        for file_path in sorted(self.results.keys()):
            findings = self.results[file_path]
            report.append(f"📄 {file_path} ({len(findings)} finding(s))")

            for finding in findings:
                # Format: Line XX | Type | "Text" | Code snippet
                text = finding['text'][:60] + '...' if len(finding['text']) > 60 else finding['text']
                code = finding['line_content'].strip()[:80] + '...' if len(finding['line_content']) > 80 else finding['line_content'].strip()
                report.append(f"   L{finding['line']:4d} | {finding['type']:20s} | \"{text}\" | {code}")

            report.append("-" * 120)

        report.append("\n" + "=" * 120)
        report.append(f"END OF REPORT - {total_files} files, {total_findings} findings")
        report.append("=" * 120)

        return '\n'.join(report)

    def generate_markdown_report(self) -> str:
        """Generate a markdown report"""
        report = []
        report.append("# Hardcoded Text Report\n")

        total_files = len(self.results)
        total_findings = sum(len(findings) for findings in self.results.values())

        report.append(f"## Summary\n")
        report.append(f"- **Files with hardcoded text**: {total_files}")
        report.append(f"- **Total findings**: {total_findings}")
        report.append(f"- **Scan date**: {self.get_timestamp()}\n")

        report.append("---\n")
        report.append("## Findings by File\n")

        for file_path in sorted(self.results.keys()):
            findings = self.results[file_path]
            report.append(f"### 📄 `{file_path}`\n")
            report.append(f"**Found:** {len(findings)} hardcoded text(s)\n")

            for i, finding in enumerate(findings, 1):
                report.append(f"#### {i}. Line {finding['line']} - {finding['type']}\n")
                report.append(f"**Text:** `\"{finding['text']}\"`\n")
                report.append(f"**Code:**")
                report.append(f"```typescript")
                report.append(finding['line_content'])
                report.append(f"```\n")

        return '\n'.join(report)

    @staticmethod
    def get_timestamp():
        from datetime import datetime
        return datetime.now().strftime("%Y-%m-%d %H:%M:%S")


def main():
    parser = argparse.ArgumentParser(
        description='Find hardcoded English text in React/TypeScript files'
    )
    parser.add_argument(
        '--path',
        default='src/app',
        help='Base path to search (default: src/app)'
    )
    parser.add_argument(
        '--output',
        help='Output file for results'
    )
    parser.add_argument(
        '--format',
        choices=['text', 'json', 'markdown', 'compact'],
        default='compact',
        help='Output format: text (detailed), compact (one line per finding), json, markdown (default: compact)'
    )
    parser.add_argument(
        '--min-length',
        type=int,
        default=3,
        help='Minimum string length to report (default: 3)'
    )
    parser.add_argument(
        '--exclude',
        nargs='+',
        default=['__tests__', 'test.tsx', 'test.ts'],
        help='Patterns to exclude'
    )
    parser.add_argument(
        '--include-console',
        action='store_true',
        help='Include console.log messages in search (excluded by default)'
    )

    args = parser.parse_args()

    # Get base path
    base_path = Path(args.path)
    if not base_path.exists():
        print(f"Error: Path {base_path} does not exist")
        return 1

    print(f"🔍 Scanning for hardcoded text in: {base_path}")
    print(f"   Format: {args.format}")
    print(f"   Min length: {args.min_length}")
    print(f"   Excluding: {', '.join(args.exclude)}")
    print(f"   Include console: {args.include_console}")
    print("")

    # Create finder and scan
    finder = HardcodedTextFinder(min_length=args.min_length, include_console=args.include_console)
    finder.scan_directory(base_path, exclude_patterns=args.exclude)

    # Generate report
    if args.format == 'markdown':
        report = finder.generate_markdown_report()
    elif args.format == 'compact':
        report = finder.generate_compact_report()
    else:
        report = finder.generate_report(output_format=args.format)

    # Output results
    if args.output:
        with open(args.output, 'w', encoding='utf-8') as f:
            f.write(report)
        print(f"✅ Report saved to: {args.output}")
    else:
        print(report)

    return 0


if __name__ == '__main__':
    exit(main())

