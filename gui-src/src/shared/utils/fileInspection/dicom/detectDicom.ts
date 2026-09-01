export function detectBinaryDICOM(base64: string): boolean {
  try {
    // DICOM files have "DICM" magic number at byte 128-131
    // Decode first ~200 bytes to check for DICM marker
    const buffer = Buffer.from(base64.slice(0, 300), "base64");

    // Standard DICOM: check for "DICM" at offset 128
    if (buffer.length > 132) {
      const dicmMarker = buffer.toString("ascii", 128, 132);
      if (dicmMarker === "DICM") {
        return true;
      }
    }

    // Some DICOM files may not have the preamble
    // Check if it starts with common DICOM group/element patterns
    if (buffer.length >= 8) {
      // Look for common DICOM tag patterns (e.g., 0002,0000 or 0008,0000)
      const firstBytes = buffer.subarray(0, 8);
      // Check for little-endian group numbers like 0x0002 or 0x0008
      if ((firstBytes[0] === 0x02 || firstBytes[0] === 0x08) && firstBytes[1] === 0x00) {
        return true;
      }
    }

    return false;
  } catch {
    return false;
  }
}

export function detectTextDICOM(decoded: string): boolean {
  // DICOM text dump format has lines starting with numbers followed by colon
  // Example: "0: (0002,0000) UL #4 [188] Group Length"
  const lines = decoded.trim().split("\n");
  if (lines.length < 2) return false;

  // Check if multiple lines match the DICOM text dump pattern
  let matchCount = 0;
  for (let i = 0; i < Math.min(10, lines.length); i++) {
    if (/^\d+:\s*\([\dA-Fa-f]{4},[\dA-Fa-f]{4}\)/.test(lines[i])) {
      matchCount++;
    }
  }

  // If at least 2 lines match the pattern, it's likely a DICOM text dump
  return matchCount >= 2;
}
