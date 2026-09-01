import { signIn, signOut } from "next-auth/react";

// Helper function to handle 401 errors with token refresh and logout
const handle401 = async () => {
  // Trigger token refresh silently
  const refreshResult = await signIn("keycloak", { redirect: false });

  if (!refreshResult?.ok) {
    // If refresh failed, log the user out
    signOut({ callbackUrl: "/", redirect: true });
  }

  throw new Error("Session refreshed. Retry the request.");
};

export const fetcher = async (...args: [RequestInfo, RequestInit?]) => {
  const res = await fetch(...args);

  // Handle 401 Unauthorized - token expired or invalid
  if (res.status === 401) {
    await handle401();
  }

  if (!res.ok) {
    const error = new Error(`Error: ${res.statusText} (status: ${res.status})`);
    throw error;
  }

  // Clone response to allow reading body multiple times if needed
  const resClone = res.clone();
  const contentType = res.headers.get("content-type");

  if (contentType?.includes("application/json")) {
    try {
      return await res.json();
    } catch (e) {
      console.error("Failed to parse JSON response:", e);
      try {
        const text = await resClone.text();
        console.error("Response text (first 200 chars):", text.substring(0, 200));
      } catch (textError) {
        console.error("Could not read response text:", textError);
      }
      throw new Error("Invalid JSON response from server");
    }
  } else if (contentType?.includes("text/plain") || contentType?.includes("text/html")) {
    return res.text();
  } else {
    // Default to JSON if content type is not specified
    try {
      return await res.json();
    } catch (e) {
      console.error("Failed to parse response as JSON (no content-type):", e);
      try {
        const text = await resClone.text();
        console.error("Response text (first 200 chars):", text.substring(0, 200));
      } catch (textError) {
        console.error("Could not read response text:", textError);
      }
      throw new Error("Invalid JSON response from server");
    }
  }
};

// Function to create a fetcher with auth token
export const authFetcher = (token: string) => {
  return async (url: RequestInfo, init?: RequestInit) => {
    const response = await fetch(url, {
      ...init,
      headers: {
        ...init?.headers,
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });

    // Handle 401 Unauthorized - token expired or invalid
    if (response.status === 401) {
      await handle401();
    }

    if (!response.ok) {
      throw new Error(`Error: ${response.statusText} (status: ${response.status})`);
    }

    const resClone = response.clone();
    const contentType = response.headers.get("content-type");

    if (contentType?.includes("application/json")) {
      try {
        return await response.json();
      } catch (e) {
        console.error("Failed to parse JSON response (auth):", e);
        try {
          const text = await resClone.text();
          console.error("Response text (first 200 chars):", text.substring(0, 200));
        } catch (textError) {
          console.error("Could not read response text:", textError);
        }
        throw new Error("Invalid JSON response from server");
      }
    } else if (contentType?.includes("text/plain") || contentType?.includes("text/html")) {
      return response.text();
    } else {
      // Default to JSON if content type is not specified
      try {
        return await response.json();
      } catch (e) {
        console.error("Failed to parse response as JSON (auth, no content-type):", e);
        try {
          const text = await resClone.text();
          console.error("Response text (first 200 chars):", text.substring(0, 200));
        } catch (textError) {
          console.error("Could not read response text:", textError);
        }
        throw new Error("Invalid JSON response from server");
      }
    }
  };
};

export const blobFetcher = async (url: string) => {
  const res = await fetch(url);

  // Handle 401 Unauthorized - token expired or invalid
  if (res.status === 401) {
    await handle401();
  }

  if (!res.ok) {
    throw new Error(`Error: ${res.statusText} (status: ${res.status})`);
  }
  return res.blob();
};

export const base64Fetcher = async (url: string): Promise<string> => {
  const res = await fetch(url);

  // Handle 401 Unauthorized - token expired or invalid
  if (res.status === 401) {
    await handle401();
  }

  if (!res.ok) {
    throw new Error(`Error: ${res.statusText} (status: ${res.status})`);
  }
  const arrayBuffer = await res.arrayBuffer();
  const base64 = arrayBufferToBase64(arrayBuffer);
  return base64;
};

const arrayBufferToBase64 = (buffer: ArrayBuffer): string => {
  const uint8Array = new Uint8Array(buffer);
  let binaryString = "";
  const chunkSize = 0x8000; // Arbitrary chunk size
  for (let i = 0; i < uint8Array.length; i += chunkSize) {
    binaryString += String.fromCodePoint(...uint8Array.subarray(i, i + chunkSize));
  }
  return Buffer.from(binaryString, "binary").toString("base64");
};
