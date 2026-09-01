import {
  Property,
  isByteArrayItemProperty,
  isByteArrayProperty,
  isStringProperty,
  isBooleanProperty,
  ByteArrayItemProperty,
  ByteArrayProperty,
} from "@maestro/types/report/Property";
import { DatahouseItemReference } from "@shared/types/datahouse/DatahouseItem";
import { AttachmentRenderer } from "./AttachmentRenderer";
import { handleDownloadItem, getTypeFromReferences } from "./utils/propertyUtils";

interface PropertyRendererProps {
  /** The property to render */
  property: Property;
  /** Default name if property.name is empty */
  defaultName: string;
  /** Whether this is an input (affects rendering of inline values) */
  isInput: boolean;
  /** Current report/item ID */
  itemId: string;
  /** Datahouse item references for type lookup */
  references: DatahouseItemReference[];
}

/**
 * Renders a ByteArrayItemProperty (datahouse item references or attachments)
 */
function ByteArrayItemPropertyRenderer({
  property,
  defaultName,
  itemId,
  references,
}: Readonly<{
  property: ByteArrayItemProperty;
  defaultName: string;
  itemId: string;
  references: DatahouseItemReference[];
}>) {
  const itemName = property.fileName || property.name || defaultName;

  // Handle attachment or datahouse references using unified renderer
  if (property.reference?.includes("attachments") || property.reference?.includes("datahouse")) {
    return (
      <AttachmentRenderer
        name={itemName}
        reference={property.reference}
        mimeType={property.mimeType}
        itemType={property.itemType}
        itemId={itemId}
        references={references}
      />
    );
  }

  // Handle inline content (value present)
  if (property.value) {
    const mimeType = property.mimeType || getTypeFromReferences(itemId, references);
    return (
      <button
        type="button"
        className="text-blue align-middle"
        title="Download file"
        onClick={() => handleDownloadItem(property.value!, itemName, mimeType)}
      >
        {itemName}
      </button>
    );
  }

  // No reference or value
  return <span className="text-grey-500">{itemName}</span>;
}

/**
 * Renders a ByteArrayProperty (inline file content)
 */
function ByteArrayPropertyRenderer({ property, defaultName }: Readonly<{ property: ByteArrayProperty; defaultName: string }>) {
  const itemName = property.fileName || property.name || defaultName;

  if (property.value) {
    const mimeType = property.mimeType || "application/octet-stream";
    return (
      <button
        type="button"
        className="text-blue align-middle"
        title="Download file"
        onClick={() => handleDownloadItem(property.value, itemName, mimeType)}
      >
        {itemName}
      </button>
    );
  }

  return <span className="text-grey-500">{itemName}</span>;
}

/**
 * Renders inline property values (STRING, BOOLEAN, etc.)
 * Used for inputs that should be displayed inline
 */
export function InlinePropertyValueRenderer({ property }: Readonly<{ property: Property }>) {
  if (isStringProperty(property)) {
    return (
      <>
        <span className="font-semibold">{property.name}: </span>
        <span className="font-mono break-all bg-grey-100 p-1 rounded-small text-[13px]">{property.value}</span>
      </>
    );
  }

  if (isBooleanProperty(property)) {
    return (
      <>
        <span className="font-semibold">{property.name}: </span>
        <span className="font-mono break-all bg-grey-100 p-1 rounded-small text-[13px]">{property.value ? "true" : "false"}</span>
      </>
    );
  }

  // For other property types (INTEGER, FLOAT, DATE), display as string
  if ("value" in property && property.value !== undefined && property.value !== null) {
    return (
      <>
        <span className="font-semibold">{property.name}: </span>
        <span className="font-mono break-all bg-grey-100 p-1 rounded-small text-[13px]">{String(property.value)}</span>
      </>
    );
  }

  return null;
}

/**
 * Main component that renders any property type as a list item
 *
 * Handles:
 * - ByteArrayItemProperty (datahouse items, attachments)
 * - ByteArrayProperty (inline files)
 * - StringProperty, BooleanProperty, etc. (inline values for inputs)
 *
 * @example
 * ```tsx
 * {properties.map((prop, index) => (
 *   <PropertyRenderer
 *     key={index}
 *     property={prop}
 *     defaultName={`Item ${index + 1}`}
 *     isInput={false}
 *     itemId={reportId}
 *     references={references}
 *   />
 * ))}
 * ```
 */
export function PropertyRenderer({ property, defaultName, itemId, references }: Readonly<PropertyRendererProps>) {
  // ByteArrayItemProperty - can be report references or attachments
  if (isByteArrayItemProperty(property)) {
    return (
      <li key={property.name} className="list-item ml-5">
        <ByteArrayItemPropertyRenderer property={property} defaultName={defaultName} itemId={itemId} references={references} />
      </li>
    );
  }

  // ByteArrayProperty - inline file content
  if (isByteArrayProperty(property)) {
    return (
      <li key={property.name} className="list-item ml-5">
        <ByteArrayPropertyRenderer property={property} defaultName={defaultName} />
      </li>
    );
  }

  // For render inline values (STRING, BOOLEAN, etc.)
  if (isStringProperty(property) || isBooleanProperty(property)) {
    const inlineContent = <InlinePropertyValueRenderer property={property} />;
    if (inlineContent) {
      return (
        <li key={property.name} className="list-item ml-5">
          {inlineContent}
        </li>
      );
    }
  }

  // Fallback - display name only
  return (
    <li key={property.name} className="list-item ml-5">
      <span className="text-grey-500">{property.name || defaultName}</span>
    </li>
  );
}
