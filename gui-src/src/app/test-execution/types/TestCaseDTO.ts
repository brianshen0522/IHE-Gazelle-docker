import {Property, PropertyType, Test} from "@maestro/types/execution/TestSuiteRun";

export type TestCaseDTO = {
  name: string;
  description: string;
  inputs: InputDTO[];
  steps: TestStepDTO[];
};

export type TestStepDTO = {
  id: string;
  name: string;
  description: string;
  type: string;
  properties: Property[];
};

export type InputDTO = {
  key: string;
  label: string;
  format: string;
  type: PropertyType;
  required: boolean;
};

export function transformTestToTestRun(testCase: TestCaseDTO) : Test {
  return {
      id : testCase.name,
      name: testCase.name,
      steps: testCase.steps.map(step => ({
        id: step.id,
        name: step.name,
        type: step.type,
        properties: step.properties
      })),
  }
}