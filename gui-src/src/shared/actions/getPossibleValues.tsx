"use server";

interface GetPossibleValuesProps {
  field: string;
  searchParameters: Record<string, string>;
  token?: string;
}

export interface PossibleValuesResult {
  possibleValues?: string[];
  error?: string;
}

export async function getPossibleValues({ field, searchParameters, token }: GetPossibleValuesProps): Promise<PossibleValuesResult> {
  const message = "Unable to retrieve possible values";
  try {
    const query = new URLSearchParams(searchParameters).toString();
    const queries = query ? `?${query}` : "";
    const res = await fetch(
      `${process.env.GZL_SIMULATION_GATEWAY_URL}/indexes/${field}/values${queries}`, {
        headers: {
          ...(token && { Authorization: `Bearer ${token}` }),
        },
      }
    );
    if (!res.ok) {
      throw new Error(` : ${res.status}`);
    }
    const data: string[] = await res.json();
    return { possibleValues: data };
  } catch (err) {
    const errorMessage = err instanceof Error ? `${message}: ${err.message}` : message;
    return { error: errorMessage };
  }
}
