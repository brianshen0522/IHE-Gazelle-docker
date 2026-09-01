import { describe, it, expect } from "vitest";
import { isUserInterface, getUrlFromService, normalizeServiceName } from "./serviceRegistryUtils";

describe("isUserInterface", () => {
  it("returns true for 'User Interface' (case-insensitive)", () => {
    expect(isUserInterface({ interfaceName: "User Interface" })).toBe(true);
    expect(isUserInterface({ interfaceName: "user interface" })).toBe(true);
    expect(isUserInterface({ interfaceName: "USER INTERFACE" })).toBe(true);
  });

  it("returns false for other interface names", () => {
    expect(isUserInterface({ interfaceName: "Admin Interface" })).toBe(false);
    expect(isUserInterface({ interfaceName: "userinterface" })).toBe(false);
    expect(isUserInterface({ interfaceName: "" })).toBe(false);
  });
});

describe("getUrlFromService", () => {
  it("returns the correct URL for matching type", () => {
    const service = {
      providedInterfaces: [
        {
          interfaceName: "User Interface",
          bindings: [
            { "@type": "web", webUrl: "https://example.com/web" },
            { "@type": "api", webUrl: "https://example.com/api" },
          ],
        },
      ],
    };
    expect(getUrlFromService(service, "web")).toBe("https://example.com/web");
    expect(getUrlFromService(service, "api")).toBe("https://example.com/api");
  });

  it("returns empty string if no matching binding", () => {
    const service = {
      providedInterfaces: [
        {
          interfaceName: "User Interface",
          bindings: [{ "@type": "web", webUrl: "https://example.com/web" }],
        },
      ],
    };
    expect(getUrlFromService(service, "api")).toBe("");
  });

  it("returns empty string if no bindings", () => {
    const service = {
      providedInterfaces: [
        {
          interfaceName: "User Interface",
        },
      ],
    };
    expect(getUrlFromService(service, "web")).toBe("");
  });

  it("returns empty string if no User Interface", () => {
    const service = {
      providedInterfaces: [
        {
          interfaceName: "Admin Interface",
          bindings: [{ "@type": "web", webUrl: "https://example.com/web" }],
        },
      ],
    };
    expect(getUrlFromService(service, "web")).toBe("");
  });

  it("returns empty string if providedInterfaces is undefined", () => {
    const service = {};
    expect(getUrlFromService(service, "web")).toBe("");
  });

  it("defaults type to 'web'", () => {
    const service = {
      providedInterfaces: [
        {
          interfaceName: "User Interface",
          bindings: [{ "@type": "web", webUrl: "https://example.com/web" }],
        },
      ],
    };
    expect(getUrlFromService(service)).toBe("https://example.com/web");
  });
});

describe("normalizeServiceName", () => {
  it("normalizes hyphens, underscores, and extra spaces", () => {
    expect(normalizeServiceName("My-Service_Name")).toBe("my service name");
    expect(normalizeServiceName("My   Service--Name__Test")).toBe("my service name test");
    expect(normalizeServiceName("  My_Service  ")).toBe("my service");
  });

  it("handles empty string", () => {
    expect(normalizeServiceName("")).toBe("");
  });

  it("handles already normalized names", () => {
    expect(normalizeServiceName("my service name")).toBe("my service name");
  });
});
