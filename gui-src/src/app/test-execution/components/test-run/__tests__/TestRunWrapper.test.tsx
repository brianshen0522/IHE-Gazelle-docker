/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import TestRunWrapper from "../TestRunWrapper";
import { Session } from "next-auth";
import * as useEnvHook from "@/shared/hooks/useEnv";
import * as useGetTestRunExecutionsHook from "../../../hooks/SWR/useGetTestRunExecutions";
import * as useGetTestModelHook from "../../../hooks/SWR/useGetTestModel";
import * as useInputManagementHook from "@/shared/hooks/useInputManagement";
import * as useAutoCreateExecutionHook from "../../../hooks/useAutoCreateExecution";
import * as useTestExecutionHook from "../../../hooks/useTestExecution";
import * as useUnsavedChangesHook from "@/shared/context/UnsavedChangeContext";
import * as useTestSessionHook from "../../../context/TestSessionContext";

// Mock Next.js navigation
const mockPush = vi.fn();
const mockBack = vi.fn();
const mockSearchParams: Record<string, string> = {};

vi.mock("next/navigation", () => ({
  useRouter: () => ({
    push: mockPush,
    back: mockBack,
  }),
  useSearchParams: () => ({
    get: (key: string) => mockSearchParams[key] ?? null,
  }),
}));

// Mock components
vi.mock("../test-info/TestRun", () => ({
  default: ({ testModel, testSessionId }: any) => (
    <div data-testid="test-run-info">
      {testModel?.name} - {testSessionId}
    </div>
  ),
}));

vi.mock("../test-configuration/TestConfiguration", () => ({
  default: ({ currentExecution, onRun, isRunning }: any) => (
    <div data-testid="test-configuration">
      <span data-testid="current-execution-id">{currentExecution?.id}</span>
      <button data-testid="test-run" onClick={onRun} disabled={isRunning}>
        {isRunning ? "Running" : "Run"}
      </button>
    </div>
  ),
}));

vi.mock("../test-step/TestStepWrapper", () => ({
  default: ({ testStatus, isRunning }: any) => (
    <div data-testid="test-step-wrapper">
      <span data-testid="test-status">{testStatus}</span>
      <span data-testid="is-running">{isRunning ? "true" : "false"}</span>
    </div>
  ),
}));

vi.mock("../ExecutionCompletionModal", () => ({
  default: ({ isOpen, onClose, onBackToTestSuite }: any) => (
    <div data-testid="completion-modal" data-open={isOpen}>
      <button data-testid="close-modal" onClick={onClose}>
        Close
      </button>
      <button data-testid="back-to-suite" onClick={onBackToTestSuite}>
        Back to Suite
      </button>
    </div>
  ),
}));

vi.mock("../TestRunHeader", () => ({
  TestRunHeader: ({ testName }: any) => <div data-testid="test-run-header">{testName}</div>,
}));

vi.mock("@shared/services/RenderSanitizedHTML", () => ({
  default: ({ untrustedHTML }: any) => <div data-testid="sanitized-html">{untrustedHTML}</div>,
}));

vi.mock("@shared/components/errors/InternalError", () => ({
  default: ({ title, message }: any) => (
    <div data-testid="internal-error">
      <h1>{title}</h1>
      <p>{message}</p>
    </div>
  ),
}));

vi.mock("@gazelle/gazelle-component-ui", async (importOriginal) => {
  const actual = await importOriginal<typeof import("@gazelle/gazelle-component-ui")>();

  return {
    ...actual,
    Skeleton: () => <div data-testid="skeleton" />,
    Button: ({ id, onClick, disabled, children }: any) => (
      <button data-testid={id} onClick={onClick} disabled={disabled}>
        {children}
      </button>
    ),
    CollapsableCard: ({ title, children }: any) => (
      <div data-testid="collapsable-card">
        <h2>{title}</h2>
        {children}
      </div>
    ),
    Input: ({ id, value, setValue, placeholder }: any) => (
      <input data-testid={`input-${id}`} value={value} onChange={(e) => setValue(e.target.value)} placeholder={placeholder} />
    ),
    NoticeBanner: ({ children }: any) => <div data-testid="notice-banner">{children}</div>,
    UploadFile: ({ id, fileName, onFileChange }: any) => (
      <div data-testid={`upload-file-${id}`}>
        <span data-testid={`file-name-${id}`}>{fileName}</span>
        <input data-testid={`file-input-${id}`} type="file" onChange={(e) => onFileChange(e.target.files?.[0] || null)} />
      </div>
    ),
  };
});

