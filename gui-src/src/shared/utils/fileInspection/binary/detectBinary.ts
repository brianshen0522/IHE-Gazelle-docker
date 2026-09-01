function decodeBase64ToUint8Array(base64: string): Uint8Array {
  try {
    const binaryString = atob(base64);
    const bytes = new Uint8Array(binaryString.length);
    for (let i = 0; i < binaryString.length; i++) {
      bytes[i] = binaryString.codePointAt(i)!;
    }
    return bytes;
  } catch {
    return new Uint8Array(0);
  }
}

function isBinaryBytes(bytes: Uint8Array): boolean {
  if (bytes.length === 0) return false;

  const length = Math.min(bytes.length, 512);
  let nonPrintable = 0;

  for (let i = 0; i < length; i++) {
    const byte = bytes[i];
    if (byte === 0) return true;
    const printable = byte === 9 || byte === 10 || byte === 13 || (byte >= 32 && byte <= 126);
    if (!printable) nonPrintable++;
  }

  return nonPrintable / length > 0.3;
}

export function isBinaryEncodedString(base64: string): boolean {
  const bytes = decodeBase64ToUint8Array(base64);
  return isBinaryBytes(bytes);
}
