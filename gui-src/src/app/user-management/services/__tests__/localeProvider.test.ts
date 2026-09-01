/* eslint-disable @typescript-eslint/no-explicit-any */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { selectedLocaleProvider } from "../localeProvider";

// Mock next/headers
vi.mock("next/headers", () => ({
  cookies: vi.fn(),
  headers: vi.fn(),
}));

describe("selectedLocaleProvider", async () => {
  const { cookies, headers } = await import("next/headers");

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("Locale from cookies", () => {
    it("should return locale from GZL_LOCALE cookie when available", async () => {
      const mockCookieStore = {
        get: vi.fn().mockReturnValue({ value: "fr" }),
      };
      vi.mocked(cookies).mockResolvedValue(mockCookieStore as any);

      const locale = await selectedLocaleProvider();

      expect(locale).toBe("fr");
      expect(mockCookieStore.get).toHaveBeenCalledWith("GZL_LOCALE");
    });

    it("should return 'de' locale from cookie", async () => {
      const mockCookieStore = {
        get: vi.fn().mockReturnValue({ value: "de" }),
      };
      vi.mocked(cookies).mockResolvedValue(mockCookieStore as any);

      const locale = await selectedLocaleProvider();

      expect(locale).toBe("de");
    });

    it("should return 'es' locale from cookie", async () => {
      const mockCookieStore = {
        get: vi.fn().mockReturnValue({ value: "es" }),
      };
      vi.mocked(cookies).mockResolvedValue(mockCookieStore as any);

      const locale = await selectedLocaleProvider();

      expect(locale).toBe("es");
    });
  });

  describe("Locale from headers", () => {
    it("should return locale from accept-language-header when cookie is not set", async () => {
      const mockCookieStore = {
        get: vi.fn().mockReturnValue(undefined),
      };
      const mockHeaders = {
        get: vi.fn().mockReturnValue("it"),
      };
      vi.mocked(cookies).mockResolvedValue(mockCookieStore as any);
      vi.mocked(headers).mockResolvedValue(mockHeaders as any);

      const locale = await selectedLocaleProvider();

      expect(locale).toBe("it");
      expect(mockCookieStore.get).toHaveBeenCalledWith("GZL_LOCALE");
      expect(mockHeaders.get).toHaveBeenCalledWith("accept-language-header");
    });

    it("should return locale from header when cookie value is empty", async () => {
      const mockCookieStore = {
        get: vi.fn().mockReturnValue({ value: "" }),
      };
      const mockHeaders = {
        get: vi.fn().mockReturnValue("cs"),
      };
      vi.mocked(cookies).mockResolvedValue(mockCookieStore as any);
      vi.mocked(headers).mockResolvedValue(mockHeaders as any);

      const locale = await selectedLocaleProvider();

      expect(locale).toBe("cs");
    });

    it("should handle complex accept-language header value", async () => {
      const mockCookieStore = {
        get: vi.fn().mockReturnValue(undefined),
      };
      const mockHeaders = {
        get: vi.fn().mockReturnValue("fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7"),
      };
      vi.mocked(cookies).mockResolvedValue(mockCookieStore as any);
      vi.mocked(headers).mockResolvedValue(mockHeaders as any);

      const locale = await selectedLocaleProvider();

      expect(locale).toBe("fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7");
    });
  });

  describe("Default locale", () => {
    it("should return 'en' when neither cookie nor header is set", async () => {
      const mockCookieStore = {
        get: vi.fn().mockReturnValue(undefined),
      };
      const mockHeaders = {
        get: vi.fn().mockReturnValue(null),
      };
      vi.mocked(cookies).mockResolvedValue(mockCookieStore as any);
      vi.mocked(headers).mockResolvedValue(mockHeaders as any);

      const locale = await selectedLocaleProvider();

      expect(locale).toBe("en");
    });

    it("should return 'en' when cookie is undefined and header is empty string", async () => {
      const mockCookieStore = {
        get: vi.fn().mockReturnValue(undefined),
      };
      const mockHeaders = {
        get: vi.fn().mockReturnValue(""),
      };
      vi.mocked(cookies).mockResolvedValue(mockCookieStore as any);
      vi.mocked(headers).mockResolvedValue(mockHeaders as any);

      const locale = await selectedLocaleProvider();

      expect(locale).toBe("en");
    });

    it("should return 'en' when both cookie and header are empty", async () => {
      const mockCookieStore = {
        get: vi.fn().mockReturnValue({ value: "" }),
      };
      const mockHeaders = {
        get: vi.fn().mockReturnValue(null),
      };
      vi.mocked(cookies).mockResolvedValue(mockCookieStore as any);
      vi.mocked(headers).mockResolvedValue(mockHeaders as any);

      const locale = await selectedLocaleProvider();

      expect(locale).toBe("en");
    });
  });

  describe("Priority order", () => {
    it("should prioritize cookie over header when both are set", async () => {
      const mockCookieStore = {
        get: vi.fn().mockReturnValue({ value: "br" }),
      };
      const mockHeaders = {
        get: vi.fn().mockReturnValue("fr"),
      };
      vi.mocked(cookies).mockResolvedValue(mockCookieStore as any);
      vi.mocked(headers).mockResolvedValue(mockHeaders as any);

      const locale = await selectedLocaleProvider();

      expect(locale).toBe("br");
      expect(mockHeaders.get).not.toHaveBeenCalled();
    });

    it("should not call headers when cookie is present", async () => {
      const mockCookieStore = {
        get: vi.fn().mockReturnValue({ value: "en" }),
      };
      const mockHeaders = {
        get: vi.fn(),
      };
      vi.mocked(cookies).mockResolvedValue(mockCookieStore as any);
      vi.mocked(headers).mockResolvedValue(mockHeaders as any);

      await selectedLocaleProvider();

      expect(mockHeaders.get).not.toHaveBeenCalled();
    });
  });
});