describe("TestRunWrapper", () => {
  const mockSession: Session = {
    access_token: "test-token",
    user: {
      gazelleId: "user-1",
      organization: "org-1",
    },
    expires: "2026-12-31",
  } as Session;

  const mockTestModel = {
    id: "test-1",
    name: "Test Model",
    summary: "Test summary",
    description: "Test description",
    tags: [],
    testRoles: [],
    supportedInputs: [{ id: "input-1", type: "TEXT", label: "Input 1", required: true }],
    steps: [{ name: "Step 1", type: "ACTION" }],
  };

  const mockExecution = {
    id: "exec-1",
    testId: "test-1",
    testSessionId: "session-1",
    testSuiteId: "suite-1",
    testName: "Test Execution",
    testSpecification: JSON.stringify(mockTestModel),
    status: "NOT_STARTED",
    startedAt: "2026-01-01",
    executedBy: "user-1",
    inputs: [],
  };

  const mockEnv = {
    BASE_URL: "http://localhost:3000",
    FQDN: "localhost",
    GZL_MAESTRO_WEBSOCKET_URL: "ws://localhost:8080",
    GZL_TEST_EXECUTION_WEBSOCKET_URL: "ws://localhost:8080",
    GZL_SIMULATION_REQUEST_TIMEOUT_SECONDS: "30",
    GZL_DTH_API_URL: "http://localhost:8080",
    GZL_REGISTRATION_URL: "http://localhost:8080",
    GZL_TEST_BED_TITLE: "Test Bed",
    KC_LOGIN_URL: "http://localhost:9000",
    KEYCLOAK_ISSUER: "http://localhost:9000",
    KC_CLIENT_ID: "test-client",
    GZL_TM_URL: "http://localhost:8080",
    GZL_DTH_VALIDATION: "true",
  };

  const mockUseEnv = vi.spyOn(useEnvHook, "useEnv");
  const mockUseGetTestRunExecutions = vi.spyOn(useGetTestRunExecutionsHook, "useGetTestRunExecutions");
  const mockUseGetTestModel = vi.spyOn(useGetTestModelHook, "useGetTestModel");
  const mockUseInputManagement = vi.spyOn(useInputManagementHook, "useInputManagement");
  const mockUseAutoCreateExecution = vi.spyOn(useAutoCreateExecutionHook, "useAutoCreateExecution");
  const mockUseTestExecution = vi.spyOn(useTestExecutionHook, "useTestExecution");
  const mockUseUnsavedChanges = vi.spyOn(useUnsavedChangesHook, "useUnsavedChanges");
  const mockUseTestSession = vi.spyOn(useTestSessionHook, "useTestSession");
  const mockSetHasUnsavedChanges = vi.fn();
  const mockSetTestSessionData = vi.fn();
  const mockStartExecution = vi.fn();
  const mockSetExecution = vi.fn();
  const mockHandleCloseModal = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    Object.keys(mockSearchParams).forEach((key) => delete mockSearchParams[key]);

    // Default mock implementations
    mockUseEnv.mockReturnValue({ env: mockEnv, envLoading: false });
    mockUseUnsavedChanges.mockReturnValue({ setHasUnsavedChanges: mockSetHasUnsavedChanges } as any);
    mockUseTestSession.mockReturnValue({
      setTestSessionData: mockSetTestSessionData,
      testSessionId: "session-1",
    } as any);

    mockUseInputManagement.mockReturnValue({
      uploadedInputs: [],
      updateInput: vi.fn(),
      areRequiredInputsFilled: true,
      clearInputs: vi.fn(),
      getInputValue: vi.fn(),
    });

    mockUseAutoCreateExecution.mockReturnValue({
      isCreatingExecution: false,
    });

    mockUseTestExecution.mockReturnValue({
      execution: null,
      setExecution: mockSetExecution,
      isRunning: false,
      interactWithUser: undefined,
      startExecution: mockStartExecution,
      disconnect: vi.fn(),
      completeUserInteraction: vi.fn(),
      isCompletionModalOpen: false,
      handleCloseModal: mockHandleCloseModal,
    });
  });

  describe("Loading States", () => {
    it("shows skeletons when loading execution", () => {
      mockUseGetTestRunExecutions.mockReturnValue({
        data: undefined,
        isLoading: true,
        isError: false,
      } as any);
      mockUseGetTestModel.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: false,
      } as any);

      render(<TestRunWrapper params={{ executionId: "exec-1" }} session={mockSession} />);

      expect(screen.getAllByTestId("skeleton")).toHaveLength(4);
    });

    it("shows skeletons when loading test model", () => {
      mockSearchParams["testId"] = "test-1";
      mockUseGetTestRunExecutions.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: false,
      } as any);
      mockUseGetTestModel.mockReturnValue({
        data: undefined,
        isLoading: true,
        isError: false,
      } as any);

      render(<TestRunWrapper params={{}} session={mockSession} />);

      expect(screen.getAllByTestId("skeleton")).toHaveLength(4);
    });

    it("shows skeletons when creating execution", () => {
      mockUseGetTestRunExecutions.mockReturnValue({
        data: mockExecution,
        isLoading: false,
        isError: false,
      } as any);
      mockUseGetTestModel.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: false,
      } as any);
      mockUseAutoCreateExecution.mockReturnValue({
        isCreatingExecution: true,
      });

      render(<TestRunWrapper params={{ executionId: "exec-1" }} session={mockSession} />);

      expect(screen.getAllByTestId("skeleton")).toHaveLength(4);
    });
  });

  describe("Error States", () => {
    it("shows error when execution fetch fails", () => {
      mockUseGetTestRunExecutions.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: { message: "Failed to fetch execution" },
      } as any);
      mockUseGetTestModel.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: false,
      } as any);

      render(<TestRunWrapper params={{ executionId: "exec-1" }} session={mockSession} />);

      expect(screen.getByTestId("internal-error")).toBeInTheDocument();
      expect(screen.getByText("Error loading test")).toBeInTheDocument();
    });

    it("shows error when test model fetch fails", () => {
      mockSearchParams["testId"] = "test-1";
      mockUseGetTestRunExecutions.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: false,
      } as any);
      mockUseGetTestModel.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: { message: "Failed to fetch test model" },
      } as any);

      render(<TestRunWrapper params={{}} session={mockSession} />);

      expect(screen.getByTestId("internal-error")).toBeInTheDocument();
      expect(screen.getByText("Error loading test")).toBeInTheDocument();
    });

    it("shows error when no data is available", () => {
      mockUseGetTestRunExecutions.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: false,
      } as any);
      mockUseGetTestModel.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: false,
      } as any);

      render(<TestRunWrapper params={{ executionId: "exec-1" }} session={mockSession} />);

      expect(screen.getByTestId("internal-error")).toBeInTheDocument();
      expect(screen.getByText("No test data found")).toBeInTheDocument();
    });
  });

  describe("Execution Flow (with executionId)", () => {
    it("renders execution successfully", () => {
      mockUseGetTestRunExecutions.mockReturnValue({
        data: mockExecution,
        isLoading: false,
        isError: false,
      } as any);
      mockUseGetTestModel.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: false,
      } as any);

      render(<TestRunWrapper params={{ executionId: "exec-1" }} session={mockSession} />);

      expect(screen.getByTestId("test-run-header")).toBeInTheDocument();
      expect(screen.getByText("Test Execution")).toBeInTheDocument();
      expect(screen.getByTestId("test-configuration")).toBeInTheDocument();
      expect(screen.getByTestId("test-step-wrapper")).toBeInTheDocument();
    });

    it("displays description when available", () => {
      mockUseGetTestRunExecutions.mockReturnValue({
        data: mockExecution,
        isLoading: false,
        isError: false,
      } as any);
      mockUseGetTestModel.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: false,
      } as any);

      render(<TestRunWrapper params={{ executionId: "exec-1" }} session={mockSession} />);

      expect(screen.getByTestId("sanitized-html")).toBeInTheDocument();
      expect(screen.getByText("Test description")).toBeInTheDocument();
    });

    it("uses execution from WebSocket when available", () => {
      const wsExecution = { ...mockExecution, id: "ws-exec-1", status: "IN_PROGRESS" as const };
      mockUseGetTestRunExecutions.mockReturnValue({
        data: mockExecution,
        isLoading: false,
        isError: false,
      } as any);
      mockUseGetTestModel.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: false,
      } as any);
      mockUseTestExecution.mockReturnValue({
        execution: wsExecution,
        setExecution: mockSetExecution,
        isRunning: false,
        interactWithUser: undefined,
        startExecution: mockStartExecution,
        disconnect: vi.fn(),
        completeUserInteraction: vi.fn(),
        isCompletionModalOpen: false,
        handleCloseModal: mockHandleCloseModal,
      });

      render(<TestRunWrapper params={{ executionId: "exec-1" }} session={mockSession} />);

      expect(screen.getByTestId("current-execution-id")).toHaveTextContent("ws-exec-1");
    });
  });

  describe("Test Model Flow (with testId)", () => {
    it("renders test model successfully", () => {
      mockSearchParams["testId"] = "test-1";
      mockUseGetTestRunExecutions.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: false,
      } as any);
      mockUseGetTestModel.mockReturnValue({
        data: mockTestModel,
        isLoading: false,
        isError: false,
      } as any);

      render(<TestRunWrapper params={{}} session={mockSession} />);

      expect(screen.getByTestId("test-run-header")).toBeInTheDocument();
      expect(screen.getByText("Test Model")).toBeInTheDocument();
    });
  });

  describe("Test Execution", () => {
    it("calls startExecution when run button is clicked", () => {
      mockUseGetTestRunExecutions.mockReturnValue({
        data: mockExecution,
        isLoading: false,
        isError: false,
      } as any);
      mockUseGetTestModel.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: false,
      } as any);

      render(<TestRunWrapper params={{ executionId: "exec-1" }} session={mockSession} />);

      const runButton = screen.getByTestId("test-run");
      fireEvent.click(runButton);

      expect(mockStartExecution).toHaveBeenCalled();
    });
  });

  describe("Modal Handling", () => {
    it("displays completion modal when open", () => {
      mockUseGetTestRunExecutions.mockReturnValue({
        data: mockExecution,
        isLoading: false,
        isError: false,
      } as any);
      mockUseGetTestModel.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: false,
      } as any);
      mockUseTestExecution.mockReturnValue({
        execution: null,
        setExecution: mockSetExecution,
        isRunning: false,
        interactWithUser: undefined,
        startExecution: mockStartExecution,
        disconnect: vi.fn(),
        completeUserInteraction: vi.fn(),
        isCompletionModalOpen: true,
        handleCloseModal: mockHandleCloseModal,
      });

      render(<TestRunWrapper params={{ executionId: "exec-1" }} session={mockSession} />);

      const modal = screen.getByTestId("completion-modal");
      expect(modal).toHaveAttribute("data-open", "true");
    });

    it("closes modal when close button is clicked", () => {
      mockUseGetTestRunExecutions.mockReturnValue({
        data: mockExecution,
        isLoading: false,
        isError: false,
      } as any);
      mockUseGetTestModel.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: false,
      } as any);
      mockUseTestExecution.mockReturnValue({
        execution: null,
        setExecution: mockSetExecution,
        isRunning: false,
        interactWithUser: undefined,
        startExecution: mockStartExecution,
        disconnect: vi.fn(),
        completeUserInteraction: vi.fn(),
        isCompletionModalOpen: true,
        handleCloseModal: mockHandleCloseModal,
      });

      render(<TestRunWrapper params={{ executionId: "exec-1" }} session={mockSession} />);

      const closeButton = screen.getByTestId("close-modal");
      fireEvent.click(closeButton);

      expect(mockHandleCloseModal).toHaveBeenCalled();
    });

    it("navigates to test suite when back button is clicked", () => {
      mockUseGetTestRunExecutions.mockReturnValue({
        data: mockExecution,
        isLoading: false,
        isError: false,
      } as any);
      mockUseGetTestModel.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: false,
      } as any);
      mockUseTestExecution.mockReturnValue({
        execution: null,
        setExecution: mockSetExecution,
        isRunning: false,
        interactWithUser: undefined,
        startExecution: mockStartExecution,
        disconnect: vi.fn(),
        completeUserInteraction: vi.fn(),
        isCompletionModalOpen: true,
        handleCloseModal: mockHandleCloseModal,
      });

      render(<TestRunWrapper params={{ executionId: "exec-1" }} session={mockSession} />);

      const backButton = screen.getByTestId("back-to-suite");
      fireEvent.click(backButton);

      expect(mockHandleCloseModal).toHaveBeenCalled();
      expect(mockBack).toHaveBeenCalled();
    });
  });

  describe("Context Synchronization", () => {
    it("syncs URL params with context on mount", () => {
      mockSearchParams["testId"] = "test-1";
      mockUseGetTestRunExecutions.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: false,
      } as any);
      mockUseGetTestModel.mockReturnValue({
        data: mockTestModel,
        isLoading: false,
        isError: false,
      } as any);

      render(<TestRunWrapper params={{}} session={mockSession} />);

      expect(mockSetTestSessionData).toHaveBeenCalledWith({
        testSessionId: "session-1",
        testId: "test-1",
      });
    });

    it("syncs execution from API to WebSocket hook", async () => {
      mockUseGetTestRunExecutions.mockReturnValue({
        data: mockExecution,
        isLoading: false,
        isError: false,
      } as any);
      mockUseGetTestModel.mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: false,
      } as any);

      render(<TestRunWrapper params={{ executionId: "exec-1" }} session={mockSession} />);

      await waitFor(() => {
        expect(mockSetExecution).toHaveBeenCalledWith(mockExecution);
      });
    });
  });
});
