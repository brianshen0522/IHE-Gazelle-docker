"use server";
import { authOptions } from "@/shared/components/auth/authOptions";
import { AppsFromServiceRegistry, NavbarApp, ServiceRegistryElement, TestBedConfigurations } from "@/shared/components/layout/types";
import axios from "axios";
import { AppLink } from "@gazelle/gazelle-component-ui";
import { getServerSession } from "next-auth";

// Transform service registry elements to app format for navbar
function transformServiceToApp(service: ServiceRegistryElement): NavbarApp {
  return {
    id: service.instanceId,
    name: service.name,
    description: service.description,
    icon: service.name,
    url: service.providedInterfaces?.flatMap((i) => i.bindings || []).find((binding) => binding["@type"] === "WEB")?.webUrl || "#",
    status: service.status,
    version: service.version,
  };
}

type ServiceRegistryResult = {
  success: boolean;
  message?: string;
  services: ServiceRegistryElement[];
};

export async function getServicesFromServiceRegistry(): Promise<ServiceRegistryResult> {
  try {
    const session = await getServerSession(authOptions);
    const accessToken = session?.access_token;

    let header = {};
    if (accessToken) {
      header = { Authorization: `Bearer ${accessToken}` }
    }

    // FIXME: Problem if with have more than 250 services registered here, we should search from serviceName (or name) instead of _limit
    const { data } = await axios.get<ServiceRegistryElement[]>(`${process.env.GZL_SERVICE_REGISTRY_URL}/services?_limit=250`, {headers: header});

    const allowedStatuses = new Set(["AVAILABLE", "UNKNOWN", "UNREACHABLE"]);
    const filtered = data.filter((service) => allowedStatuses.has(service.status));
    return { success: true, services: filtered };
  } catch (err: unknown) {
    let message = "Failed to fetch services.";
    if (err instanceof Error) {
      message = err.message || message;
      console.error("Failed to fetch services:", message);
    } else {
      console.error("Failed to fetch services:", err);
    }
    return { success: false, message, services: [] };
  }
}

export async function getAppsFromServiceRegistry(
    config: Pick<TestBedConfigurations, "menuEntries">,
): Promise<AppsFromServiceRegistry & { message?: string }> {
  try {
    const serviceResult = await getServicesFromServiceRegistry();
    if (!serviceResult.success) {
      return {
        apps: [],
        otherApps: [],
        message: serviceResult.message,
      };
    }
    const apps = serviceResult.services.map(transformServiceToApp);
    const urlMap: Record<string, string> = apps.reduce(
        (acc, app) => {
          acc[normalizeServiceName(app.name)] = app.url;
          return acc;
        },
        {} as Record<string, string>,
    );

    const otherApps: AppLink[] =
        config.menuEntries?.map((entry: AppLink) => {
          if (entry.children) {
            return {
              ...entry,
              children: entry.children.map((child: AppLink) => {
                if (child.serviceName) {
                  const url = urlMap[normalizeServiceName(child.serviceName)] ?? "";
                  return { ...child, url };
                }
                return child;
              }),
            };
          }
          return entry;
        }) || [];

    return {
      apps: apps,
      otherApps: otherApps,
    };
  } catch (err: unknown) {
    let message = "Failed to fetch and transform services.";
    if (err instanceof Error) {
      message = err.message || message;
      console.error("Failed to fetch and transform services:", message);
    }
    return {
      apps: [],
      otherApps: [],
      message,
    };
  }
}

/**
 * Normalize service name by converting to lowercase, replacing hyphens and underscores with spaces,
 * collapsing multiple spaces into a single space, and trimming leading/trailing spaces.
 * @param name The service name to normalize
 * @returns The normalized service name
 */
function normalizeServiceName(name: string): string {
  return name.toLowerCase().replaceAll("-", " ").replaceAll("_", " ").replaceAll(/\s+/g, " ").trim();
}
