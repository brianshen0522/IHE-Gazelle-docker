"use server";
import { MONITOR, PROJECT_ADMIN, TESTING_SESSION_MANAGER } from "@/shared/types/GazelleRole";
import { Session } from "next-auth";
import { ExecuteValidationParams, ExecuteValidationResult, FileInput, MaestroInput, MaestroResponse, StepProperty } from "./types";

/**
 * Build Maestro request for validation execution
 */
function buildMaestroRequest(profileId: string, serviceName: string, fileInputs: FileInput[], session: Session) {
  const stepProperties: StepProperty[] = [
    { name: "validationService", type: "STRING", value: serviceName },
    { name: "validationProfile", type: "STRING", value: profileId },
  ];
  const owners = [session.user.gazelleId];
  const readers = [TESTING_SESSION_MANAGER, PROJECT_ADMIN, MONITOR];
  const organization = session?.user?.organization;

  if (organization) {
    readers.push(`org:${organization}`);
  }

  const inputs: MaestroInput[] = fileInputs.map((file, index) => {
    const inputName = `inputFile${index + 1}`;
    stepProperties.push({
      name: file.id,
      type: "BYTE_ARRAY",
      value: `\${${inputName}}`,
    });
    return {
      name: inputName,
      type: "BYTE_ARRAY",
      value: file.content,
      fileName: file.name,
    };
  });

  return {
    testRunId: `Validation: ${profileId}`,
    accessControlList: {
      owners: owners,
      readers: readers,
      editors: [],
      isPublic: false,
    },
    test: {
      id: `validation-${profileId}`,
      name: `Validation of ${profileId}`,
      steps: [{ name: "Validation Step", type: "VALIDATION", properties: stepProperties }],
    },
    inputs,
  };
}

/**
 * Extract report ID from Maestro response
 */
function extractReportId(result: MaestroResponse): string | null {
  // Check for validation report in stepRunReports outputs
  const outputs = result.testRunReports?.[0]?.stepRunReports?.[0]?.outputs;

  if (outputs) {
    const report = outputs.find((o) => o.itemType === "VALIDATION_REPORT" || o.name === "VALIDATION_REPORT");

    if (report?.reference) {
      return report.reference.split("/").pop() ?? null;
    }
  }

  // Check alternative location
  if (result.test?.steps?.[0]?.report?.location) {
    return result.test.steps[0].report.location.split("/").pop() ?? null;
  }

  return null;
}

/**
 * Server action to execute validation via Maestro
 */
export async function executeValidation({ profileId, serviceName, fileInputs, session }: ExecuteValidationParams): Promise<ExecuteValidationResult> {
  try {
    if (!session?.access_token) {
      return { error: "Authentication required" };
    }

    const maestroRequest = buildMaestroRequest(profileId, serviceName, fileInputs, session);

    const response = await fetch(`${process.env.GZL_MAESTRO_URL}/v1/test/run?persist=${process.env.GZL_MAESTRO_PERSIST ?? "true"}`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${session.access_token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(maestroRequest),
    });

    if (!response.ok) {
      const errorText = await response.text();

      // Try to parse error as JSON for structured error info
      let errorMessage = `Maestro execution failed (${response.status} ${response.statusText})`;

      try {
        const errorJson = JSON.parse(errorText);
        // Extract meaningful error information from JSON response
        const details = errorJson.message || errorJson.error || errorJson.detail || errorText;
        errorMessage += `: ${details}`;
      } catch {
        // If not JSON, use raw text
        if (errorText) {
          errorMessage += `: ${errorText}`;
        }
      }

      console.error("Maestro error details:", {
        status: response.status,
        statusText: response.statusText,
        body: errorText,
      });

      return { error: errorMessage };
    }

    const responseText = await response.text();
    if (!responseText) {
      return { error: "Empty response from Maestro" };
    }

    const result = JSON.parse(responseText);

    // Try to extract report ID first
    const reportId = extractReportId(result);

    if (reportId) {
      return { reportId };
    }

    // Standalone mode (no report persistence): the validation report is returned inline (base64)
    const inlineOutputs = result.testRunReports?.[0]?.stepRunReports?.[0]?.outputs as
      | { name?: string; itemType?: string; value?: string }[]
      | undefined;
    const inlineReportOutput = inlineOutputs?.find((o) => o.itemType === "VALIDATION_REPORT" || o.name === "VALIDATION_REPORT");
    if (inlineReportOutput?.value) {
      try {
        const decoded = Buffer.from(inlineReportOutput.value, "base64").toString("utf-8");
        return { inlineReport: JSON.parse(decoded) };
      } catch {
        // fall through to error handling below
      }
    }

    // If no report, check for unexpected errors to provide details
    const unexpectedErrors = result.testRunReports?.[0]?.stepRunReports?.[0]?.unexpectedErrors;
    if (unexpectedErrors && unexpectedErrors.length > 0) {
      const errorMessages = unexpectedErrors.map((err: { name: string; message: string }) => `${err.name}: ${err.message}`).join("; ");
      return { error: `Validation failed with errors: ${errorMessages}` };
    }

    return { error: "No report generated" };
  } catch (error) {
    console.error("Validation execution error:", error);
    return {
      error: error instanceof Error ? error.message : "Unknown error occurred",
    };
  }
}
