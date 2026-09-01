import { ParameterType } from "@simulation-portal/types/ParameterType";

export interface SimulationSequence {
  id: string;
  version?: string;
  simulatorName?: string;
  simulatorVersion?: string;
  simulatorUrl?: string;
  valid: boolean;
  validReportMessage?: string;
  transactions?: string[];
  shortDescription?: string;
  description?: string;
  runnable?: boolean;
  standards?: string[];
  simulatedRoles: SimulatedRole[];
  testedRoles: TestedRole[];
  supportedParameters?: SupportedParameter[];
}

export interface SimulatedRole {
  name: string;
  type: string;
  configs?: Parameter[];
}

export interface TestedRole {
  name: string;
  type: string;
}

export interface SupportedParameter {
  name: string;
  groupName: string;
  description: string;
  type: string;
  required: boolean;
  defaultValue?: Option;
  options?: Option[];
  valueSetId?: string;
  error?: string;
}

export interface Option {
  value: string;
  label: string;
}

export interface Parameter {
  name: string;
  value: string;
  type: ParameterType;
}