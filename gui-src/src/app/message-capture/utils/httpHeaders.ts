export type HttpHeaders = Record<string, unknown> | null | undefined;

export const getHttpHeaderValue = (headers: HttpHeaders, headerName: string): string | undefined => {
  if (!headers) return undefined;

  const normalizedHeaderName = headerName.toLowerCase();
  const matchingHeaderKey = Object.keys(headers).find((key) => key.toLowerCase() === normalizedHeaderName);
  if (!matchingHeaderKey) return undefined;

  const value = headers[matchingHeaderKey];
  if (value === null || value === undefined) return undefined;

  return typeof value === "string" ? value : String(value);
};

export const getHttpContentType = (headers: HttpHeaders): string => getHttpHeaderValue(headers, "content-type") ?? "";

export const getHttpMimeType = (contentType: string): string => contentType.split(";")[0].trim().toLowerCase();
