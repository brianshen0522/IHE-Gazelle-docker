"use server";

export type MenuDAO = {
  platformName: string;
  logoCustomerUrl: string;
  documentationUrl: string;
  helpdeskUrl: string;
  contactEmail: string;
  termsOfServiceUrl: string;
  privacyPolicyUrl: string;
  legalInformationUrl: string;
  cookiesPreferencesUrl: string;
  menuEntries: MenuEntryDAO[];
}

export type MenuEntryDAO = {
  name: string;
  serviceName?: string;
  description?: string;
  url?: string;
  icon?: string;
  children?: MenuEntryDAO[];
}
