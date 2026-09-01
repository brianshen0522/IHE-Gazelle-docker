"use server";
import { TestBedConfigurations } from "@/shared/components/layout/types";
import { promises as fs } from "node:fs";
import { parse } from "yaml";

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
  testSession: [];
  error: string;
};

export type MenuEntryDAO = {
  name: string;
  serviceName?: string;
  description?: string;
  url?: string;
  icon?: string;
  children?: MenuEntryDAO[];
};

/**
 * Read HTML home content file
 */
export async function readHTMLHomeContent(): Promise<string> {
  const fqdn = process.env.FQDN ?? "";
  const scheme = process.env.SCHEME ?? "";
  const configurationFolder = process.env.GZL_HOME_CONFIGURATION_FOLDER;
  const fileName = process.env.GZL_HOME_HTML_FILE_NAME ?? "home.html";
  const filePath = (configurationFolder ?? ".") + "/" + fileName;

  try {
    return await readFileAndReturnContent(filePath, fqdn, scheme);
  } catch (err) {
    if (!(err instanceof SyntaxError)) {
      const defaultFilePath = process.cwd() + "/resources/home/default-home.html";
      return readFileAndReturnContent(defaultFilePath, fqdn, scheme);
    }
    throw err;
  }
}

/**
 * Read YAML menu entries as TestBedConfigurations object
 */
export async function readTestBedConfigurations(): Promise<TestBedConfigurations> {
  const testBedConfigurationsString = await readYAMLTestBedConfigurations();
  return parse(testBedConfigurationsString, { version: "1.1" });
}

/**
 * Read YAML menu entries for Navbar UI component
 */
export async function readNavbarConfiguration(): Promise<TestBedConfigurations> {
  return await readTestBedConfigurations();
}

/**
 * Read YAML menu entries as string
 */
export async function readYAMLTestBedConfigurations(): Promise<string> {
  const fqdn = process.env.FQDN;
  const scheme = process.env.SCHEME;
  const configurationFolder = process.env.GZL_HOME_CONFIGURATION_FOLDER;
  const fileName = process.env.GZL_HOME_HTML_FILE_NAME ?? "menu.yaml";
  const filePath = (configurationFolder ?? ".") + "/" + fileName;

  try {
    return await readFileAndReturnContent(filePath, fqdn ?? "", scheme ?? "");
  } catch (err) {
    if (!(err instanceof SyntaxError)) {
      console.warn(`Unable to read ${filePath}, retrieve the default configuration file...`);
      const defaultFilePath = process.cwd() + "/resources/home/default-menu.yaml";
      return readFileAndReturnContent(defaultFilePath, fqdn ?? "", scheme ?? "");
    }
    throw err;
  }
}

const readFileAndReturnContent = async (filePath: string, fqdn: string, scheme: string) => {
  const fileContents = await fs.readFile(filePath, "utf-8");
  return fileContents.replaceAll("${FQDN}", fqdn).replaceAll("${SCHEME}", scheme);
};
