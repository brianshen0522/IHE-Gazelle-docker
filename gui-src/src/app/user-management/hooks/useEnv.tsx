"use client";
import { getEnv } from "@/shared/actions/getEnv";
import { useState, useEffect } from "react";

const useEnv = () => {
  const [envTMUrl, setEnvTMUrl] = useState("");
  const [envGumRegistration, setEnvGumRegistration] = useState("");
  const [envGZLLoginUrl, setEnvGZLLoginUrl] = useState("");
  const [envKcBaseUrl, setEnvKcBaseUrl] = useState("");
  const [envClientId, setEnvClientId] = useState("");

  useEffect(() => {
    const requestEnv = async () => {
      try {
        const response = await getEnv();
        setEnvGumRegistration(response.GZL_REGISTRATION_URL ?? "");
        setEnvGZLLoginUrl(response.KC_LOGIN_URL ?? "");
        setEnvKcBaseUrl(response.KEYCLOAK_ISSUER ?? "");
        setEnvClientId(response.KC_CLIENT_ID ?? "");
        setEnvTMUrl(response.GZL_TM_URL ?? "");
      } catch (error) {
        console.error(error);
        console.error("Error has occured while fetching the env variable");
      }
    };
    requestEnv();
  }, []);

  return { envGumRegistration, envGZLLoginUrl, envKcBaseUrl, envClientId, envTMUrl };
};

export default useEnv;
