import { CapabilitiesDisplay } from "./CapabilitiesDisplay";
import { Capability } from "../../types/TestCase";

interface TestRoleCardProps {
  name: string;
  capabilities: Capability;
  isExpanded: boolean
}

/**
 * Displays a test role with its capabilities in an expandable card
 */
export const TestRoleCard = ({ name, capabilities, isExpanded }: TestRoleCardProps) => {
  return (
    <div className={`flex flex-row pl-2 ${isExpanded ? 'mb-4' : ''}`}>
       <span className="text-medium">• {name}</span>
      {isExpanded && (
        <ul className={`pl-6 items-center list-disc list-outside [&_ul]:list-[revert]`}>
          <CapabilitiesDisplay capabilities={capabilities} isExpanded={isExpanded} />
        </ul>
      )}
    </div>
  );
};

