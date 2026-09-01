import { JSX, PropsWithChildren, useState } from "react";
import { ChevronDown } from "lucide-react";

type CollapsableSubCardProps = {
  title: JSX.Element | string;
  fallbackText?: string;
  expanded?: boolean;
  className?: string;
};

const CollapsableSubCard = ({ title, fallbackText, className, expanded = true, children }: PropsWithChildren<CollapsableSubCardProps>) => {
  const [isCollapse, setIsCollapse] = useState(expanded);

  const getChildren = () => {
    return <div className={`${className}`}>{children ?? fallbackText}</div>;
  };

  return (
    <div className="w-full rounded-md border border-grey bg-white">
      <div className={`w-full ${isCollapse ? "rounded-t-md" : "rounded-md"} align-middle`}>
        <button
          className={`w-full flex flex-row gap-2 items-center p-1 hover:bg-lightpurple ${isCollapse ? "rounded-t-md" : "rounded-md"} pl-2 pb-2`}
          onClick={() => setIsCollapse((prevState) => !prevState)}
        >
          <div className={`transform transition-transform duration-300 ${isCollapse ? "rotate-0" : "-rotate-90"}`}>
            <ChevronDown />
          </div>
          {title}
        </button>
      </div>
      <div className={`grid transition-all duration-300 ease-in-out ${isCollapse ? "grid-rows-[1fr] opacity-100 p-2" : "grid-rows-[0fr] opacity-0"}`}>
        <div className="overflow-hidden">{getChildren()}</div>
      </div>
    </div>
  );
};

export default CollapsableSubCard;
