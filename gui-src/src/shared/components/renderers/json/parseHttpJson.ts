import {base64ToUtf8} from "@/app/message-capture/utils/base64ToUtf8";

export function parseHttpJson(base64Data: string): string {
  try {
    const jsonData = JSON.parse(base64ToUtf8(base64Data));
    return JSON.stringify(jsonData, null, 2);
  } catch (error) {
    return `Error parsing HTTP data: ${error}`;
  }
}
