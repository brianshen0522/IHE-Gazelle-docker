import { useState } from "react";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import { duotoneLight } from "react-syntax-highlighter/dist/esm/styles/prism";
import format from "xml-formatter";
import CheckboxOption from "@shared/CheckboxOption";
import { LoadMoreButton } from "@gazelle/gazelle-component-ui";
import { parseDicomXml } from "@shared/components/renderers/xml/parseDicomXml";
import { parseHl7v2Xml } from "@shared/components/renderers/xml/parseHl7Xml";
import { parseHttpXml } from "@shared/components/renderers/xml/parseHttpXml";
import { useTranslation } from "react-i18next";
import { toast } from "react-toastify";
import { base64ToUtf8 } from "@/app/message-capture/utils/base64ToUtf8";
import {
  displayLineFromLinesProperties,
  getCLassNameForLine,
  getColorToHighlight,
  getIconToHighlight,
} from "@shared/components/renderers/linePropertyHelper";
import { DocumentRenderer } from "@shared/components/renderers/Renderers";
import { useAutoExpandLines } from "@shared/components/renderers/hooks/useAutoExpandLines";

export const XmlRenderer = ({ base64Data, xmlData, dataType, contentType, linesProperties }: DocumentRenderer) => {
  const { t } = useTranslation();
  const [visibleLines, setVisibleLines] = useState(500);
  const [showLineNumbers, setShowLineNumbers] = useState(true);
  const [isPretty, setIsPretty] = useState(true);

  // Auto-expand visible lines when we have a selected line beyond current visible range
  useAutoExpandLines(linesProperties, visibleLines, setVisibleLines);

  let xmlStr = "";

  if (xmlData) xmlStr = xmlData;

  if (base64Data) {
    try {
      switch (dataType) {
        case "DICOM":
          xmlStr = parseDicomXml(base64Data);
          break;
        case "HTTP":
          xmlStr = parseHttpXml(base64Data, contentType || "");
          break;
        case "HL7v2":
          xmlStr = parseHl7v2Xml(base64Data);
          break;
        case "SYSLOG":
          xmlStr = base64Data;
          break;
        default:
          console.error("Unknown type:", dataType);
          break;
      }
    } catch (error) {
      toast.error("Unable to parse XML data: " + (error instanceof Error ? error.message : String(error)));
      xmlStr = base64ToUtf8(base64Data);
    }
  }

  const xmlLines = xmlStr?.split("\n") ?? []; // Split the XML string into an array of lines

  let basicXml;
  try {
    // Prettify the XML string
    basicXml =
      format(xmlLines?.join("\n"), {
        indentation: "  ",
        lineSeparator: "\r\n",
      }) ?? t("gzl.user.interface.error") + ": " + t("gzl.message.capture.failed_to_format_xml_invalid_data_type");
  } catch (error) {
    basicXml = t("gzl.user.interface.error") + ": " + (error instanceof Error ? error.message : t("gzl.message.capture.failed_to_parse_xml"));
  }

  const prettyXmlLines = basicXml.split("\r\n");

  return (
    <div className="flex flex-col gap-2 w-full">
      <div className="flex items-center gap-8 pt-1 pl-3">
        <CheckboxOption
          id="prettify-xml-checkbox"
          type="checkbox"
          checked={isPretty}
          onChange={() => setIsPretty(!isPretty)}
          htmlFor="prettify-xml-checkbox"
        >
          {t("gzl.message.capture.prettify_content")}
        </CheckboxOption>

        <CheckboxOption
          id="show-xml-line-number"
          type="checkbox"
          checked={showLineNumbers}
          onChange={() => setShowLineNumbers(!showLineNumbers)}
          htmlFor="show-xml-line-number"
        >
          {t("gzl.message.capture.show_line_numbers")}
        </CheckboxOption>
      </div>

      {isPretty ? (
        <div className="overflow-y-auto">
          <SyntaxHighlighter
            language="xml"
            style={duotoneLight}
            showLineNumbers={true}
            wrapLongLines
            lineNumberStyle={showLineNumbers ? {} : { display: "none" }}
            lineProps={(line) => displayLineFromLinesProperties(linesProperties, line)}
          >
            {prettyXmlLines.slice(0, visibleLines).join("\r\n")}
          </SyntaxHighlighter>
          <LoadMoreButton dataset={xmlLines} visibleLines={visibleLines} setVisibleLines={setVisibleLines} />
        </div>
      ) : (
        <div className="overflow-y-auto">
          <pre>
            {xmlLines?.slice(0, visibleLines).map((line, lineIndex) => {
              const colorToHighlight = getColorToHighlight(linesProperties, lineIndex + 1);
              return (
                <code key={`${lineIndex + 1}-${line}`} className="flex items-center">
                  {colorToHighlight === "" && (
                    <p>
                      {showLineNumbers && <span className="text-line_number">{lineIndex + 1}</span>} {line}
                    </p>
                  )}
                  {colorToHighlight !== "" && (
                    <button
                      onClick={linesProperties?.find((line) => line.lineNumber === lineIndex + 1)!.onClickHandler}
                      className={getCLassNameForLine(linesProperties, lineIndex + 1)}
                      style={colorToHighlight ? { backgroundColor: colorToHighlight } : undefined}
                    >
                      {showLineNumbers && <span className="text-line_number">{lineIndex + 1}</span>}
                      {getIconToHighlight(linesProperties, lineIndex + 1)} {line}
                    </button>
                  )}
                </code>
              );
            })}
          </pre>
          <LoadMoreButton dataset={xmlLines} visibleLines={visibleLines} setVisibleLines={setVisibleLines} />
        </div>
      )}
    </div>
  );
};
