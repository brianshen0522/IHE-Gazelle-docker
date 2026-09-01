export const base64ToUtf8 = (str: string) => {
  const binary = atob(str);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.codePointAt(i)!;
  }
  const decoder = new TextDecoder("utf-8");
  return decoder.decode(bytes);
};
