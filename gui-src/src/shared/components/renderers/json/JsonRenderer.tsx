import { useState } from "react";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import { duotoneLight } from "react-syntax-highlighter/dist/esm/styles/prism";
import { parseDicomJson } from "@shared/components/renderers/json/parseDicomJson";
import { parseHttpJson } from "@shared/components/renderers/json/parseHttpJson";
import { parseHl7v2Json } from "@shared/components/renderers/json/parseHl7v2Json";
import { parseSyslogJson } from "@shared/components/renderers/json/parseSyslogJson";
import CheckboxOption from "@shared/CheckboxOption";
import { useTranslation } from "react-i18next";

import {
  displayLineFromLinesProperties,
  getCLassNameForLine,
  getColorToHighlight,
  getIconToHighlight,
} from "@shared/components/renderers/linePropertyHelper";
import { DocumentRenderer } from "@shared/components/renderers/Renderers";

export const JsonRenderer = ({ base64Data, dataType, linesProperties }: DocumentRenderer) => {
  const { t } = useTranslation();
  const [showLineNumbers, setShowLineNumbers] = useState(true);
  const [prettify, setPrettify] = useState(true);

  if (!base64Data) {
    return <div>No data provided.</div>;
  }

  let dataStr = "";
  if (base64Data) {
    switch (dataType) {
      case "DICOM":
        dataStr = parseDicomJson(base64Data);
        break;
      case "HTTP":
        dataStr = parseHttpJson(base64Data);
        break;
      case "HL7v2":
        dataStr = parseHl7v2Json(base64Data);
        break;
      case "SYSLOG":
        dataStr = parseSyslogJson(base64Data);
        break;
      default:
        console.error("Unknown type:", dataType);
        break;
    }
  }

  return (
    <div className="flex flex-col gap-2 w-full">
      {dataType !== "HL7v2" && (
        <div className="flex items-center gap-8 pt-1 pl-3">
          <CheckboxOption id="prettify-json" type="checkbox" htmlFor="prettify-json" checked={prettify} onChange={() => setPrettify(!prettify)}>
            Prettify content
          </CheckboxOption>
          <CheckboxOption
            id="show-json-line-numbers"
            type="checkbox"
            htmlFor="show-json-line-numbers"
            checked={showLineNumbers}
            onChange={() => setShowLineNumbers(!showLineNumbers)}
          >
            {t("gzl.message.capture.show_line_numbers")}
          </CheckboxOption>
        </div>
      )}
      {prettify && dataType !== "HL7v2" ? (
        <div className="overflow-y-auto">
          <SyntaxHighlighter
            language="json"
            style={duotoneLight}
            showLineNumbers={true}
            wrapLongLines
            lineNumberStyle={showLineNumbers ? {} : { display: "none" }}
            lineProps={(lineNumber) => displayLineFromLinesProperties(linesProperties, lineNumber)}
          >
            {dataStr}
          </SyntaxHighlighter>
        </div>
      ) : (
        <div className="overflow-y-auto">
          {dataType === "HL7v2" ? (
            <pre>{dataStr}</pre>
          ) : (
            <pre>
              {typeof dataStr === "string" &&
                dataStr.split("\n").map((line, lineIndex) => {
                  const colorToHighlight = getColorToHighlight(linesProperties, lineIndex + 1);
                  return (
                    <div key={`${lineIndex + 1}-${line}`} className={`flex items-center`}>
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
                    </div>
                  );
                })}
            </pre>
          )}
        </div>
      )}
    </div>
  );
};
