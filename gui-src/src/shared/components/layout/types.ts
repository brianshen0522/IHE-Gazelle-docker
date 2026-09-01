import { AppLink, BreadcrumbItem, BreadcrumbsProps } from "@gazelle/gazelle-component-ui";
import { Session } from "next-auth";

export interface GazelleRootLayoutProps {
  children: React.ReactNode;
  session?: Session | null;
  testBedConfigurations: TestBedConfigurations;
  navbarConfig: TestBedConfigurations;
  initialConsent: string | null;
  lang: string;
  userRegistrationEnabled?: boolean;
}

export interface TestBedConfigurations {
  platformName: string;
  logoCustomerUrl: string;
  logoCustomerWebsiteUrl: string;
  documentationUrl: string;
  favoriteDocUrl: string;
  helpdeskUrl: string;
  contactEmail: string;
  termsOfServiceUrl: string;
  privacyPolicyUrl: string;
  legalInformationUrl: string;
  cookiesPreferencesUrl: string;
  testSession: [];
  menuEntries: AppLink[];
  error: string;
}

export interface ContentHeaderProps {
  breadcrumbsProps?: BreadcrumbsProps;
  breadcrumbsItems?: BreadcrumbItem[];
  title?: string;
  secured?: boolean;
}

export interface GlobalFooterProps {
  testBedConfigurations: TestBedConfigurations;
}

export interface ClientNavbarProps {
  config: TestBedConfigurations;
  groups: string[];
  documentationUrl?: string;
}

export type ServiceBinding = {
  "@type": string;
  serviceUrl?: string;
  webUrl?: string;
  secured: boolean;
};

export type ProvidedInterface = {
  interfaceName: string;
  interfaceVersion: string;
  bindings: ServiceBinding[];
};

export type ServiceRegistryElement = {
  name: string;
  version: string;
  status: string;
  description: string;
  instanceId: string;
  providedInterfaces: ProvidedInterface[];
  replicaId: string;
  selfRegistered: boolean;
};

export type NavbarApp = {
  id: string;
  name: string;
  description: string;
  icon: string;
  url: string;
  status: string;
  version: string;
};

export interface AppsFromServiceRegistry {
  apps: NavbarApp[];
  otherApps: AppLink[];
}
