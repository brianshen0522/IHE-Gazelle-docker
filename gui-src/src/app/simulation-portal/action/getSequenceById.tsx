"use server";

import { SimulationSequence } from "@simulation-portal/types/SimulationSequence";
import { getServerSession } from "next-auth";
import { authOptions } from "@auth/authOptions";

interface GetByIdProps {
  id: string | null;
}

export interface GetByIdResult {
  simulationSequence?: SimulationSequence;
  error?: string;
}

export async function getSequenceById({ id }: GetByIdProps): Promise<GetByIdResult> {
  const message = "Unable to retrieve simulation sequence";
  try {
    if (!id) {
      throw new Error("Id is not defined.");
    }
    const session = await getServerSession(authOptions);
    const token = session?.access_token;

    const encodedId = encodeURIComponent(id);
    const res = await fetch(`${process.env.GZL_SIMULATION_GATEWAY_URL}/${encodedId}`, {
      headers: {
        ...(token && { Authorization: `Bearer ${token}` }),
      },
    });
    if (!res.ok) {
      throw new Error(` : ${res.status}`);
    }
    const data: SimulationSequence = await res.json();
    return { simulationSequence: data };
  } catch (err) {
    const errorMessage = err instanceof Error ? `${message}: ${err.message}` : message;
    return { error: errorMessage };
  }
}
