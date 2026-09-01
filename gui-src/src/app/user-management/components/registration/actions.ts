"use server";
import axios from "axios";
import { NewUserRequest, RegisterUser } from "@/app/user-management/components/registration/types";
import { selectedLocaleProvider } from "@user-management/services/localeProvider";

export async function registerUser({ userInfos, organizationInfos, selectedOrg, joinOrCreateOrg, acceptedTOS }: RegisterUser) {
  const isNewOrganization = joinOrCreateOrg === "CREATE";
  const newUser = isNewOrganization
    ? {
        ...userInfos,
        organization: organizationInfos,
        consent: acceptedTOS,
      }
    : {
        ...userInfos,
        organizationId: selectedOrg?.id,
        consent: acceptedTOS,
      };

  return await submitRegistration(newUser);
}

export async function submitRegistration(newUser: NewUserRequest) {
  const acceptLanguageHeader = await selectedLocaleProvider();
  try {
    const response = await axios.post(`${process.env.GZL_GUM_API_URL}/v2/users/register`, newUser, {
      headers: { "Accept-Language": acceptLanguageHeader },
    });
    return response.data;
  } catch (err: unknown) {
    const statusCode = axios.isAxiosError(err) ? err.response?.status || 500 : 500;
    return {
      data: {
        error: axios.isAxiosError(err) ? err.response?.data?.error : "Unknown error",
        code: statusCode,
        message: axios.isAxiosError(err) ? err.response?.data?.message || err.message : "Unknown error",
      },
      status: statusCode,
    };
  }
}

export async function getLocalOrganizations() {
  try {
    const { data, status } = await axios.get(`${process.env.GZL_GUM_API_URL}/organizations`, {
      params: { delegated: false, limit: 300 }, //TODO do not retrieve all orga here try to search based on user writing
    });
    return { data, status };
  } catch (err: unknown) {
    if (axios.isAxiosError(err) && err.response?.data) {
      return { data: err.response.data, error: err.response.data.error };
    } else {
      return { data: null, error: "Failed to fetch organizations" };
    }
  }
}

export type ActivationData = {
  firstName: string;
  lastName: string;
};

export type ActivationResponse = {
  data: unknown;
  status?: number;
};

export async function activateUser(activationCode: string): Promise<ActivationResponse> {
  try {
    const response = await axios.post(`${process.env.GZL_GUM_API_URL}/v2/users/activate/${activationCode}`);
    return { data: response.data, status: response.status };
  } catch (err: unknown) {
    const statusCode = axios.isAxiosError(err) ? err.response?.status || 500 : 500;
    return { data: axios.isAxiosError(err) ? err.response?.data : null, status: statusCode };
  }
}
