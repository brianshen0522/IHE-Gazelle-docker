import { NextRequest, NextResponse } from "next/server";

const getOrgId = (group: any): string | null => (group.id?.startsWith("org") ? group.id.split(":")[1] || null : null);

export async function GET(req: NextRequest) {
  const url = new URL(`${process.env.GZL_GUM_API_URL}/groups`);

  const searchParams = req.nextUrl.searchParams;
  const type = searchParams.get("type");
  const offset = searchParams.get("offset");
  const limit = searchParams.get("limit");
  const search = searchParams.get("search");

  // Append query parameters
  if (type) url.searchParams.append("type", type);
  if (offset) url.searchParams.append("offset", offset);
  if (limit) url.searchParams.append("limit", limit);
  if (search) url.searchParams.append("search", search);

  const auth = req.headers.get("Authorization");
  const headers: HeadersInit = {};
  if (auth) headers.Authorization = auth;

  try {
    const response = await fetch(url, { headers });
    const data = await response.json();

    if (!response.ok) {
      return NextResponse.json({ error: data }, { status: response.status });
    }

    if (!Array.isArray(data)) return NextResponse.json(data);

    // Get unique org IDs
    const orgsToFetch = [...new Set(data.map(getOrgId).filter(Boolean))];
    if (orgsToFetch.length === 0) return NextResponse.json(data);

    // Fetch all organization data in parallel and create a map
    const orgMap = Object.fromEntries(
      await Promise.all(
        orgsToFetch.map(async (id) => {
          try {
            const response = await fetch(`${process.env.GZL_GUM_API_URL}/organizations/${id}/`, { headers });
            return [id, (await response.json()).name];
          } catch (e) {
            console.error(`Error fetching org ${id}:`, e);
            return [id, null];
          }
        }),
      ),
    );

    // Map org names to groups
    const resolvedData = data.map((group) => {
      const id = getOrgId(group);
      return id && orgMap[id] ? { ...group, name: orgMap[id] } : group;
    });

    return NextResponse.json({ resolvedData, status: response.status });
  } catch (err: any) {
    console.error(err);
    return NextResponse.json({ error: err?.message || "Unable to get groups" }, { status: 500 });
  }
}
