"use server";
import { getServerSession } from "next-auth";
import axios from "axios";
import { authOptions } from "@/shared/components/auth/authOptions";

export type UserSummary = {
  id: string;
  firstName: string;
  lastName: string;
  organizationId: string;
};

type UserSummaryByIdResult = {
  userSummary?: UserSummary;
  error?: string;
};

/**
 * Get the summary of a user by its id
 * @param id the id of the user
 * @param session the session of the current logged in entity
 * @return a summary of user information if found, otherwise returns an error
 */
export async function getUserSummaryById(id: string): Promise<UserSummaryByIdResult> {
  const session = await getServerSession(authOptions);
  try {
    const url = `${process.env.GZL_GUM_API_URL}/v2/users/${id}/summary`;
    const accessToken = session?.access_token;
    const response = await axios.get(url, {
      headers: {
        authorization: "Bearer " + accessToken,
      },
    });
    const newVar = (await response.data) as UserSummary;

    return { userSummary: newVar };
  } catch (err: unknown) {
    console.error(err);
    return { error: (err as { message?: string })?.message || "Unable to get user summary by id" };
  }
}
