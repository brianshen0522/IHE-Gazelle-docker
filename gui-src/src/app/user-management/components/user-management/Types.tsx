import { ReactNode } from "react";
import { CellContext } from "@tanstack/react-table";
import { Session } from "next-auth";

export interface ModalUserProps {
  info?: CellContext<User, ReactNode[]>;
  toggleModal?: (id: string) => void;
}

export type KeycloakProps = {
  envKcBaseUrl: string;
  envKcRealm: string;
  envClientId: string;
  envGZLLoginUrl: string;
};

export interface User {
  selected?: null;
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  organizationId: string;
  organization?: string;
  organizationName?: string;
  groupIds: string[];
  delegated: boolean;
  activated: boolean;
  lastLoginTimestamp?: Date;
  actions?: ReactNode[];
}

export interface UserPreferences {
  notifiedByEmail: boolean;
  languagesSpoken: string[];
  profileThumbnailUri: string;
  profilePictureUri: string;
  tableLabel: string;
}

export interface EditUserProps {
  userPref?: UserPreferences;
  user?: User;
  userPictureUrl?: string;
  session?: Session;
  isSidePanelContext?: boolean;
  account: boolean;
  originUrl?: string | null;
}

// Type definitions for the users filters parameters
export type UsersParams = {
  [key: string]: string;
};

export type UsersOptions = {
  offset?: number;
  limit?: number;
  params?: UsersParams;
  sortBy: string;
  sortOrder: "ASC" | "DESC" | null;
};

export interface Organization {
  id: string;
  name: string;
  shortname: string;
  url: string;
  delegated: boolean;
  archived: boolean;
  lastUpdateTimestamp: Date;
  action?: ReactNode[];
}
