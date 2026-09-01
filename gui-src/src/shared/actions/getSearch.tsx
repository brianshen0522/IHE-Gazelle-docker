'use server'

export interface SearchProps {
  searchParameters: Record<string, string>;
  field: string;
  sortOrder: "asc" | "desc" | null;
  offset: number;
  limit: number;
  token?: string;
}

export interface SearchResultProps<T> {
  searchResult?: T[];
  contentRange?: string | null;
  error?: string;
}

export async function getSearch<T>({
                                     searchParameters,
                                     field,
                                     sortOrder,
                                     offset,
                                     limit,
                                     token
                                   }: SearchProps): Promise<SearchResultProps<T>> {
  const message = 'Unable to retrieve data';
  try {
    const url = buildUrl({searchParameters, field, sortOrder, offset, limit})
    const res = await fetch(url, {
      headers: {
        ...(token && { Authorization: `Bearer ${token}` }),
      },
    });
    if (!res.ok) {
      throw new Error(` : ${res.status}`);
    }
    const data: T[] = await res.json()

    return {
      searchResult: data,
      contentRange: res.headers.get("content-range")
    };
  } catch (err) {
    const errorMessage = err instanceof Error ? `${message}: ${err.message}` : message;
    return {error: errorMessage};
  }

}

function buildUrl({searchParameters, field, sortOrder, offset, limit}: SearchProps) {
  const query = new URLSearchParams(searchParameters).toString();
  let url = `${process.env.GZL_SIMULATION_GATEWAY_URL}?${query}${query ? "&" : ""}_offset=${offset}&_limit=${limit}`;

  if (field && sortOrder !== null) {
    const fields = field.split(",");
    const orders = sortOrder.split(",");

    const sortParams = fields
      .map((f, index) => {
        const order = orders[index];
        return `${order === "desc" ? "-" : ""}${f}`;
      })
      .join(",");

    url += `&_sort=${sortParams}`;
  }
  return url;
}