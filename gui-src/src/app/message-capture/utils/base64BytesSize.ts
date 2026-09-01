export function base64ToBytesSize(base64String: string) {
  if (!base64String) {
    return 0;
  }
  return atob(base64String).length;
}
