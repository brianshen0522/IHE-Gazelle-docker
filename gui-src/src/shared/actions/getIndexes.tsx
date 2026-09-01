'use server'

export interface GetIndexesProps {
  token?: string;
}

export interface Index {
  name: string;
  fieldType: string;
}

export interface IndexResult {
  indexes?: Index[];
  error?: string;
}

export async function getIndexes({ token }: GetIndexesProps): Promise<IndexResult> {
  const message = 'Unable to retrieve indexes';
  try {
    const res = await fetch(
      `${process.env.GZL_SIMULATION_GATEWAY_URL}/indexes`, {
        headers: {
          ...(token && { Authorization: `Bearer ${token}` }),
        },
      }
    );
    if (!res.ok) {
      throw new Error(` : ${res.status}`);
    }
    const data: Index[] = await res.json();
    return {indexes: data};
  } catch (err) {
    const errorMessage = err instanceof Error ? `${message}: ${err.message}` : message;
    return {error: errorMessage};
  }
}