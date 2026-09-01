import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import axios from "axios";
import { getServicesFromServiceRegistry, getAppsFromServiceRegistry } from "./actions";
import type { ServiceRegistryElement, TestBedConfigurations } from "@/shared/components/layout/types";
import type { AppLink } from "@gazelle/gazelle-component-ui";
import { getServerSession } from "next-auth";

vi.mock("axios");
vi.mock("next-auth", () => ({
  getServerSession: vi.fn(),
}));

const mockedAxios = axios as unknown as {
  get: ReturnType<typeof vi.fn>;
};

const mockServices: ServiceRegistryElement[] = [
  {
    instanceId: "1",
    name: "A",
    description: "",
    status: "AVAILABLE",
    version: "1.0",
    providedInterfaces: [],
    replicaId: "",
    selfRegistered: false,
  },
  {
    instanceId: "2",
    name: "B",
    description: "",
    status: "UNKNOWN",
    version: "1.0",
    providedInterfaces: [],
    replicaId: "",
    selfRegistered: false,
  },
  {
    instanceId: "3",
    name: "C",
    description: "",
    status: "UNREACHABLE",
    version: "1.0",
    providedInterfaces: [],
    replicaId: "",
    selfRegistered: false,
  },
  {
    instanceId: "4",
    name: "D",
    description: "",
    status: "REMOVED",
    version: "1.0",
    providedInterfaces: [],
    replicaId: "",
    selfRegistered: false,
  },
  {
    instanceId: "ui-1",
    name: "Patient Manager",
    description: "Manage patients",
    status: "AVAILABLE",
    version: "1.0",
    providedInterfaces: [
      {
        interfaceName: "User Interface",
        bindings: [
          {
            "@type": "WEB",
            webUrl: "https://patient-manager/web",
            secured: false,
          },
        ],
        interfaceVersion: "",
      },
    ],
    replicaId: "",
    selfRegistered: false,
  },
  {
    instanceId: "api-1",
    name: "Patient API",
    description: "Patient API",
    status: "AVAILABLE",
    version: "1.0",
    providedInterfaces: [
      {
        interfaceName: "REST",
        interfaceVersion: "",
        bindings: [],
      },
    ],
    replicaId: "",
    selfRegistered: false,
  },
  {
    instanceId: "ui-2",
    name: "Test Service",
    description: "Test",
    status: "AVAILABLE",
    version: "1.0",
    providedInterfaces: [
      {
        interfaceName: "User Interface",
        bindings: [
          {
            "@type": "WEB",
            webUrl: "https://test/web",
            secured: false,
          },
        ],
        interfaceVersion: "",
      },
    ],
    replicaId: "",
    selfRegistered: false,
  },
];

describe("getServicesFromServiceRegistry", () => {
  const OLD_ENV = process.env;

  beforeEach(() => {
    vi.clearAllMocks();
    process.env = { ...OLD_ENV, GZL_SERVICE_REGISTRY_URL: "https://mock-registry" };
    const mockedGetServerSession = vi.mocked(getServerSession);
    mockedGetServerSession.mockResolvedValue({ access_token: "mock-token" });
  });

  afterEach(() => {
    process.env = OLD_ENV;
  });

  it("returns filtered services with allowed statuses", async () => {
    mockedAxios.get = vi.fn().mockResolvedValue({ data: mockServices.slice(0, 4) });

    const result = await getServicesFromServiceRegistry();

    expect(mockedAxios.get).toHaveBeenCalledWith("https://mock-registry/services?_limit=250", {
      headers: {
        Authorization: "Bearer mock-token",
      },
    });
    expect(result.success).toBe(true);
    expect(result.services).toHaveLength(3);
    expect(result.services.map((s) => s.status)).toEqual(["AVAILABLE", "UNKNOWN", "UNREACHABLE"]);
  });

  it("returns empty array and logs error on axios failure", async () => {
    const error = new Error("Network error");
    mockedAxios.get = vi.fn().mockRejectedValue(error);
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});

    const result = await getServicesFromServiceRegistry();

    expect(result).toEqual({ success: false, message: "Network error", services: [] });
    expect(consoleSpy).toHaveBeenCalledWith("Failed to fetch services:", "Network error");

    consoleSpy.mockRestore();
  });

  it("returns empty array and logs error for non-Error rejection", async () => {
    mockedAxios.get = vi.fn().mockRejectedValue("some string error");
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});

    const result = await getServicesFromServiceRegistry();

    expect(result.success).toBe(false);
    expect(result.services).toEqual([]);
    expect(consoleSpy).toHaveBeenCalledWith("Failed to fetch services:", "some string error");

    consoleSpy.mockRestore();
  });
});

