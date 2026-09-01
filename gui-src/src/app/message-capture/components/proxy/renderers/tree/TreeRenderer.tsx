import { useEffect, useState } from "react";
import xml2js from "xml2js";
import TreeNode from "./TreeNode";
import { parseHl7v2Xml } from "@shared/components/renderers/xml/parseHl7Xml";
import { parseHttpJson } from "@shared/components/renderers/json/parseHttpJson";
import { parseHttpXml } from "@shared/components/renderers/xml/parseHttpXml";
import { useTranslation } from "react-i18next";
import { DocumentRenderer } from "@shared/components/renderers/Renderers";

export const TreeRenderer = ({ node, dataType, contentType }: DocumentRenderer) => {
  const [parsedNode, setParsedNode] = useState({});
  const [expandAll, setExpandAll] = useState(false);
  const [expandedKeys, setExpandedKeys] = useState<Record<string, boolean>>({});
  const [error, setError] = useState<string | null>(null);
  const { t } = useTranslation();

  let xmlNode: string | object | undefined;
  let jsonNode: string | undefined;

  useEffect(() => {
    if (xmlNode) {
      const parser = new xml2js.Parser();
      const xmlString = typeof xmlNode === "string" ? xmlNode : JSON.stringify(xmlNode);
      parser.parseString(xmlString, (err, result) => {
        if (err) {
          setError(`Failed to parse: ${err.message}`);
        } else {
          setParsedNode(result);
        }
      });
    } else if (jsonNode) {
      try {
        const parsedJSON = {
          root: JSON.parse(jsonNode),
        };
        setParsedNode(parsedJSON);
      } catch (err) {
        setError(`Failed to parse JSON: ${(err as Error).message}`);
      }
    }
  }, [dataType, jsonNode, xmlNode]);

  if (dataType === "HL7v2") {
    if (!node) return null;
    xmlNode = parseHl7v2Xml(node)?.replaceAll("&", "&amp;");
  } else if (dataType === "HTTP") {
    if (!node) return null;
    if (contentType?.toLowerCase().includes("json")) jsonNode = parseHttpJson(node);
    else xmlNode = parseHttpXml(node, contentType!);
  }

  const addKeysRecursively = (node: object, path: string, newKeys: Record<string, boolean>, expandAll: boolean) => {
    Object.entries(node).forEach(([key, value], index) => {
      const uniqueKey = `${path}-${key}-${index}`;
      newKeys[uniqueKey] = expandAll;
      if (typeof value === "object" && value !== null) {
        addKeysRecursively(value, uniqueKey, newKeys, expandAll);
      }
    });
  };

  const toggleExpandAll = () => {
    setExpandAll((prev) => !prev);
    setExpandedKeys(() => {
      const newKeys: Record<string, boolean> = {};
      addKeysRecursively(parsedNode, "root", newKeys, !expandAll);
      return newKeys;
    });
  };

  return (
    <div className="flex flex-col gap-4">
      {error ? (
        <div className="text-red">{error}</div>
      ) : (
        <>
          <button
            onClick={toggleExpandAll}
            className="flex justify-start cursor-pointer hover:text-color"
            aria-label="Expand/Collapse all"
            title={t("gzl.user.interface.expand_collapse")}
          >
            {expandAll ? t("gzl.user.interface.collapse_all") : t("gzl.user.interface.expand_all")}
          </button>

          <TreeNode node={parsedNode} level={0} path="root" expandedKeys={expandedKeys} setExpandedKeys={setExpandedKeys} />
        </>
      )}
    </div>
  );
};
