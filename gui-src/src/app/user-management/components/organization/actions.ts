"use server";
import { authOptions } from "@/shared/components/auth/authOptions";
import { getServerSession } from "next-auth/next";
import { revalidatePath } from "next/cache";

interface ActionState {
  success: boolean;
  message: string;
}

export async function editOrganizationAction(prevState: ActionState, formData: FormData): Promise<ActionState> {
  const session = await getServerSession(authOptions);
  const accessToken = session?.access_token;

  const organizationId = formData.get("organizationId") as string;
  const shortname = formData.get("shortName") as string;
  const name = formData.get("name") as string;
  const website = formData.get("website") as string;

  try {
    const response = await fetch(`${process.env.GZL_GUM_API_URL}/organizations/${organizationId}`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
      body: JSON.stringify({
        shortname,
        name,
        url: website || undefined,
      }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || errorData.error || "Failed to update organization");
    }

    revalidatePath("/user-management/organization/edit");

    return {
      success: true,
      message: "Organization updated successfully",
    };
  } catch (error) {
    console.error("Error updating organization:", error);
    return {
      success: false,
      message: error instanceof Error ? error.message : "Failed to update organization",
    };
  }
}

export async function createOrganizationAction(prevState: ActionState, formData: FormData): Promise<ActionState> {
  const session = await getServerSession(authOptions);
  const accessToken = session?.access_token;

  const shortname = formData.get("shortName") as string;
  const name = formData.get("name") as string;
  const website = formData.get("website") as string;

  try {
    const response = await fetch(`${process.env.GZL_GUM_API_URL}/organizations`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
      body: JSON.stringify({
        shortname,
        name,
        url: website || undefined,
      }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || errorData.error || "Failed to create organization");
    }

    revalidatePath("/user-management/organization/create");

    return {
      success: true,
      message: "Organization created successfully",
    };
  } catch (error) {
    console.error("Error creating organization:", error);
    return {
      success: false,
      message: error instanceof Error ? error.message : "Failed to create organization",
    };
  }
}

export async function archiveOrganizationAction(organizationId: string): Promise<ActionState> {
  const session = await getServerSession(authOptions);
  const accessToken = session?.access_token;

  try {
    const response = await fetch(`${process.env.GZL_GUM_API_URL}/organizations/${organizationId}`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || errorData.error || "Failed to archive organization");
    }

    return {
      success: true,
      message: "Organization archived successfully",
    };
  } catch (error) {
    console.error("Error archiving organization:", error);
    return {
      success: false,
      message: error instanceof Error ? error.message : "Failed to archive organization",
    };
  }
}