describe("getAppsFromServiceRegistry", () => {
  const OLD_ENV = process.env;

  beforeEach(() => {
    vi.clearAllMocks();
    process.env = { ...OLD_ENV, GZL_SERVICE_REGISTRY_URL: "https://mock-registry" };
    const mockedGetServerSession = vi.mocked(getServerSession);
    mockedGetServerSession.mockResolvedValue({ access_token: "mock-token" });
  });

  afterEach(() => {
    process.env = OLD_ENV;
  });

  it("transforms UI services to apps and maps menu entries", async () => {
    mockedAxios.get = vi.fn().mockResolvedValue({ data: mockServices });

    const config: Pick<TestBedConfigurations, "menuEntries"> = {
      menuEntries: [
        {
          name: "Patient",
          serviceName: "patient-manager",
          url: "",
          children: [
            {
              name: "Dashboard",
              serviceName: "patient-manager",
              url: "",
            },
          ],
        },
        {
          name: "Static Link",
          url: "https://static.com",
        },
      ] as AppLink[],
    };

    const result = await getAppsFromServiceRegistry(config);

    expect(result.apps).toHaveLength(6);
    const patientManagerApp = result.apps.find(app => app.id === "ui-1");
    expect(patientManagerApp).toEqual({
      id: "ui-1",
      name: "Patient Manager",
      description: "Manage patients",
      icon: "Patient Manager",
      url: "https://patient-manager/web",
      status: "AVAILABLE",
      version: "1.0",
    });

    expect(result.otherApps).toHaveLength(2);
    expect(result.otherApps[0].children?.[0].url).toBe("https://patient-manager/web");
    expect(result.otherApps[1].url).toBe("https://static.com");
  });

  it("handles menu entries with no serviceName", async () => {
    mockedAxios.get = vi.fn().mockResolvedValue({ data: mockServices });

    const config: Pick<TestBedConfigurations, "menuEntries"> = {
      menuEntries: [
        {
          name: "External",
          url: "https://external.com",
          children: [
            {
              name: "Child No Service",
              url: "https://child.com",
            },
          ],
        },
      ] as AppLink[],
    };

    const result = await getAppsFromServiceRegistry(config);

    expect(result.otherApps[0].children?.[0].url).toBe("https://child.com");
  });

  it("returns empty arrays on error", async () => {
    const error = new Error("Failed to fetch");
    mockedAxios.get = vi.fn().mockRejectedValue(error);
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});

    const config: Pick<TestBedConfigurations, "menuEntries"> = {
      menuEntries: [],
    };

    const result = await getAppsFromServiceRegistry(config);

    expect(result).toEqual({ apps: [], otherApps: [], message: "Failed to fetch" });
    expect(consoleSpy).toHaveBeenCalledWith("Failed to fetch services:", "Failed to fetch");

    consoleSpy.mockRestore();
  });

  it("handles non-Error exceptions", async () => {
    mockedAxios.get = vi.fn().mockRejectedValue("unexpected error");
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});

    const config: Pick<TestBedConfigurations, "menuEntries"> = {
      menuEntries: [],
    };

    const result = await getAppsFromServiceRegistry(config);

    expect(result).toEqual({
      apps: [],
      otherApps: [],
      message: "Failed to fetch services."
    });
    expect(consoleSpy).toHaveBeenCalledWith("Failed to fetch services:", "unexpected error");

    consoleSpy.mockRestore();
  });

  it("handles config with no menuEntries", async () => {
    mockedAxios.get = vi.fn().mockResolvedValue({ data: mockServices });

    const config: Pick<TestBedConfigurations, "menuEntries"> = {
      menuEntries: [],
    };

    const result = await getAppsFromServiceRegistry(config);

    expect(result.apps).toHaveLength(6);
    expect(result.otherApps).toEqual([]);
  });

  it("handles missing service URL in urlMap", async () => {
    const mockServices: ServiceRegistryElement[] = [];

    mockedAxios.get = vi.fn().mockResolvedValue({ data: mockServices });

    const config: Pick<TestBedConfigurations, "menuEntries"> = {
      menuEntries: [
        {
          name: "Parent",
          url: "",
          children: [
            {
              name: "Child",
              serviceName: "non-existent-service",
              url: "",
            },
          ],
        },
      ] as AppLink[],
    };

    const result = await getAppsFromServiceRegistry(config);

    expect(result.otherApps[0].children?.[0].url).toBe("");
  });
});
