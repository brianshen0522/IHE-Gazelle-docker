import { describe, it, expect, vi, beforeEach } from "vitest";
import axios from "axios";
import { createUser } from "../actions";

// Mock axios
vi.mock("axios");
const mockedAxios = vi.mocked(axios);

// Mock auth session
vi.mock("../../../services/getAuthSession", () => ({
  getSessionAuth: vi.fn(() => Promise.resolve({ accessToken: "test-token" })),
}));

// Mock locale provider
vi.mock("@user-management/services/localeProvider", () => ({
  selectedLocaleProvider: vi.fn(() => Promise.resolve("en")),
}));

describe("createUser action", () => {
  const initialState = { success: false, message: "" };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("validation", () => {
    it("returns error when firstName is missing", async () => {
      const formData = new FormData();
      formData.append("lastName", "Doe");
      formData.append("email", "john@example.com");
      formData.append("organizationId", "org-1");

      const result = await createUser(initialState, formData);

      expect(result.success).toBe(false);
      expect(result.message).toBe("Missing required user attributes");
    });

    it("returns error when lastName is missing", async () => {
      const formData = new FormData();
      formData.append("firstName", "John");
      formData.append("email", "john@example.com");
      formData.append("organizationId", "org-1");

      const result = await createUser(initialState, formData);

      expect(result.success).toBe(false);
      expect(result.message).toBe("Missing required user attributes");
    });

    it("returns error when email is missing", async () => {
      const formData = new FormData();
      formData.append("firstName", "John");
      formData.append("lastName", "Doe");
      formData.append("organizationId", "org-1");

      const result = await createUser(initialState, formData);

      expect(result.success).toBe(false);
      expect(result.message).toBe("Missing required user attributes");
    });

    it("returns error when neither organizationId nor organization details provided", async () => {
      const formData = new FormData();
      formData.append("firstName", "John");
      formData.append("lastName", "Doe");
      formData.append("email", "john@example.com");

      const result = await createUser(initialState, formData);

      expect(result.success).toBe(false);
      expect(result.message).toBe("Must provide either organizationId or organization details");
    });

    it("returns error when creating new org without shortName", async () => {
      const formData = new FormData();
      formData.append("firstName", "John");
      formData.append("lastName", "Doe");
      formData.append("email", "john@example.com");
      formData.append("name", "ACME Corp");

      const result = await createUser(initialState, formData);

      expect(result.success).toBe(false);
      expect(result.message).toBe("Organization short name is required when creating a new organization");
    });
  });

  describe("successful user creation with existing organization", () => {
    it("creates user with existing organization", async () => {
      const formData = new FormData();
      formData.append("firstName", "John");
      formData.append("lastName", "Doe");
      formData.append("email", "john@example.com");
      formData.append("organizationId", "org-123");

      mockedAxios.post.mockResolvedValueOnce({
        data: { id: "user-123", firstName: "John", lastName: "Doe" },
      });

      const result = await createUser(initialState, formData);

      expect(result.success).toBe(true);
      expect(result.message).toBe("success");
      expect(result.data).toEqual({ id: "user-123", firstName: "John", lastName: "Doe" });

      expect(mockedAxios.post).toHaveBeenCalledWith(
        expect.any(String),
        {
          firstName: "John",
          lastName: "Doe",
          email: "john@example.com",
          organizationId: "org-123",
        },
        expect.objectContaining({
          headers: expect.objectContaining({
            Authorization: "Bearer test-token",
          }),
        }),
      );
    });

    it("does not include organization object when organizationId is provided", async () => {
      const formData = new FormData();
      formData.append("firstName", "John");
      formData.append("lastName", "Doe");
      formData.append("email", "john@example.com");
      formData.append("organizationId", "org-123");

      mockedAxios.post.mockResolvedValueOnce({
        data: { id: "user-123" },
      });

      await createUser(initialState, formData);

      const callArgs = mockedAxios.post.mock.calls[0][1];
      expect(callArgs).not.toHaveProperty("organization");
    });
  });

  describe("successful user creation with new organization", () => {
    it("creates user with new organization", async () => {
      const formData = new FormData();
      formData.append("firstName", "John");
      formData.append("lastName", "Doe");
      formData.append("email", "john@example.com");
      formData.append("name", "ACME Corporation");
      formData.append("shortName", "acme");
      formData.append("website", "https://acme.com");

      mockedAxios.post.mockResolvedValueOnce({
        data: { id: "user-123", firstName: "John", lastName: "Doe" },
      });

      const result = await createUser(initialState, formData);

      expect(result.success).toBe(true);
      expect(result.message).toBe("success");

      expect(mockedAxios.post).toHaveBeenCalledWith(
        expect.any(String),
        {
          firstName: "John",
          lastName: "Doe",
          email: "john@example.com",
          organization: {
            shortname: "acme",
            name: "ACME Corporation",
          },
        },
        expect.objectContaining({
          headers: expect.objectContaining({
            Authorization: "Bearer test-token",
          }),
        }),
      );
    });

    it("does not include organizationId when creating new organization", async () => {
      const formData = new FormData();
      formData.append("firstName", "John");
      formData.append("lastName", "Doe");
      formData.append("email", "john@example.com");
      formData.append("name", "ACME Corporation");
      formData.append("shortName", "acme");
      formData.append("website", "https://acme.com");

      mockedAxios.post.mockResolvedValueOnce({
        data: { id: "user-123" },
      });

      await createUser(initialState, formData);

      const callArgs = mockedAxios.post.mock.calls[0][1];
      expect(callArgs).not.toHaveProperty("organizationId");
    });
  });

  describe("error handling", () => {
    it("handles generic API errors", async () => {
      const formData = new FormData();
      formData.append("firstName", "John");
      formData.append("lastName", "Doe");
      formData.append("email", "john@example.com");
      formData.append("organizationId", "org-123");

      mockedAxios.post.mockRejectedValueOnce({
        response: {
          data: { message: "Internal server error" },
          status: 500,
        },
      });

      const result = await createUser(initialState, formData);

      expect(result.success).toBe(false);
      expect(result.message).toBe("Internal server error");
    });

    it("handles 409 conflict error with custom message for duplicate shortname", async () => {
      const formData = new FormData();
      formData.append("firstName", "John");
      formData.append("lastName", "Doe");
      formData.append("email", "john@example.com");
      formData.append("name", "ACME Corporation");
      formData.append("shortName", "acme");
      formData.append("website", "https://acme.com");

      mockedAxios.post.mockRejectedValueOnce({
        response: {
          data: { message: "Organization already exists" },
          status: 409,
        },
      });

      const result = await createUser(initialState, formData);

      expect(result.success).toBe(false);
      expect(result.message).toBe('Organization with shortname "acme" already exists. Please choose a different shortname.');
    });

    it("handles 409 conflict error normally when not creating new org", async () => {
      const formData = new FormData();
      formData.append("firstName", "John");
      formData.append("lastName", "Doe");
      formData.append("email", "john@example.com");
      formData.append("organizationId", "org-123");

      mockedAxios.post.mockRejectedValueOnce({
        response: {
          data: { message: "User already exists" },
          status: 409,
        },
      });

      const result = await createUser(initialState, formData);

      expect(result.success).toBe(false);
      expect(result.message).toBe("User already exists");
    });

    it("handles errors without response data", async () => {
      const formData = new FormData();
      formData.append("firstName", "John");
      formData.append("lastName", "Doe");
      formData.append("email", "john@example.com");
      formData.append("organizationId", "org-123");

      mockedAxios.post.mockRejectedValueOnce(new Error("Network error"));

      const result = await createUser(initialState, formData);

      expect(result.success).toBe(false);
      expect(result.message).toBe("Network error");
    });

    it("handles unknown errors", async () => {
      const formData = new FormData();
      formData.append("firstName", "John");
      formData.append("lastName", "Doe");
      formData.append("email", "john@example.com");
      formData.append("organizationId", "org-123");

      mockedAxios.post.mockRejectedValueOnce("Unknown error");

      const result = await createUser(initialState, formData);

      expect(result.success).toBe(false);
      expect(result.message).toBe("Unknown error");
    });
  });

  describe("API call configuration", () => {
    it("sends correct headers", async () => {
      const formData = new FormData();
      formData.append("firstName", "John");
      formData.append("lastName", "Doe");
      formData.append("email", "john@example.com");
      formData.append("organizationId", "org-123");

      mockedAxios.post.mockResolvedValueOnce({
        data: { id: "user-123" },
      });

      await createUser(initialState, formData);

      expect(mockedAxios.post).toHaveBeenCalledWith(
        expect.any(String),
        expect.any(Object),
        expect.objectContaining({
          headers: {
            Authorization: "Bearer test-token",
            "Accept-Language": "en",
          },
        }),
      );
    });
  });
});
