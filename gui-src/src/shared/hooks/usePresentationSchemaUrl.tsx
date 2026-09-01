import { useEnv } from "./useEnv";

const usePresentationSchemaUrl = (schemaName: string, app: string) => {
  const { env } = useEnv();
  const presentationSchemaUrl = env?.BASE_URL ? `${env.BASE_URL}/${app}/api/presentationSchemas/${schemaName}` : null;
  return { presentationSchemaUrl };
};

export default usePresentationSchemaUrl;
