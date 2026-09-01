import { AccessControlList } from "@/shared/types/AccessControlListTypes";
import { SystemUnderTest } from "@maestro/types/report/TestReport";

export interface TestSuiteRun {
  testSuite: TestSuite;
  tests?: Test[];
  inputs?: Property[]
  systemsUnderTest?: SystemUnderTest[];
  accessControlList?: AccessControlList;
}

export interface TestRun {
  test: Test;
  inputs?: Property[]
  systemsUnderTest?: SystemUnderTest[];
  accessControlList?: AccessControlList;
}

export interface TestSuite {
  id: string;
  name: string;
  testReferences: TestReference[];
  supportedInputs?: SupportedInput[];
}

export interface Test {
  id: string;
  name: string
  supportedInputs?: SupportedInput[];
  steps: Step[];
}

export interface Step {
  name: string;
  type: string;
  timeout?: number;
  properties?: Property[];
}

export interface TestReference {
  testId: string;
  properties?: Property[];
}

export interface SupportedInput {
  id: string;
  type: InputType;
  label: string;
  groupName: string;
  description: string;
  required: boolean;
  defaultValue?: string;
  possibleValues?: string[];
}

export interface Property {
  name: string;
  type: PropertyType;
  value: string;
}

export type PropertyType = "STRING" | "BYTE_ARRAY" | "BOOLEAN" | "INTEGER" | "DATE" | "FLOAT";

export type InputType = "TEXT" | "VALUE_SET" | "FILE" | "BOOLEAN";

// ---- Helpers ----

export function createProperty(name: string, value: string, type: PropertyType): Property {
  return {name, value, type};
}

export function addOrUpdateProperty(step: Step, property: Property): Step {
  const properties = step.properties ? [...step.properties] : [];
  const index = properties.findIndex(p => p.name === property.name);

  let updatedProperties: Property[];

  if (property.value.length === 0) {
    updatedProperties = properties.filter(p => p.name !== property.name);
  } else if (index === -1) {
    updatedProperties = [...properties, property];
  } else {
    updatedProperties = properties.map((p, i) => (i === index ? property : p));
  }

  return {...step, properties: updatedProperties};
}