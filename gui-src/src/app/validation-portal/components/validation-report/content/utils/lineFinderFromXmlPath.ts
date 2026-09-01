export function findLineNumberFromXmlPath(xmlPath: string, content: string): number {
  try {
    const xmlContent = safelyDecodeBase64(content);
    const lines = normalizeAndSplit(xmlContent);

    const targetElement = extractTargetElement(xmlPath);
    if (!targetElement) return 0;

    const matchers = buildOrderedMatchers(xmlPath, targetElement);

    return scanLines(lines, matchers);
  } catch (error) {
    console.error("Error finding XML line number:", error);
    return 0;
  }
}

function scanLines(lines: string[], matchers: RegExp[]): number {
  for (let i = 0; i < lines.length; i++) {
    if (matchesAny(lines[i], matchers)) {
      return i + 1;
    }
  }
  return 0;
}

function matchesAny(line: string, matchers: RegExp[]): boolean {
  return matchers.some((regex) => regex.test(line));
}

// XPath extraction
function extractTargetElement(xmlPath: string): string | null {
  const simplified = simplifyXPath(xmlPath);
  const segments = simplified.split("/").filter(Boolean);
  return segments.at(-1) ?? null;
}

function simplifyXPath(xmlPath: string): string {
  return xmlPath
    .replaceAll(/\[\d+\]/g, "")
    .replaceAll(/\[@[^=]+=(['"]).*?\1\]/g, "")
    .replaceAll(/\/@\w+$/g, "")
    .replaceAll(/^\/+/g, "")
    .replaceAll(/\/+/g, "/");
}

// Matcher builder
function buildOrderedMatchers(xmlPath: string, targetElement: string): RegExp[] {
  const matchers: RegExp[] = [];

  const escapedTarget = escapeRegex(targetElement);

  const attrValueRegex = buildAttrValueRegex(xmlPath, escapedTarget);
  const attrRegex = buildAttrRegex(xmlPath, escapedTarget);
  const openTagRegex = buildOpenTagRegex(escapedTarget);

  if (attrValueRegex) matchers.push(attrValueRegex);
  if (attrRegex) matchers.push(attrRegex);

  matchers.push(openTagRegex);

  if (xmlPath.startsWith("//")) {
    matchers.push(new RegExp(String.raw`<${escapedTarget}[\s>]`, "i"));
  }

  return matchers;
}

function buildOpenTagRegex(target: string): RegExp {
  return new RegExp(String.raw`<${target}(?:\s|>|/>)`, "i");
}

function buildAttrRegex(xmlPath: string, target: string): RegExp | null {
  const match = /@(\w+)$/.exec(xmlPath);
  return match ? new RegExp(String.raw`<${target}[^>]*\s${match[1]}=`, "i") : null;
}

function buildAttrValueRegex(xmlPath: string, target: string): RegExp | null {
  const match = /\[@(\w+)=(['"])(.+?)\2\]/.exec(xmlPath);
  return match ? new RegExp(String.raw`<${target}[^>]*\s${match[1]}=${match[2]}${match[3]}${match[2]}`, "i") : null;
}

// Utilities
function escapeRegex(str: string): string {
  return str.replaceAll(/[.*+?^${}()|[\]\\]/g, String.raw`\$&`);
}

function safelyDecodeBase64(content: string): string {
  try {
    return atob(content);
  } catch {
    return content;
  }
}

function normalizeAndSplit(xml: string): string[] {
  return xml.replaceAll("\r\n", "\n").split("\n");
}
