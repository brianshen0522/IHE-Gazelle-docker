"use client";
import { useEffect, useState } from "react";
import { useEnv } from "@/shared/hooks/useEnv";
import { useSearchParams, useRouter } from "next/navigation";
import { Session } from "next-auth";
import { Skeleton, CollapsableCard } from "@gazelle/gazelle-component-ui";
import InternalErrors from "@/shared/components/errors/InternalError";
import { useGetTestRunExecutions } from "../../hooks/SWR/useGetTestRunExecutions";
import { useGetTestModel } from "../../hooks/SWR/useGetTestModel";
import { useInputManagement } from "@/shared/hooks/useInputManagement";
import { useAutoCreateExecution } from "../../hooks/useAutoCreateExecution";
import { useTestExecution } from "../../hooks/useTestExecution";
import TestRun from "./test-info/TestRun";
import TestConfiguration from "./test-configuration/TestConfiguration";
import TestStepWrapper from "./test-step/TestStepWrapper";
import ExecutionCompletionModal from "./ExecutionCompletionModal";
import { useUnsavedChanges } from "@/shared/context/UnsavedChangeContext";
import { TestRunHeader } from "./TestRunHeader";
import RenderSanitizedHTML from "@shared/services/RenderSanitizedHTML";
import { useTestSession } from "../../context/TestSessionContext";
import TestParticipants from "./test-participants/TestParticipants";
import UserInteractionModal from "@maestro/components/UserInteractionModal";
import TestExecutionHistory from "./test-execution-history/TestExecutionHistory";
import { useTranslation } from "react-i18next";
import { LAST_TEST_SUITE_URL_KEY } from "@/shared/components/layout/GazelleRootLayout";
import {Route} from "next";

interface TestRunWrapperProps {
  params: { executionId?: string };
  session: Session | null;
}

