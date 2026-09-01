import { PropsWithChildren, useState } from "react";
import { ChevronDown } from "lucide-react";

type CollapsableCardProps = {
  id?: string;
  title: string;
  numberOfElements?: number;
  fallbackText?: string;
};

const CollapsableCard = ({ id, title, numberOfElements, fallbackText, children }: PropsWithChildren<CollapsableCardProps>) => {
  const [isCollapse, setIsCollapse] = useState(true);

  const getChildren = () => {
    if (children) {
      return <div className="mx-4">{children}</div>;
    } else {
      return <p className="mx-4">{fallbackText}</p>;
    }
  };

  return (
    <div id={id || title.toLowerCase().replaceAll(" ", "-")} className="my-1 w-full border border-lightpurple bg-white rounded-md">
      <div className={`flex flex-col ${isCollapse ? "rounded-t-xl" : "rounded-xl"}`}>
        <button
          className="flex flex-row justify-start align-middle items-center gap-1 p-1 hover:bg-lightpurple rounded duration-300 ease-in-out"
          onClick={() => setIsCollapse((prevState) => !prevState)}
        >
          <div className={`transform transition-transform duration-300 ${isCollapse ? "rotate-0" : "-rotate-90"}`}>
            <ChevronDown />
          </div>
          <h3 className="ml-3">
            {title} {numberOfElements !== undefined && `(${numberOfElements})`}
          </h3>
        </button>
      </div>
      <div className={`grid transition-all duration-300 ease-in-out ${isCollapse ? "grid-rows-[1fr] opacity-100" : "grid-rows-[0fr] opacity-0"}`}>
        <div className="overflow-hidden rounded-md shadow-lg">{getChildren()}</div>
      </div>
    </div>
  );
};

export default CollapsableCard;
