import { Capability } from "../../types/TestCase";

interface CapabilitiesDisplayProps {
  capabilities: Capability;
  isExpanded: boolean;
  level?: number;
}

/**
 * Recursively displays capabilities with support for AND/OR operators
 * Supports expand/collapse for nested structures
 */
export const CapabilitiesDisplay = ({ capabilities, isExpanded, level = 0 }: CapabilitiesDisplayProps) => {
  const indentClass = level > 0 ? "ml-4" : "";

  // Single capability
  if ("capability" in capabilities) {
    return <li className={`text-small items-center gap-1 text-grey-600`}>{capabilities.capability}</li>;
  }

  // OR operator
  if ("or" in capabilities) {
    return (
      <li className="">
        <span className="text-small text-grey-800">One of</span>
        <ul className={"ml-2 pl-2 list-inside " + indentClass}>
          {isExpanded &&
            capabilities.or.map((cap) => (
              <CapabilitiesDisplay key={JSON.stringify(cap)} capabilities={cap} level={level + 1} isExpanded={isExpanded} />
            ))}
        </ul>
      </li>
    );
  }

  // AND operator
  if ("and" in capabilities) {
    return (
      <li>
        <span className="text-small text-grey-800 ">All of</span>
        <ul className={"ml-2 pl-2 list-inside " + indentClass}>
          {isExpanded &&
            capabilities.and.map((cap) => (
              <CapabilitiesDisplay key={JSON.stringify(cap)} capabilities={cap} level={level + 1} isExpanded={isExpanded} />
            ))}
        </ul>
      </li>
    );
  }

  return null;
};
