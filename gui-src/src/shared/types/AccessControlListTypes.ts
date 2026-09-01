import { Session } from "next-auth";
import { type LucideIcon } from "lucide-react";

export type AccessControlList = {
  owners?: string[];
  readers?: string[];
  editors?: string[];
  isPublic: boolean;
  readAccessKey?: string;
};

export interface FormattedMember {
  id: string;
  name: string;
  organization: string;
}

export interface FormattedMembers {
  owners: FormattedMember[];
  editors: FormattedMember[];
  viewers: FormattedMember[];
}

export interface UseAclManagementParams {
  initialAcl: AccessControlList;
  itemId: string;
  session: Session | null;
  customPersist?: PersistAclFunction;
}

export interface UseAclManagementResult {
  acl: AccessControlList;
  formattedMembers: FormattedMembers;
  isUpdating: boolean;
  onChangeToOwner: (userId: string) => Promise<void>;
  onChangeToEditor: (userId: string) => Promise<void>;
  onChangeToViewer: (userId: string) => Promise<void>;
  onRemoveMember: (userId: string) => Promise<void>;
  handleGiveAccess: (userId: string, role: string) => Promise<void>;
  updateAcl: (newAcl: AccessControlList) => void;
}

type PolicyType = "public" | "private" | "link" | "users";

export interface PolicyMetadata {
  label: string;
  description: string;
  icon: LucideIcon;
  type: PolicyType;
}

export interface UseAclPrivacyParams {
  acl: AccessControlList;
  itemId: string;
  session: Session | null;
  onAclUpdate: (newAcl: AccessControlList) => void;
  customPersist?: PersistAclFunction;
}

export interface UseAclPrivacyResult {
  policy: PolicyMetadata;
  currentURL: string;
  isUpdating: boolean;
  onPrivacyPolicyChange: (policy: string) => Promise<void>;
}

export interface FormattedMember {
  id: string;
  name: string;
  organization: string;
}

export interface UseAclUsersParams {
  owners: FormattedMember[];
  editors: FormattedMember[];
  viewers: FormattedMember[];
  session: Session | null;
  enabled: boolean;
}

export interface UseAclUsersResult {
  availableUsers: { id: string; name: string }[];
  availableGroups: { id: string; name: string }[];
  offset: number;
  setOffset: (value: number | ((prev: number) => number)) => void;
  searchTerm: string;
  setSearchTerm: (term: string) => void;
  isLoading: boolean;
}

export type PersistAclFunction = (params: {
  itemId: string;
  accessControlList: AccessControlList;
  session: Session | null;
}) => Promise<AccessControlList>;

export interface AclDisplayProps {
  acl: AccessControlList;
  itemId: string;
  className?: string;
  customPersist?: PersistAclFunction;
  disableLinkPrivacy?: boolean;
}
