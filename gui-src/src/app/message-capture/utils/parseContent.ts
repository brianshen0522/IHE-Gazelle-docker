export function parseContent(data: Record<string, unknown>[]): Record<string, unknown>[] {
  return data?.map((item) => {
    if (typeof item.content === "string") {
      try {
        return { ...item, content: JSON.parse(item.content) };
      } catch {
        console.error(`Failed to parse content: ${item.content}`);
        return item;
      }
    }
    return item;
  });
}
