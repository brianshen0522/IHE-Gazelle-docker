"use server";

import { Session } from "next-auth";
import { AccessControlList } from "@/shared/types/AccessControlListTypes";

type ParamProps = {
  itemId: string;
  accessControlList: AccessControlList;
  session: Session | null;
};

const apiBase = process.env.GZL_DTH_API_URL!;

async function authedFetch(input: RequestInfo, accessToken: string, init: RequestInit = {}) {
  return fetch(input, {
    ...init,
    headers: {
      ...init.headers,
      Authorization: `Bearer ${accessToken}`,
      "Content-Type": "application/json",
    },
  });
}

/**
 * Update the ACL on a connection and all messages referencing it.
 */
export async function updateConnectionAndAllReferencedMessageAcl({ itemId, accessControlList, session }: ParamProps) {
  if (!session?.access_token) {
    throw new Error("No access token in session");
  }
  const token = session.access_token;

  // 1. Update the connection’s ACL
  const connRes = await authedFetch(`${apiBase}/items/${itemId}/acl`, token, {
    method: "PUT",
    body: JSON.stringify(accessControlList),
  });
  if (!connRes.ok) {
    const err = await connRes.text();
    throw new Error(`Failed to update connection ACL: ${err}`);
  }
  const updatedAcl: AccessControlList = await connRes.json();

  // 2. Fetch all messages and connection errors that reference this connection
  const referencedItems = await authedFetch(`${apiBase}/items/?reference=${itemId}`, token, {});
  if (!referencedItems.ok) {
    const err = await referencedItems.text();
    throw new Error(`Failed to fetch referenced messages: ${err}`);
  }
  const items: [{ id: string }] = await referencedItems.json();

  const allItemsToUpdate = [...items];
  // For each direct reference, find and collect its references
  const nestedRefPromises = items.map(async (item) => {
    const nestedRefsResponse = await authedFetch(`${apiBase}/items/?reference=${item.id}`, token, {});
    if (!nestedRefsResponse.ok) {
      const err = await nestedRefsResponse.text();
      console.error(`Failed to fetch nested references for item ${item.id}: ${err}`);
      return []; // Return empty array on error to continue with other items
    }
    const nestedRefs: [{ id: string }] = await nestedRefsResponse.json();
    return nestedRefs;
  });
  // Collect all nested references
  const nestedReferences = await Promise.all(nestedRefPromises);
  // 3. In parallel, update each message’s ACL
  for (const refs of nestedReferences) {
    for (const ref of refs) {
      // Avoid duplicates
      if (!allItemsToUpdate.some((item) => item.id === ref.id)) {
        allItemsToUpdate.push(ref);
      }
    }
  }

  // 4. In parallel, update each item's ACL (both direct and nested references)
  const updatePromises = allItemsToUpdate.map((item) =>
    authedFetch(`${apiBase}/items/${item.id}/acl`, token, {
      method: "PUT",
      body: JSON.stringify(updatedAcl),
    }).then((res) => {
      if (!res.ok) {
        return res.text().then((txt) => {
          throw new Error(`Item ${item.id} ACL update failed: ${txt}`);
        });
      }
    })
  );

  await Promise.all(updatePromises);
}
