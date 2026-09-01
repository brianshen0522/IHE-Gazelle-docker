import {Session} from "next-auth";

export const USER = 'user';
export const SUT_OPERATOR = 'role:sut_operator';
export const GAZELLE_ADMIN = 'role:gazelle_admin';
export const PROJECT_ADMIN = 'role:project_admin';
export const TESTING_SESSION_MANAGER = 'role:testing_session_manager';
export const MONITOR = 'role:monitor';
export const TEST_DESIGNER = 'role:test_designer';
export const LATE_REGISTRATION = 'role:late_registration';
export const PREFIX_ORGANIZATION_ADMIN = 'org-adm:';

export const canEditHomePage = (session: Session | null) => {
  if (session?.user) {
    const groups = session.user.groups;
    return groups.includes(GAZELLE_ADMIN) || groups.includes(PROJECT_ADMIN) || groups.includes(TESTING_SESSION_MANAGER) ;
  }
  return false;
};


export const canAccessTestExecution = (session: Session | null) => {
  if (session?.user) {
    const groups = session.user.groups;
    return groups.includes(GAZELLE_ADMIN) || groups.includes(USER) || groups.includes(SUT_OPERATOR) ;
  }
  return false;
};

export const canManageUsers = (session: Session | null) => {
  if (session?.user) {
    const groups = session.user.groups;
    return groups.includes(GAZELLE_ADMIN) ||
        groups.includes(PROJECT_ADMIN) ||
        groups.includes(TESTING_SESSION_MANAGER) ||
        groups.some((grp) => grp.startsWith(PREFIX_ORGANIZATION_ADMIN));
  }
  return false;
};
