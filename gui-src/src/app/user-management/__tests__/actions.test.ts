import { describe, it, expect, vi, beforeEach } from "vitest";
import axios from "axios";
import { getGUMConfigurations } from "../actions";

// Mock axios
vi.mock("axios");
const mockedAxios = vi.mocked(axios);

describe("User Management Actions", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    process.env.GZL_GUM_API_URL = "http://localhost:3000/api";
  });

  describe("getGUMConfigurations", () => {
    it("should successfully fetch GUM configurations", async () => {
      const mockConfigData = {
        registration: {
          enabled: true,
          requireEmailVerification: true,
        },
        authentication: {
          enableSSO: false,
        },
      };

      mockedAxios.get.mockResolvedValueOnce({
        data: mockConfigData,
        status: 200,
      });

      const result = await getGUMConfigurations();

      expect(mockedAxios.get).toHaveBeenCalledWith("http://localhost:3000/api/configurations");
      expect(result).toEqual({
        data: mockConfigData,
      });
    });

    it("should handle axios error with response data", async () => {
      const errorResponse = {
        response: {
          data: {
            error: "Configuration not found",
            message: "Unable to retrieve configurations",
          },
        },
      };

      mockedAxios.get.mockRejectedValueOnce(errorResponse);
      mockedAxios.isAxiosError.mockReturnValueOnce(true);

      const result = await getGUMConfigurations();

      expect(mockedAxios.get).toHaveBeenCalledWith("http://localhost:3000/api/configurations");
      expect(result).toEqual({
        data: errorResponse.response.data,
        error: "Configuration not found",
      });
    });

    it("should handle axios error without response data", async () => {
      const errorWithoutResponse = new Error("Network error");

      mockedAxios.get.mockRejectedValueOnce(errorWithoutResponse);
      mockedAxios.isAxiosError.mockReturnValueOnce(false);

      const result = await getGUMConfigurations();

      expect(mockedAxios.get).toHaveBeenCalledWith("http://localhost:3000/api/configurations");
      expect(result).toEqual({
        data: null,
        error: "Failed to fetch configs",
      });
    });

    it("should handle generic error", async () => {
      const genericError = new Error("Unknown error");

      mockedAxios.get.mockRejectedValueOnce(genericError);
      mockedAxios.isAxiosError.mockReturnValueOnce(false);

      const result = await getGUMConfigurations();

      expect(result).toEqual({
        data: null,
        error: "Failed to fetch configs",
      });
    });

    it("should use correct API endpoint from environment variable", async () => {
      process.env.GZL_GUM_API_URL = "http://api.example.com";

      mockedAxios.get.mockResolvedValueOnce({
        data: {},
        status: 200,
      });

      await getGUMConfigurations();

      expect(mockedAxios.get).toHaveBeenCalledWith("http://api.example.com/configurations");
    });
  });
});
