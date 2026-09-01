import {Capability} from "@test-execution/types/TestCase";


export interface TestRole {
  name: string;
  capabilities?: Capability;
}

export interface SupportedInput {
  id: string;
  type: string;
  label: string;
  required: boolean;
  defaultValue?: string;
  possibleValues?: string[] | PossibleValue[];
}

export type PossibleValue = {
  value: string;
  label: string;
};

export interface StepProperty {
  name: string;
  type: string;
  value: string;
}

export interface TestStep {
  name: string;
  type: string;
  properties?: StepProperty[];
  result?: string;
  description: string;
}

export interface TestModel {
  id: string;
  name: string;
  summary: string;
  description?: string;
  tags: string[];
  testRoles: TestRole[];
  supportedInputs?: SupportedInput[];
  steps?: TestStep[];
  version?: string;
}
