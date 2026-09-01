// TODO: delete this file after migrating all usages to useEnv hook in shared/hooks/useEnv.tsx

import { useState, useEffect } from "react";
import axios from "axios";

export const getEnv = async () => {
  const { data } = await axios.get(`/gazelle/message-capture/api/env`).catch((err) => {
    return { data: { data: err.response.data } };
  });
  return data;
};

const useEnvironmentVar = () => {
  const [envGzlDthValidation, setEnvGzlDthValidation] = useState("");
  const [envBaseUrl, setEnvBaseUrl] = useState("");

  useEffect(() => {
    const requestEnv = async () => {
      try {
        const response = await getEnv();
        setEnvGzlDthValidation(response.GZL_DTH_VALIDATION);
        setEnvBaseUrl(response.BASE_URL);
      } catch {
        console.error("Error has occured while fetching the environment variable");
      }
    };
    requestEnv();
  }, []);

  return { envGzlDthValidation, envBaseUrl };
};

export default useEnvironmentVar;
