import { base64ToUtf8 } from "@/app/message-capture/utils/base64ToUtf8";

export function processSyslogData(base64Data: string): { header: string; content: string } {
  const decodedData = base64ToUtf8(base64Data);
  const parts = decodedData?.split(String.fromCodePoint(0x20)); // ASCII 0x20 is space as per Syslog message format spec %d32 hexa code separator

  // According to the Syslog message format spec, the header consists of 8 parts
  const headerParts = parts.slice(0, 7);
  const contentParts = parts.slice(7);

  const header = headerParts.join(String.fromCodePoint(0x20));
  const content = contentParts.join(String.fromCodePoint(0x20));

  return { header, content };
}
