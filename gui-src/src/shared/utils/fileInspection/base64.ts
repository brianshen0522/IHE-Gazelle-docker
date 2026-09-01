export function base64Encode(text: string): string {
  // Convert string to UTF-8 bytes
  const utf8Bytes = new TextEncoder().encode(text);

  // Convert bytes to binary string
  let binaryString = "";
  for (const element of utf8Bytes) {
    binaryString += String.fromCodePoint(element);
  }

  return btoa(binaryString);
}

export function base64Decode(base64: string, maxDecodedLength = 4096): string | null {
  try {
    const decoded = atob(base64);
    // Return a sample for detection if content is large
    return decoded.length > maxDecodedLength ? decoded.slice(0, maxDecodedLength) : decoded;
  } catch {
    return null;
  }
}

export function base64DecodeComplete(base64: string): string | null {
  try {
    return atob(base64);
  } catch {
    return null;
  }
}
