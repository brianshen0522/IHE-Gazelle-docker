import { Session } from "next-auth";
import { GAZELLE_ADMIN, PREFIX_ORGANIZATION_ADMIN, TESTING_SESSION_MANAGER, PROJECT_ADMIN, TEST_DESIGNER } from "@shared/types/GazelleRole";

export const canCreateUser = (session: Session | null) => {
  if (session?.user) {
    const groups = session.user.groups;
    return (
      groups.includes(GAZELLE_ADMIN) ||
      groups.some((grp) => grp.startsWith(PREFIX_ORGANIZATION_ADMIN)) ||
      groups.includes(TESTING_SESSION_MANAGER) ||
      groups.includes(PROJECT_ADMIN)
    );
  }
  return false;
};

export const canManageUsers = (session: Session | null) => {
  if (session?.user) {
    const groups = session.user.groups;
    return (
      groups.includes(GAZELLE_ADMIN) ||
      groups.includes(PROJECT_ADMIN) ||
      groups.includes(TESTING_SESSION_MANAGER) ||
      groups.some((grp) => grp.startsWith(PREFIX_ORGANIZATION_ADMIN))
    );
  }
  return false;
};

export const isGazelleAdmin = (session: Session | null) => {
  if (session?.user) {
    const groups = session.user.groups;
    return groups.includes(GAZELLE_ADMIN);
  }
  return false;
};

export const isOnlyOrgaAdmin = (session: Session | null) => {
  if (session?.user) {
    const groups = session.user.groups;
    return !groups.includes(GAZELLE_ADMIN) && groups.some((grp) => grp.startsWith(PREFIX_ORGANIZATION_ADMIN));
  }
  return false;
};

export const isOrgAdmin = (session: Session | null) => {
  if (session?.user) {
    const groups = session.user.groups;
    return groups.some((grp) => grp.startsWith(PREFIX_ORGANIZATION_ADMIN));
  }
  return false;
};

export const cantDesignTests = (session: Session | null) => {
  if (session?.user) {
    const groups = session.user.groups;
    return !(groups.includes(GAZELLE_ADMIN) || groups.includes(TEST_DESIGNER) || groups.includes(TESTING_SESSION_MANAGER));
  }
  return false;
};
