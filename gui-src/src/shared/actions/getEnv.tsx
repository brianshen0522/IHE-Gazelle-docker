"use server";

/**
 * This server action is used to retrieve environment variables from .env file.
 * If default value id needed (if the variable is not set in .env) provide it at usage.
 */
const envKeys = [
  "BASE_URL",
  "FQDN",
  "GZL_MAESTRO_WEBSOCKET_URL",
  "GZL_TEST_EXECUTION_WEBSOCKET_URL",
  "GZL_SIMULATION_REQUEST_TIMEOUT_SECONDS",
  "GZL_DTH_API_URL",
  "GZL_REGISTRATION_URL",
  "GZL_TEST_BED_TITLE",
  "KC_LOGIN_URL",
  "KEYCLOAK_ISSUER",
  "KC_CLIENT_ID",
  "GZL_TM_URL",
  "GZL_DTH_VALIDATION",
] as const;

export type EnvConfig = Record<(typeof envKeys)[number], string | undefined>;

export async function getEnv(): Promise<EnvConfig> {
  const env = {} as EnvConfig;

  envKeys.forEach((key) => {
    env[key] = process.env[key];
  });

  return env;
}
