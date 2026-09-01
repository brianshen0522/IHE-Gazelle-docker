import { describe, it, expect, vi, beforeEach } from "vitest";
import axios from "axios";
import { registerUser, submitRegistration, getLocalOrganizations, activateUser } from "../actions";
import { RegisterUser, NewUserRequest } from "../types";

// Mock axios
vi.mock("axios");
const mockedAxios = vi.mocked(axios);

// Mock locale provider
vi.mock("@user-management/services/localeProvider", () => ({
  selectedLocaleProvider: vi.fn(() => Promise.resolve("en")),
}));

describe("Registration Actions", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    process.env.GZL_GUM_API_URL = "http://localhost:3000/api";
  });

  describe("registerUser", () => {
    const userInfos = {
      firstName: "John",
      lastName: "Doe",
      email: "john.doe@example.com",
      password: "SecurePass123!", // NOSONAR(typescript:S2068)
      passwordConfirmation: "SecurePass123!", // NOSONAR(typescript:S2068)
    };

    const organizationInfos = {
      name: "ACME Corporation",
      shortname: "acme-corp",
    };

    describe("when creating a new organization", () => {
      it("should include organization details in the registration request", async () => {
        const registerData: RegisterUser = {
          userInfos,
          organizationInfos,
          selectedOrg: null,
          joinOrCreateOrg: "CREATE",
          acceptedTOS: true,
        };

        mockedAxios.post.mockResolvedValueOnce({
          data: {
            id: "user-123",
            email: "john.doe@example.com",
            firstName: "John",
            lastName: "Doe",
            activated: false,
          },
        });

        await registerUser(registerData);

        expect(mockedAxios.post).toHaveBeenCalledWith(
          "http://localhost:3000/api/v2/users/register",
          {
            firstName: "John",
            lastName: "Doe",
            email: "john.doe@example.com",
            password: "SecurePass123!", // NOSONAR(typescript:S2068)
            passwordConfirmation: "SecurePass123!", // NOSONAR(typescript:S2068)
            organization: organizationInfos,
            consent: true,
          },
          expect.objectContaining({
            headers: { "Accept-Language": "en" },
          }),
        );
      });

      it("should return the registration response data", async () => {
        const registerData: RegisterUser = {
          userInfos,
          organizationInfos,
          selectedOrg: null,
          joinOrCreateOrg: "CREATE",
          acceptedTOS: true,
        };

        const mockResponse = {
          id: "user-123",
          email: "john.doe@example.com",
          firstName: "John",
          lastName: "Doe",
          activated: false,
        };

        mockedAxios.post.mockResolvedValueOnce({ data: mockResponse });

        const result = await registerUser(registerData);

        expect(result).toEqual(mockResponse);
      });
    });

    describe("when joining an existing organization", () => {
      it("should include organizationId in the registration request", async () => {
        const selectedOrg = {
          id: "org-123",
          name: "Existing Org",
          shortname: "existing-org",
        };

        const registerData: RegisterUser = {
          userInfos,
          organizationInfos,
          selectedOrg,
          joinOrCreateOrg: "JOIN",
          acceptedTOS: true,
        };

        mockedAxios.post.mockResolvedValueOnce({
          data: {
            id: "user-123",
            email: "john.doe@example.com",
          },
        });

        await registerUser(registerData);

        expect(mockedAxios.post).toHaveBeenCalledWith(
          "http://localhost:3000/api/v2/users/register",
          {
            firstName: "John",
            lastName: "Doe",
            email: "john.doe@example.com",
            password: "SecurePass123!", // NOSONAR(typescript:S2068)
            passwordConfirmation: "SecurePass123!", // NOSONAR(typescript:S2068)
            organizationId: "org-123",
            consent: true,
          },
          expect.objectContaining({
            headers: { "Accept-Language": "en" },
          }),
        );
      });

      it("should not include organization object when joining", async () => {
        const selectedOrg = {
          id: "org-123",
          name: "Existing Org",
          shortname: "existing-org",
        };

        const registerData: RegisterUser = {
          userInfos,
          organizationInfos,
          selectedOrg,
          joinOrCreateOrg: "JOIN",
          acceptedTOS: true,
        };

        mockedAxios.post.mockResolvedValueOnce({ data: { id: "user-123" } });

        await registerUser(registerData);

        const callArgs = mockedAxios.post.mock.calls[0][1];
        expect(callArgs).not.toHaveProperty("organization");
        expect(callArgs).toHaveProperty("organizationId", "org-123");
      });
    });

    describe("consent handling", () => {
      it("should include consent when TOS is accepted", async () => {
        const registerData: RegisterUser = {
          userInfos,
          organizationInfos,
          selectedOrg: null,
          joinOrCreateOrg: "CREATE",
          acceptedTOS: true,
        };

        mockedAxios.post.mockResolvedValueOnce({ data: { id: "user-123" } });

        await registerUser(registerData);

        const callArgs = mockedAxios.post.mock.calls[0][1];
        expect(callArgs).toHaveProperty("consent", true);
      });

      it("should include consent as false when TOS is not accepted", async () => {
        const registerData: RegisterUser = {
          userInfos,
          organizationInfos,
          selectedOrg: null,
          joinOrCreateOrg: "CREATE",
          acceptedTOS: false,
        };

        mockedAxios.post.mockResolvedValueOnce({ data: { id: "user-123" } });

        await registerUser(registerData);

        const callArgs = mockedAxios.post.mock.calls[0][1];
        expect(callArgs).toHaveProperty("consent", false);
      });
    });
  });

  describe("submitRegistration", () => {
    const newUser: NewUserRequest = {
      firstName: "Jane",
      lastName: "Smith",
      email: "jane.smith@example.com",
      password: "SecurePass456!", // NOSONAR(typescript:S2068)
      passwordConfirmation: "SecurePass456!", // NOSONAR(typescript:S2068)
      organizationId: "org-789",
      consent: true,
    };

    describe("successful registration", () => {
      it("should post to the correct endpoint with correct data", async () => {
        mockedAxios.post.mockResolvedValueOnce({
          data: {
            id: "user-789",
            email: "jane.smith@example.com",
            firstName: "Jane",
            lastName: "Smith",
            activated: false,
          },
        });

        await submitRegistration(newUser);

        expect(mockedAxios.post).toHaveBeenCalledWith(
          "http://localhost:3000/api/v2/users/register",
          newUser,
          expect.objectContaining({
            headers: { "Accept-Language": "en" },
          }),
        );
      });

      it("should return the response data on success", async () => {
        const mockResponse = {
          id: "user-789",
          email: "jane.smith@example.com",
          firstName: "Jane",
          lastName: "Smith",
          activated: false,
        };

        mockedAxios.post.mockResolvedValueOnce({ data: mockResponse });

        const result = await submitRegistration(newUser);

        expect(result).toEqual(mockResponse);
      });
    });

    describe("error handling", () => {
      it("should handle axios errors with error response", async () => {
        const axiosError = {
          response: {
            status: 400,
            data: {
              error: "Validation Error",
              message: "Invalid email format",
            },
          },
          isAxiosError: true,
        };

        mockedAxios.isAxiosError.mockReturnValue(true);
        mockedAxios.post.mockRejectedValueOnce(axiosError);

        const result = await submitRegistration(newUser);

        expect(result).toEqual({
          data: {
            error: "Validation Error",
            code: 400,
            message: "Invalid email format",
          },
          status: 400,
        });
      });

      it("should handle axios errors without response data", async () => {
        const axiosError = {
          message: "Network Error",
          isAxiosError: true,
        };

        mockedAxios.isAxiosError.mockReturnValue(true);
        mockedAxios.post.mockRejectedValueOnce(axiosError);

        const result = await submitRegistration(newUser);

        expect(result).toEqual({
          data: {
            error: undefined,
            code: 500,
            message: "Network Error",
          },
          status: 500,
        });
      });

      it("should handle non-axios errors", async () => {
        const genericError = new Error("Unknown error");

        mockedAxios.isAxiosError.mockReturnValue(false);
        mockedAxios.post.mockRejectedValueOnce(genericError);

        const result = await submitRegistration(newUser);

        expect(result).toEqual({
          data: {
            error: "Unknown error",
            code: 500,
            message: "Unknown error",
          },
          status: 500,
        });
      });

      it("should handle 409 conflict error", async () => {
        const axiosError = {
          response: {
            status: 409,
            data: {
              error: "Conflict",
              message: "User already exists",
            },
          },
          isAxiosError: true,
        };

        mockedAxios.isAxiosError.mockReturnValue(true);
        mockedAxios.post.mockRejectedValueOnce(axiosError);

        const result = await submitRegistration(newUser);

        expect(result).toEqual({
          data: {
            error: "Conflict",
            code: 409,
            message: "User already exists",
          },
          status: 409,
        });
      });
    });
  });

  describe("getLocalOrganizations", () => {
    describe("successful fetch", () => {
      it("should fetch organizations with delegated=false parameter", async () => {
        const mockOrganizations = [
          { shortname: "org-1", name: "Organization One" },
          { shortname: "org-2", name: "Organization Two" },
        ];

        mockedAxios.get.mockResolvedValueOnce({
          data: mockOrganizations,
          status: 200,
        });

        await getLocalOrganizations();

        expect(mockedAxios.get).toHaveBeenCalledWith("http://localhost:3000/api/organizations", {
          params: { delegated: false, limit: 300 },
        });
      });

      it("should return data and status on success", async () => {
        const mockOrganizations = [
          { shortname: "org-1", name: "Organization One" },
          { shortname: "org-2", name: "Organization Two" },
        ];

        mockedAxios.get.mockResolvedValueOnce({
          data: mockOrganizations,
          status: 200,
        });

        const result = await getLocalOrganizations();

        expect(result).toEqual({
          data: mockOrganizations,
          status: 200,
        });
      });
    });

    describe("error handling", () => {
      it("should handle axios errors with response data", async () => {
        const axiosError = {
          response: {
            data: {
              error: "Forbidden",
              message: "Access denied",
            },
          },
          isAxiosError: true,
        };

        mockedAxios.isAxiosError.mockReturnValue(true);
        mockedAxios.get.mockRejectedValueOnce(axiosError);

        const result = await getLocalOrganizations();

        expect(result).toEqual({
          data: {
            error: "Forbidden",
            message: "Access denied",
          },
          error: "Forbidden",
        });
      });

      it("should handle generic errors", async () => {
        const genericError = new Error("Network failure");

        mockedAxios.isAxiosError.mockReturnValue(false);
        mockedAxios.get.mockRejectedValueOnce(genericError);

        const result = await getLocalOrganizations();

        expect(result).toEqual({
          data: null,
          error: "Failed to fetch organizations",
        });
      });
    });
  });

  describe("activateUser", () => {
    const activationCode = "abc123-activation-code";

    describe("successful activation", () => {
      it("should post to the correct activation endpoint", async () => {
        mockedAxios.post.mockResolvedValueOnce({
          data: { activated: true, email: "user@example.com" },
          status: 200,
        });

        await activateUser(activationCode);

        expect(mockedAxios.post).toHaveBeenCalledWith(`http://localhost:3000/api/v2/users/activate/${activationCode}`);
      });

      it("should return data and status on success", async () => {
        const mockResponse = {
          activated: true,
          email: "user@example.com",
          firstName: "Test",
          lastName: "User",
        };

        mockedAxios.post.mockResolvedValueOnce({
          data: mockResponse,
          status: 200,
        });

        const result = await activateUser(activationCode);

        expect(result).toEqual({
          data: mockResponse,
          status: 200,
        });
      });
    });

    describe("error handling", () => {
      it("should handle axios errors with status code", async () => {
        const axiosError = {
          response: {
            status: 400,
            data: {
              error: "Invalid Code",
              message: "Activation code is invalid or expired",
            },
          },
          isAxiosError: true,
        };

        mockedAxios.isAxiosError.mockReturnValue(true);
        mockedAxios.post.mockRejectedValueOnce(axiosError);

        const result = await activateUser(activationCode);

        expect(result).toEqual({
          data: {
            error: "Invalid Code",
            message: "Activation code is invalid or expired",
          },
          status: 400,
        });
      });

      it("should handle axios errors without response", async () => {
        const axiosError = {
          message: "Network Error",
          isAxiosError: true,
        };

        mockedAxios.isAxiosError.mockReturnValue(true);
        mockedAxios.post.mockRejectedValueOnce(axiosError);

        const result = await activateUser(activationCode);

        expect(result).toEqual({
          data: undefined,
          status: 500,
        });
      });

      it("should handle non-axios errors", async () => {
        const genericError = new Error("Unknown error");

        mockedAxios.isAxiosError.mockReturnValue(false);
        mockedAxios.post.mockRejectedValueOnce(genericError);

        const result = await activateUser(activationCode);

        expect(result).toEqual({
          data: null,
          status: 500,
        });
      });

      it("should handle 404 not found error", async () => {
        const axiosError = {
          response: {
            status: 404,
            data: {
              error: "Not Found",
              message: "User not found",
            },
          },
          isAxiosError: true,
        };

        mockedAxios.isAxiosError.mockReturnValue(true);
        mockedAxios.post.mockRejectedValueOnce(axiosError);

        const result = await activateUser(activationCode);

        expect(result).toEqual({
          data: {
            error: "Not Found",
            message: "User not found",
          },
          status: 404,
        });
      });
    });
  });
});
