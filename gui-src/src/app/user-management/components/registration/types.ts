export interface Configs {
  organizationCreationEnabled: boolean;
  userRegistrationEnabled: boolean;
  termsOfServiceUrl: string;
  purgeInactivatedUsersAfterDays: number;
  userCreationEmailNotificationEnabled: boolean;
}

export interface SuccessResponse {
  activated: boolean;
  email: string;
  firstName: string;
  id: string;
  lastLoginTimestamp: number;
  lastName: string;
  organizationId: string;
  groups: Array<string>;
}

export interface ErrorResponse {
  error: string;
  message?: string;
}

export interface RegistrationResponse {
  data: SuccessResponse | ErrorResponse;
  status: number;
}

export interface FullResponse {
  status: "success" | "error";
  response: RegistrationResponse;
}

export interface OrganizationInfos {
  id?: string;
  name: string;
  shortname: string;
}

export interface UserInfos {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  passwordConfirmation: string;
}

export interface NewUserRequest extends UserInfos {
  organizationInfos?: OrganizationInfos;
  organizationId?: string;
  consent: boolean;
}

export type RegisterUser = {
  userInfos: UserInfos;
  organizationInfos: OrganizationInfos;
  selectedOrg: OrganizationInfos | null;
  joinOrCreateOrg: "JOIN" | "CREATE";
  acceptedTOS: boolean;
};

export interface UserInfos {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  passwordConfirmation: string;
}

export interface UserInformationsProps {
  userInfos: UserInfos;
  setUserInfos: React.Dispatch<React.SetStateAction<UserInfos>>;
}

export type RegistrationValidationProps = {
  userInfos: UserInformationsProps["userInfos"];
  joinOrCreateOrg: "JOIN" | "CREATE";
  selectedOrg: { name: string; shortname: string } | null;
  organizationInfos: { name: string; shortname: string };
  configs: Configs;
};

export interface RegistrationResultProps {
  result: FullResponse;
  joinOrCreateOrg: "JOIN" | "CREATE";
  selectedOrg: OrganizationInfos | null;
}

export interface RegistrationFormProps {
  configs: Configs;
  privacyPolicyUrl: string;
}
