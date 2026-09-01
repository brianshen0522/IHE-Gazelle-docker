import { Session } from "next-auth";
import { AccessControlList } from "@/shared/types/AccessControlListTypes";

const GAZELLE_ADMIN = "role:gazelle_admin";
const USER = "user";

/**
 * Determine if the resource can be read by the entity accessing it
 * @param session the current session of the entity, if null or undefined then it is considered unauthenticated.
 * @param accessControlList the access control list of the resource to access
 * @param providedReadAccessKey optional read access key from URL query params
 * @return true if user can read the resource, otherwise false
 */
export function canReadResource(
  session: Session | undefined | null,
  accessControlList: AccessControlList,
  providedReadAccessKey?: string | null,
): boolean {
  // Public access
  if (accessControlList.isPublic) {
    return true;
  }

  // Read access key validation
  if (providedReadAccessKey) {
    const isValid = accessControlList.readAccessKey === undefined || providedReadAccessKey === accessControlList.readAccessKey;
    if (isValid) {
      return true;
    }
  }

  // Authentication required for remaining checks
  if (!session?.user) {
    return false;
  }

  const user = session.user;
  const userGroups = user.groups;

  // Admin check
  if (userGroups.includes(GAZELLE_ADMIN)) {
    return true;
  }

  // Accessible by any authenticated user
  if (accessControlList.readers?.includes(USER)) {
    return true;
  }

  // Combined role check - check all roles in one pass
  const userId = user.gazelleId;

  // Check if user is owner (by ID or group)
  if (accessControlList.owners?.includes(userId) || accessControlList.owners?.some((ownerGroup) => userGroups.includes(ownerGroup))) {
    return true;
  }

  // Check if user is editor (by ID or group)
  if (accessControlList.editors?.includes(userId) || accessControlList.editors?.some((editorGroup) => userGroups.includes(editorGroup))) {
    return true;
  }

  // Check if user is reader (by ID or group)
  if (accessControlList.readers?.includes(userId) || accessControlList.readers?.some((readerGroup) => userGroups.includes(readerGroup))) {
    return true;
  }

  return false;
}

/**
 * Determine if the resource can be updated by the entity accessing it
 * @param session the current session of the entity, if null or undefined then it is considered unauthenticated.
 * @param accessControlList the access control list of the resource to update
 * @return true if user can update the resource, otherwise false
 */
export function canUpdateResource(session: Session | undefined | null, accessControlList: AccessControlList) {
  return (
    isAuthenticated(session) &&
    (isAmongstTheOwners(session, accessControlList) || isAmongstTheEditors(session, accessControlList) || isAdmin(session))
  );
}

/**
 * Determine if the resource can be deleted by the entity accessing it
 * @param session the current session of the entity, if null or undefined then it is considered unauthenticated.
 * @param accessControlList the access control list of the resource to delete
 * @return true if user can delete the resource, otherwise false
 */
export function canDeleteResource(session: Session | undefined | null, accessControlList: AccessControlList) {
  return isAuthenticated(session) && (isAmongstTheOwners(session, accessControlList) || isAdmin(session));
}

/**
 * Determine if the access control list of the resource can be read or updated by the entity accessing it
 * @param session the current session of the entity, if null or undefined then it is considered unauthenticated.
 * @param accessControlList the access control list of the resource to access
 * @return true if user can read the access control list of the resource, otherwise false
 */
export function canReadOrUpdateResourceACL(session: Session | undefined | null, accessControlList: AccessControlList) {
  return isAuthenticated(session) && (isAmongstTheOwners(session, accessControlList) || isAdmin(session));
}

export function isAmongstTheOwners(session: Session | undefined | null, accessControlList: AccessControlList) {
  if (!session) return false;

  const user = session.user;
  return accessControlList.owners?.includes(user.gazelleId) || accessControlList.owners?.some((ownerGroup) => user.groups.includes(ownerGroup));
}

export function isAmongstTheReaders(session: Session | undefined | null, accessControlList: AccessControlList) {
  if (!session) return false;

  const user = session.user;
  return accessControlList.readers?.includes(user.gazelleId) || accessControlList.readers?.some((readerGroup) => user.groups.includes(readerGroup));
}

export function isAmongstTheEditors(session: Session | undefined | null, accessControlList: AccessControlList) {
  if (!session) return false;

  const user = session.user;
  return accessControlList.editors?.includes(user.gazelleId) || accessControlList.editors?.some((editorGroup) => user.groups.includes(editorGroup));
}

export const isAdmin = (session: Session | undefined | null) => {
  if (session === undefined || session === null) {
    return false;
  }
  return session.user.groups.includes(GAZELLE_ADMIN);
};

export const isAuthenticated = (session: Session | undefined | null) => !!session;
