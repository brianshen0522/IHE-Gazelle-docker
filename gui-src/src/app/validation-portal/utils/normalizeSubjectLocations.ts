export function normalizeSubjectLocations(locations?: { value?: string }[] | null): string {
  const set = new Set((locations ?? []).map((l) => l.value).filter((v): v is string => Boolean(v)));

  return Array.from(set).join("|");
}
