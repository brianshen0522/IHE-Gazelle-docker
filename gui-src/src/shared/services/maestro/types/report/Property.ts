/**
 * Base Property type that all property subtypes extend.
 * Matches the backend PropertyDTO structure.
 */
export interface BaseProperty {
  name: string;
  type: string;
}

/**
 * String property - contains a simple string value
 */
export interface StringProperty extends BaseProperty {
  type: "STRING";
  value: string;
}

/**
 * Boolean property - contains a boolean value
 */
export interface BooleanProperty extends BaseProperty {
  type: "BOOLEAN";
  value: boolean;
}

/**
 * Integer property - contains an integer value
 */
export interface IntegerProperty extends BaseProperty {
  type: "INTEGER";
  value: number;
}

/**
 * Float property - contains a float value
 */
export interface FloatProperty extends BaseProperty {
  type: "FLOAT";
  value: number;
}

/**
 * Date property - contains an ISO date string
 */
export interface DateProperty extends BaseProperty {
  type: "DATE";
  value: string;
}

/**
 * Byte array property - contains binary data as base64 encoded string
 * Used for inline file content
 */
export interface ByteArrayProperty extends BaseProperty {
  type: "BYTE_ARRAY";
  value: string; // base64 encoded
  fileName?: string;
  mimeType?: string;
}

/**
 * Byte array item property - references a datahouse item
 * Used for reports and files stored in datahouse
 */
export interface ByteArrayItemProperty extends BaseProperty {
  type: "BYTE_ARRAY_ITEM";
  value?: string | null; // base64 encoded or null if only reference exists
  fileName?: string;
  mimeType?: string;
  itemType?: string; // Type of the referenced item (e.g., "VALIDATION_REPORT", "SIMULATION_REPORT")
  reference?: string; // URL to datahouse item
}

/**
 * Union type of all property types
 */
export type Property =
  | StringProperty
  | BooleanProperty
  | IntegerProperty
  | FloatProperty
  | DateProperty
  | ByteArrayProperty
  | ByteArrayItemProperty;

/**
 * Type guard to check if a property is a ByteArrayItemProperty
 */
export function isByteArrayItemProperty(
  property: Property
): property is ByteArrayItemProperty {
  return property.type === "BYTE_ARRAY_ITEM";
}

/**
 * Type guard to check if a property is a ByteArrayProperty
 */
export function isByteArrayProperty(
  property: Property
): property is ByteArrayProperty {
  return property.type === "BYTE_ARRAY";
}

/**
 * Type guard to check if a property is a StringProperty
 */
export function isStringProperty(
  property: Property
): property is StringProperty {
  return property.type === "STRING";
}

/**
 * Type guard to check if a property is a BooleanProperty
 */
export function isBooleanProperty(
  property: Property
): property is BooleanProperty {
  return property.type === "BOOLEAN";
}
