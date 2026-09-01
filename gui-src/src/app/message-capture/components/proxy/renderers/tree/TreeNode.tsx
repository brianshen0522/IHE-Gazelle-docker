import React from "react";
import { PlusSquare, MinusSquare, Folder } from "lucide-react";
import InfoTruncateData from "@/app/message-capture/components/proxy/message/InfoTruncateData";

export type TreeNodeProps = {
  node: any;
  level: number;
  path: string;
  expandedKeys: Record<string, boolean>;
  setExpandedKeys: React.Dispatch<React.SetStateAction<Record<string, boolean>>>;
};

const TreeNode = ({ node, level = 0, path, expandedKeys, setExpandedKeys }: TreeNodeProps) => {
  const toggleExpanded = (key: string) => {
    setExpandedKeys((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  if (typeof node === "string" || typeof node === "number") {
    return <div>{node}</div>;
  }

  return (
    <>
      {Object.entries(node).map(([key, value], index) => {
        const nodeKey = `${path}-${key}-${index}`;
        const isExpanded = expandedKeys[nodeKey];
        const isCollapsible =
          typeof value === "object" && value !== null && (Array.isArray(value) ? value.length <= 100 : Object.keys(value).length <= 100);

        return (
          <div key={nodeKey} className={`flex flex-col gap-2 ${level && "pl-4"}`}>
            <div className="flex items-center gap-2 cursor-pointer">
              {isCollapsible && (
                <button onClick={() => toggleExpanded(nodeKey)}>{isExpanded ? <MinusSquare size={16} /> : <PlusSquare size={16} />}</button>
              )}
              <Folder size={16} /> <div className="text-purple font-bold">{key}:</div>
              <div>
                {typeof value === "string" || typeof value === "number"
                  ? ` ${value}`
                  : !isExpanded &&
                    value !== null &&
                    (Array.isArray(value) ? (
                      <div className="flex items-center gap-2">
                        <span className="text-line_number">array[{value.length}]</span>
                        <span>{value.length > 100 && <InfoTruncateData />}</span>
                      </div>
                    ) : (
                      <div className="flex items-center gap-2">
                        <span className="text-line_number">object{`{${Object.keys(value ?? {}).length}}`}</span>
                        <span>{Object.keys(value ?? {}).length > 100 && <InfoTruncateData />}</span>
                      </div>
                    ))}
              </div>
            </div>

            {isExpanded && isCollapsible && typeof value === "object" && value !== null && (
              <TreeNode key={nodeKey} node={value} level={level + 1} path={nodeKey} expandedKeys={expandedKeys} setExpandedKeys={setExpandedKeys} />
            )}
          </div>
        );
      })}
    </>
  );
};

export default TreeNode;