export default function TestRunWrapper({ params, session }: Readonly<TestRunWrapperProps>) {
  const { t } = useTranslation();
  const { env } = useEnv();
  const router = useRouter();
  const searchParams = useSearchParams();
  const { setHasUnsavedChanges } = useUnsavedChanges();
  const { setTestSessionData, testSessionId } = useTestSession();

  // Get params from URL
  const executionId = params.executionId ?? searchParams.get("executionId") ?? "";
  const testId = searchParams.get("testId") ?? "";

  // Sync URL params with context
  useEffect(() => {
    setTestSessionData({
      testSessionId,
      testId,
    });
  }, [executionId, testId, testSessionId, setTestSessionData]);

  // Determine which flow to use: with executionId or with testId
  const useTestModelFlow = !executionId;

  // Fetch test model (only when creating new execution from test model)
  const { data: testModel, isLoading: isLoadingTestModel, isError: isErrorTestModel } = useGetTestModel(useTestModelFlow ? testId || "" : "");

  // Fetch test run execution (when executionId is available)
  const { data: testRunExecution, isLoading, isError } = useGetTestRunExecutions(executionId);

  // Parse test from execution if it exists, otherwise use fetched test model
  const testFromExecution = testRunExecution?.testSpecification ? JSON.parse(testRunExecution.testSpecification) : null;
  const effectiveTest = testFromExecution || testModel;

  // Get test name from execution or test model
  const testName = testRunExecution?.testName ?? effectiveTest?.name;

  const { uploadedInputs, updateInput, areRequiredInputsFilled } = useInputManagement(
    effectiveTest?.supportedInputs,
    setHasUnsavedChanges,
    testRunExecution?.inputs,
  );

  // Auto-create execution when navigating from test suite
  const { isCreatingExecution } = useAutoCreateExecution({
    useTestModelFlow,
    testModel: effectiveTest,
    executionId,
    testId,
    testSessionId,
  });

  // Test execution via WebSocket
  const { execution, setExecution, isRunning, interactWithUser, startExecution, completeUserInteraction, isCompletionModalOpen, handleCloseModal } =
    useTestExecution({
      executionId,
      wsUrl: env?.GZL_TEST_EXECUTION_WEBSOCKET_URL,
      accessToken: session?.access_token,
      userInfo: {
        userId: session?.user?.gazelleId,
        organizationGroupId: session?.user?.organization,
      },
      testSteps: effectiveTest?.steps,
      supportedInputs: effectiveTest?.supportedInputs,
      uploadedInputs,
      onUnsavedChange: setHasUnsavedChanges,
    });

  // Sync initial data from API to WebSocket hook
  useEffect(() => {
    if (testRunExecution && !execution) {
      setExecution(testRunExecution);
    }
  }, [testRunExecution, execution, setExecution]);

  // Use execution from WebSocket if available, otherwise use API data
  const currentExecution = execution || testRunExecution;

  const currentTestSessionId = currentExecution?.testSessionId ?? testSessionId;

  // Get description from execution or test model
  const description = effectiveTest?.description;

  // Track test report summaries to force re-renders when they change
  const [reportSummaries, setReportSummaries] = useState<any[]>([]);
  useEffect(() => {
    const summaries = currentExecution?.testReportSummaries || [];
    setReportSummaries(summaries);
  }, [currentExecution?.testReportSummaries]);

  // Input handlers
  const handleFileChange = (inputId: string, fileName: string, fileContent: string | null) => {
    updateInput(inputId, fileContent);
  };

  const handleInputChange = (inputId: string, value: string) => {
    updateInput(inputId, value);
  };

  const handleRunExecution = () => {
    if (currentExecution) {
      // startExecution will connect WebSocket if not already connected
      startExecution();
    }
  };

  const handleBackToTestSuite = () => {
    handleCloseModal();
    const testSuiteUrl = sessionStorage.getItem(LAST_TEST_SUITE_URL_KEY);
    if (testSuiteUrl) {
      router.push(testSuiteUrl as Route);
    } else {
      router.back();
    }
  };

  const handleSeeDetails = () => {
    handleCloseModal();
  };

  // Loading state
  const skeletonKeys = Array.from({ length: 4 }, (_, i) => `skeleton-${i}`);
  if (isLoading || isLoadingTestModel || isCreatingExecution) {
    return (
      <div className="space-y-4">
        {skeletonKeys.map((key) => (
          <Skeleton key={key} className="h-60" />
        ))}
      </div>
    );
  }

  // Error states
  if (isError || isErrorTestModel) {
    const errorMessage = isError?.message ?? isErrorTestModel?.message ?? "Unknown error";
    return <InternalErrors title="Error loading test" message={`An error occurred while fetching the test data: ${errorMessage}`} />;
  }

  // For test model flow: show test model info while execution is being created
  // For execution flow: ensure we have execution data
  if (!currentExecution && !effectiveTest) {
    return (
      <InternalErrors
        title="No test data found"
        message="No test run execution or test model found. Please make sure you have the correct parameters."
      />
    );
  }

  return (
    <>
      <TestRunHeader testName={testName} />
      <div className="space-y-8">
        <CollapsableCard title="Test run execution">
          <TestRun testModel={effectiveTest} testSessionId={currentTestSessionId} testRun={currentExecution} />
        </CollapsableCard>

        {/* TMP: title is test roles while waiting for participants feature */}
        {effectiveTest?.testRoles.length > 0 && (
          <CollapsableCard title="Test roles">
            <TestParticipants testModel={effectiveTest} />
          </CollapsableCard>
        )}

        {description && (
          <CollapsableCard title="Description">
            <RenderSanitizedHTML untrustedHTML={description} />
          </CollapsableCard>
        )}

        <TestConfiguration
          currentExecution={currentExecution}
          supportedInputs={effectiveTest?.supportedInputs}
          onFileChange={handleFileChange}
          onInputChange={handleInputChange}
          uploadedInputs={uploadedInputs}
          areRequiredInputsFilled={areRequiredInputsFilled}
          onRun={handleRunExecution}
          isRunning={isRunning}
        />

        {currentExecution && <TestStepWrapper testRun={currentExecution} testSteps={effectiveTest?.steps} isRunning={isRunning} />}

        {reportSummaries.length > 0 && (
          <CollapsableCard title={t("gzl.user.interface.test_exec_history")}>
            <TestExecutionHistory
              testReportSummaries={reportSummaries}
              testSessionId={currentTestSessionId}
              executionId={executionId}
            />
          </CollapsableCard>
        )}

        {interactWithUser && <UserInteractionModal interactWithUser={interactWithUser} onComplete={completeUserInteraction} />}

        <ExecutionCompletionModal
          isOpen={isCompletionModalOpen}
          execution={currentExecution}
          onClose={handleCloseModal}
          onBackToTestSuite={handleBackToTestSuite}
          onSeeDetails={handleSeeDetails}
        />
      </div>
    </>
  );
}
