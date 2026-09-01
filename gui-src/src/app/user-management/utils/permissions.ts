import { Session } from "next-auth";

export const GAZELLE_ADMIN = "role:gazelle_admin";
export const PROJECT_ADMIN = "role:project_admin";
export const TESTING_SESSION_MANAGER = "role:testing_session_manager";
export const MONITOR = "role:monitor";
export const TEST_DESIGNER = "role:test_designer";
export const LATE_REGISTRATION = "role:late_registration";
export const SUT_OPERATOR = "role:sut_operator";
export const PREFIX_ORGANIZATION_ADMIN = "org-adm:";

const HIGH_LEVEL_ADMIN_ROLES = [GAZELLE_ADMIN, PROJECT_ADMIN, TESTING_SESSION_MANAGER] as const;

// Helper to safely get groups from session
const getGroups = (session: Session | null): string[] => {
  return session?.user?.groups ?? [];
};

// Core role checks
export const isGazelleAdmin = (session: Session | null) => {
  return getGroups(session).includes(GAZELLE_ADMIN);
};

export const isProjectAdmin = (session: Session | null) => {
  return getGroups(session).includes(PROJECT_ADMIN);
};

export const isTestingSessionManager = (session: Session | null) => {
  return getGroups(session).includes(TESTING_SESSION_MANAGER);
};

export const isOrgAdmin = (session: Session | null) => {
  return getGroups(session).some((grp) => grp.startsWith(PREFIX_ORGANIZATION_ADMIN));
};

export const isOnlyOrgaAdmin = (session: Session | null) => {
  const groups = getGroups(session);
  return groups.some((grp) => grp.startsWith(PREFIX_ORGANIZATION_ADMIN)) && !HIGH_LEVEL_ADMIN_ROLES.some((role) => groups.includes(role));
};

// Hierarchical admin rights checks
export const hasProjectAdministratorRights = (session: Session | null) => {
  return isGazelleAdmin(session) || isProjectAdmin(session);
};

export const hasTestingSessionManagerRights = (session: Session | null) => {
  return hasProjectAdministratorRights(session) || isTestingSessionManager(session);
};

export const hasOrgAdminRights = (session: Session | null) => {
  return hasTestingSessionManagerRights(session) || isOrgAdmin(session);
};

// Permission checks
export const canCreateUser = (session: Session | null) => {
  return hasOrgAdminRights(session);
};

export const canManageUsers = (session: Session | null) => {
  return hasOrgAdminRights(session);
};

export const canManageOrganizations = (session: Session | null) => {
  return hasTestingSessionManagerRights(session);
};

export const canEditOrganization = (session: Session | null) => {
  return isOrgAdmin(session);
};

export const cantDesignTests = (session: Session | null) => {
  const groups = getGroups(session);
  return !(groups.includes(GAZELLE_ADMIN) || groups.includes(TEST_DESIGNER) || groups.includes(TESTING_SESSION_MANAGER));
};

export const canEditRole = (groupKey: string, onSelf: boolean, delegated: boolean, session: Session) => {
  if (delegated) return false;

  if (isGazelleAdmin(session)) {
    return !(groupKey === GAZELLE_ADMIN && onSelf);
  }

  if (isProjectAdmin(session)) {
    return groupKey !== GAZELLE_ADMIN && groupKey !== PROJECT_ADMIN;
  }

  if (isTestingSessionManager(session)) {
    return groupKey !== GAZELLE_ADMIN && groupKey !== PROJECT_ADMIN && groupKey !== TESTING_SESSION_MANAGER;
  }

  if (isOrgAdmin(session)) {
    return !onSelf && (groupKey === SUT_OPERATOR || groupKey.startsWith(PREFIX_ORGANIZATION_ADMIN));
  }

  return false;
};
