import { StepResult, TestResult } from "@maestro/types/report/Result";
import { UnexpectedError } from "@maestro/types/report/UnexpectedError";
import { ParameterType } from "@simulation-portal/types/ParameterType";

export interface SimulationReport {
  uuid: string;
  dateTime: string;
  result: TestResult;
  serviceName: string;
  serviceVersion: string;
  sequenceId: string;
  transactionReports: TransactionReport[];
  simulationParameters: Parameter[];
  unexpectedErrors: UnexpectedError[];
}

export interface TransactionReport {
  result: StepResult;
  transaction: string;
  standards: string[];
  note?: string;
  initiator: Role;
  responder: Role;
  messages: Message[];
  validationReports: ValidationReport[];
  unexpectedErrors: UnexpectedError[];
}

export interface ValidationReport {
  validationProfileId: string;
  validationResult: StepResult;
  subject: string;
  content?: string;      // Optional - inline validation report content (JSON string)
  reference?: string;    // Optional - URL to datahouse item (e.g., "http://datahouse/rest/v1/items/{itemId}")
                         // Should be null/undefined if not stored, NOT a placeholder like "ref"
  itemType?: string;     // Type of referenced item (should be "VALIDATION_REPORT" when reference exists)
}

export interface Role {
  name: string;
  configs: Parameter[];
  simulated: boolean;
}

export interface Message {
  name: string;
  dateTime: string;
  content?: string;      // Optional - may be null when reference exists
  reference?: string;    // Optional - URL to datahouse item
  sender: string;
  receiver: string;
}

export interface Parameter {
  name: string;
  value: string;
  type: ParameterType;
}