"use server";
import axios from "axios";
import { User, UserPreferences } from "@/app/user-management/components/user-management/Types";
import { selectedLocaleProvider } from "@user-management/services/localeProvider";
import { getSessionAuth } from "../../services/getAuthSession";

const baseUrl = process.env.GZL_GUM_API_URL;

interface CreateUserState {
  success: boolean;
  message: string;
  data?: unknown;
}

interface OrganizationDetails {
  shortname: string;
  name: string;
}

interface UserCreationAttributes {
  firstName: string;
  lastName: string;
  email: string;
  organizationId?: string;
  organization?: OrganizationDetails;
}

// Extracts error message from an unknown error object
function getErrorMessage(err: unknown): string {
  return (err as { response?: { data?: { message?: string } } })?.response?.data?.message || (err instanceof Error ? err.message : "Unknown error");
}

// Extracts error data from an unknown error object (for logging)
function getErrorData(err: unknown): unknown {
  return (err as { response?: { data?: unknown } })?.response?.data;
}

// Extracts HTTP status code from an unknown error object
function getErrorStatusCode(err: unknown): number | undefined {
  return (err as { response?: { status?: number } })?.response?.status;
}

export async function editUserAttribute(user: User, userId: string) {
  const { accessToken } = await getSessionAuth();
  const requestData = {
    firstName: user.firstName,
    lastName: user.lastName,
    email: user.email,
    groupIds: user.groupIds,
    organizationId: user.organizationId,
  };

  try {
    const response = await axios.patch(`${baseUrl}/users/${userId}`, requestData, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
    });
    return response.data;
  } catch (err: unknown) {
    throw new Error(getErrorMessage(err));
  }
}

export async function editUserPreferences(userPref: UserPreferences, userId: string) {
  const { accessToken } = await getSessionAuth();
  const requestData = {
    tableLabel: userPref.tableLabel,
    notifiedByEmail: userPref.notifiedByEmail,
    languagesSpoken: userPref.languagesSpoken,
  };

  try {
    const response = await axios.put(`${baseUrl}/users/${userId}/preferences`, requestData, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
    });
    return response.data;
  } catch (err: unknown) {
    throw new Error(getErrorMessage(err));
  }
}

export async function updateUserActivationStatus(activation: boolean, userId: string) {
  const activationOrder = activation ? "activate" : "deactivate";
  const acceptLanguageHeader = await selectedLocaleProvider();
  const { accessToken } = await getSessionAuth();

  try {
    const response = await axios.post(
      `${baseUrl}/users/${activationOrder}`,
      { userId },
      {
        headers: {
          Authorization: `Bearer ${accessToken}`,
          "Content-Type": "application/json",
          "Accept-Language": acceptLanguageHeader,
        },
      },
    );
    return response.data;
  } catch (err: unknown) {
    throw new Error(getErrorMessage(err));
  }
}

export const deleteUser = async (userId: string) => {
  const { accessToken } = await getSessionAuth();
  const acceptLanguageHeader = await selectedLocaleProvider();

  try {
    const response = await axios.delete(`${baseUrl}/users/${userId}`, {
      headers: { Authorization: `Bearer ${accessToken}`, "Accept-Language": acceptLanguageHeader },
    });
    return response.data;
  } catch (err: unknown) {
    return { success: false, error: getErrorMessage(err) };
  }
};

export async function deleteUserPicture(userId: string) {
  const { accessToken } = await getSessionAuth();
  try {
    const response = await axios.delete(`${baseUrl}/users/${userId}/preferences/picture`, {
      headers: { Authorization: `Bearer ${accessToken}` },
    });
    return response.data;
  } catch (err: unknown) {
    throw new Error(getErrorMessage(err));
  }
}

export async function uploadUserPicture(image: ArrayBuffer, userId: string) {
  const { accessToken } = await getSessionAuth();
  try {
    const response = await axios.put(`${baseUrl}/users/${userId}/preferences/picture`, image, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "image/jpeg",
      },
    });
    return response.data;
  } catch (err: unknown) {
    throw new Error(getErrorMessage(err));
  }
}

export async function createUser(prevState: CreateUserState, formData: FormData): Promise<CreateUserState> {
  const { accessToken, session } = await getSessionAuth();
  const acceptLanguageHeader = await selectedLocaleProvider();

  const firstName = formData.get("firstName") as string | null;
  const lastName = formData.get("lastName") as string | null;
  const email = formData.get("email") as string | null;
  let organizationId = formData.get("organizationId") as string | null;
  const name = formData.get("name") as string | null;
  const shortName = formData.get("shortName") as string | null;

  // If no organization is selected as the user is organization admin, default to their own organization
  const currentUserOrganization = session?.user?.organization;
  if (!organizationId && !name && !shortName && currentUserOrganization) {
    organizationId = currentUserOrganization;
  }

  if (!firstName || !lastName || !email) {
    return { success: false, message: "Missing required user attributes" };
  }

  if (!name && !shortName && !organizationId) {
    return { success: false, message: "Must provide either organizationId or organization details" };
  }

  if (name && !shortName) {
    return { success: false, message: "Organization short name is required when creating a new organization" };
  }

  const attributes: UserCreationAttributes = {
    firstName,
    lastName,
    email,
  };

  // Only include organizationId when selecting existing org
  if (organizationId && !name) {
    attributes.organizationId = organizationId;
  }
  // Only include organization object when creating new org
  else if (name && shortName) {
    attributes.organization = {
      shortname: shortName,
      name: name,
    };
  }

  try {
    const response = await axios.post(`${baseUrl}/users`, attributes, {
      headers: { Authorization: `Bearer ${accessToken}`, "Accept-Language": acceptLanguageHeader },
    });

    return {
      success: true,
      message: "success",
      data: response.data,
    };
  } catch (err: unknown) {
    const statusCode = getErrorStatusCode(err);
    const errorMessage = getErrorMessage(err);
    console.error("Error creating user:", getErrorData(err) || errorMessage);

    // Handle 409 Conflict - organization shortname already exists
    if (statusCode === 409 && name && shortName) {
      return {
        success: false,
        message: `Organization with shortname "${shortName}" already exists. Please choose a different shortname.`,
      };
    }

    return { success: false, message: errorMessage };
  }
}
