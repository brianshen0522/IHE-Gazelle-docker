"use server";
import { readMlangFileFromTestCaseName } from "@test-execution/services/testCaseJSONProvider";

export type ServerActionError = {
  error?: string;
};

function getRobotEngineUrl() {
  if (!process.env.GZL_ROBOT_ENGINE_URL) {
    throw new Error("GZL_ROBOT_ENGINE_URL is not defined");
  }
  return process.env.GZL_ROBOT_ENGINE_URL;
}

function buildFormDataRequestBody(mlangFile: string): FormData {
  const body = new FormData();
  body.append("mlang_file", mlangFile);
  return body;
}

export async function sendTestToRobotEngine(testCaseName: string): Promise<ServerActionError> {
  const mlangContent = await readMlangFileFromTestCaseName(testCaseName);
  const robotUrl = getRobotEngineUrl();
  const requestBody = buildFormDataRequestBody(mlangContent);

  const requestOptions = {
    method: "POST",
    body: requestBody,
  };

  console.warn("Sending request to Robot engine.");
  return await fetch(`${robotUrl}/run_from_mlang`, requestOptions)
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
