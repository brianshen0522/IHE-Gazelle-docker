interface ServiceInterface {
  interfaceName: string;
  bindings?: { ["@type"]: string; webUrl?: string }[];
}

interface Service {
  providedInterfaces?: ServiceInterface[];
}

export function isUserInterface(iface: { interfaceName: string }): boolean {
  return iface.interfaceName.toLowerCase() === "user interface";
}

/**
 * Retrieves the service URL from a given service object based on the specified interface type.
 *
 * @param service - The service object containing provided interfaces and bindings.
 * @param type - The type of interface to look for (default is "web").
 * @returns The URL of the service binding matching the specified type, or "#" if not found.
 */
export const getUrlFromService = (service: Service, type = "web") => {
  const uiInterface = service.providedInterfaces?.find(isUserInterface);
  const binding = uiInterface?.bindings?.find((b) => b["@type"] === type);
  const url = binding?.webUrl ?? "";
  return url;
};

export const normalizeServiceName = (name: string): string => {
  return name.toLowerCase().replaceAll("-", " ").replaceAll("_", " ").replaceAll(/\s+/g, " ").trim();
};
