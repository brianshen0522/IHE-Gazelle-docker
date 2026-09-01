import type { ValidationInput } from "@/shared/types/validation/types";

export function getAttachmentIdFromItem(item: ValidationInput): string | null {
  const location = item.location?.split(/[?#]/)[0];
  if (location) {
    return location.split("/").findLast(Boolean) ?? location;
  }
  return item.itemId ?? null;
}
