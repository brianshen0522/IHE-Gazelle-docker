"use server";

import { getServerSession } from "next-auth";
import { authOptions } from "@shared/components/auth/authOptions";
import axios from "axios";

export async function createTestRunExecution(data: {
  testSessionId: string;
  testId: string;
  testSpecification: string;
  accessControlList?: {
    owners?: string[];
    readers?: string[];
    editors?: string[];
    isPublic?: boolean;
    readAccessKey?: string;
  };
}) {
  try {
    const session = await getServerSession(authOptions);
    const accessToken = session?.access_token;

    if (!accessToken) {
      return { success: false, error: "Unauthorized" };
    }

    const { testId, testSessionId, testSpecification, accessControlList } = data;

    if (!testId || !testSessionId) {
      return { success: false, error: "testId and testSessionId are required" };
    }

    const payload = {
      testId,
      testSessionId,
      testSpecification,
      accessControlList: accessControlList || {
        owners: [session.user?.gazelleId],
        readers: [],
        editors: [],
        isPublic: false,
        readAccessKey: "",
      },
    };

    const response = await axios.post(`${process.env.GZL_TEST_EXECUTION_URL}/test-run-executions`, payload, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
    });

    return { success: true, data: response.data };
  } catch (error) {
    console.error("Error creating test run execution:", error);
    if (axios.isAxiosError(error)) {
      const errorMessage = error.response?.data?.message || error.message || "Failed to create test run execution";
      return { success: false, error: errorMessage };
    }
    return { success: false, error: "Failed to create test run execution" };
  }
}

export async function updateTestRunExecutionAcl(data: {
  executionId: string;
  accessControlList: {
    owners?: string[];
    readers?: string[];
    editors?: string[];
    isPublic?: boolean;
    readAccessKey?: string;
  };
}) {
  try {
    const session = await getServerSession(authOptions);
    const accessToken = session?.access_token;

    if (!accessToken) {
      return { success: false, error: "Unauthorized" };
    }

    const { executionId, accessControlList } = data;

    if (!executionId) {
      return { success: false, error: "executionId is required" };
    }

    const response = await fetch(`${process.env.GZL_TEST_EXECUTION_URL}/test-run-executions/${executionId}`, {
      method: "PATCH",
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ accessControlList }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: "Failed to update ACL" }));
      const errorMessage = errorData.message || "Failed to update ACL";
      return { success: false, error: errorMessage };
    }

    // Handle empty or non-JSON responses (204 No Content, etc.)
    const text = await response.text();
    const responseData = text ? JSON.parse(text) : { accessControlList };
    return { success: true, data: responseData };
  } catch (error) {
    console.error("Error updating test run execution ACL:", error);
    const errorMessage = error instanceof Error ? error.message : "Failed to update ACL";
    return { success: false, error: errorMessage };
  }
}
