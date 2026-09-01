"use server";
import { readMlangFileFromTestCaseName } from "@test-execution/services/testCaseJSONProvider";

export type ServerActionError = {
  error?: string;
};

function getItbEngineUrl() {
  if (!process.env.GZL_TDL_ENGINE_URL) {
    throw new Error("GZL_TDL_ENGINE_URL is not defined");
  }
  return process.env.GZL_TDL_ENGINE_URL;
}

function buildFormDataRequestBody(mlangFile: string): FormData {
  const body = new FormData();
  body.append("mlang_file", mlangFile);
  return body;
}

export async function sendTestToItbEngine(testCaseName: string): Promise<ServerActionError> {
  const mlangContent = await readMlangFileFromTestCaseName(testCaseName);
  const itbUrl = getItbEngineUrl();
  const requestBody = buildFormDataRequestBody(mlangContent);

  const requestOptions = {
    method: "POST",
    body: requestBody,
  };

  console.warn("Sending request to Robot engine.");
  return await fetch(`${itbUrl}/run_from_mlang`, requestOptions)
    .then(async (response) => {
      if (!response.ok) {
        console.error(`Error: ${response.status} ${response.statusText}`);
        return { error: "Error sending test to Robot engine, response not ok - " + response.statusText };
      }
      return {};
    })
    .catch((error) => {
      console.error("Error POST robot engine:", error);
      return { error: "Error sending test to robot engine - " + error };
    });
}
