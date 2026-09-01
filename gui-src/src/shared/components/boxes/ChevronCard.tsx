import React, { PropsWithChildren, useState } from "react";
import { ChevronDown, ChevronRight } from "lucide-react";

type CollapsableCardProps = {
  title: string;
  numberOfElements?: number;
  fallbackText?: string;
  defaultExpanded?: boolean;
};

const ChevronCard = ({ title, numberOfElements, fallbackText, defaultExpanded = true, children }: PropsWithChildren<CollapsableCardProps>) => {
  const [isCollapse, setIsCollapse] = useState(defaultExpanded);

  const getChildren = () => {
    if (!isCollapse) {
      return <></>;
    } else if (children) {
      return <div className="mx-4">{children}</div>;
    } else {
      return <b className="mx-4">{fallbackText}</b>;
    }
  };

  return (
    <div className="my-2 w-full">
      <button className="flex items-center gap-1 text-purple ml-2" onClick={() => setIsCollapse((prev) => !prev)}>
        {isCollapse ? <ChevronDown size={20} strokeWidth={2.5} /> : <ChevronRight size={20} strokeWidth={2.5} />}
        <h3>
          {title} {numberOfElements ?? ""}
        </h3>
      </button>

      {isCollapse && (
        <div className="mt-2 w-full rounded-2xl bg-white border border-grey-200 shadow-[4px_4px_4px_rgba(0,0,0,0.25)] p-1">{getChildren()}</div>
      )}
    </div>
  );
};

export default ChevronCard;
