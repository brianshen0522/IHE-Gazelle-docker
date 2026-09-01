// Test structure from backend
export interface Test {
  id?: string; // testSuiteId - added by API normalization
  testSessionId: string;
  testId: string;
  name: string;
  summary: string;
  tags: string[];
  testRoles: TestRole[];
  inScope: boolean;
  version: string;
}

/**
 * Represents a single capability string
 */
interface SingleCapability {
  capability: string;
}

/**
 * Represents a logical OR operation on capabilities
 */
interface OrCapability {
  or: Capability[];
}

/**
 * Represents a logical AND operation on capabilities
 */
interface AndCapability {
  and: Capability[];
}

/**
 * Recursive capability type that supports:
 * - Single capability: { "capability": "MHD/*" }
 * - OR operation: { "or": [...] }
 * - AND operation: { "and": [...] }
 */
export type Capability = SingleCapability | OrCapability | AndCapability;

interface TestRole {
  name: string;
  capabilities: Capability;
}