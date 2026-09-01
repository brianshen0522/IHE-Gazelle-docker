import { JSX, useState } from "react";
import CheckboxOption from "@shared/CheckboxOption";
import { LoadMoreButton } from "@gazelle/gazelle-component-ui";
import { base64ToUtf8 } from "@/app/message-capture/utils/base64ToUtf8";
import { parseDicom } from "@/app/message-capture/utils/parseDicom";
import { detectSeparator } from "@shared/components/renderers/xml/parseHl7Xml";
import { useTranslation } from "react-i18next";
import { DocumentRenderer } from "@shared/components/renderers/Renderers";
import { useAutoExpandLines } from "@shared/components/renderers/hooks/useAutoExpandLines";
import {isBase64Encoded} from "@message-capture/utils/isBase64Encoded";

export const RawRenderer = ({ base64Data, xmlData, dataType, linesProperties }: DocumentRenderer) => {
  const [visibleLines, setVisibleLines] = useState(500);
  const [showLineNumbers, setShowLineNumbers] = useState(true);
  const [prettify, setPrettify] = useState(true);
  const { t } = useTranslation();

  // Auto-expand visible lines when we have a selected line beyond current visible range
  useAutoExpandLines(linesProperties, visibleLines, setVisibleLines);

  const rawRenderer = () => {
    if (dataType === "HL7v2") {
      if (!base64Data) return null;
      const decodedData = isBase64Encoded(base64Data) ? base64ToUtf8(base64Data) : base64Data;
      const segments: JSX.Element[] = decodedData
        ?.split(/\r\n|\r|\n/)
        .map((segment: string, index: number) => renderSegment(segment, index, showLineNumbers, prettify, dataType));
      return (
        <>
          <div>{segments?.slice(0, visibleLines)}</div>
          <LoadMoreButton dataset={segments} visibleLines={visibleLines} setVisibleLines={setVisibleLines} />
        </>
      );
    } else if (dataType === "SYSLOG") {
      return <code>{xmlData}</code>;
    } else if (dataType === "HTTP" && xmlData) {
      return <code>{xmlData}</code>;
    } else if (dataType === "DICOM") {
      if (!base64Data) return null;
      const dicomRaw = parseDicom(base64Data, "Raw");
      const segments = dicomRaw?.split("\n").map((segment, index) => renderSegment(segment, index, showLineNumbers, prettify, dataType));
      return (
        <>
          <div>{segments?.slice(0, visibleLines)}</div>
          <LoadMoreButton dataset={segments} visibleLines={visibleLines} setVisibleLines={setVisibleLines} />
        </>
      );
    } else {
      if (!base64Data) return null;
      const decodedData = base64ToUtf8(base64Data);
      return <code>{decodedData}</code>;
    }
  };

  return (
    <div className="flex flex-col gap-4">
      {dataType === "HL7v2" && (
        <div className="flex gap-8">
          <CheckboxOption
            id="prettify-raw-content"
            type="checkbox"
            checked={prettify}
            onChange={() => setPrettify(!prettify)}
            htmlFor="prettify-raw-content"
          >
            {t("gzl.message.capture.prettify_content")}
          </CheckboxOption>

          <CheckboxOption
            id="show-raw-line-numbers"
            type="checkbox"
            checked={showLineNumbers}
            onChange={() => setShowLineNumbers(!showLineNumbers)}
            htmlFor="show-raw-line-numbers"
          >
            {t("gzl.message.capture.show_line_numbers")}
          </CheckboxOption>
        </div>
      )}
      <div className="overflow-y-auto">{rawRenderer()}</div>
    </div>
  );
};

// Hl7v2 parser for segments
const colorizeChar = (char: string, index: number, separator: string) => {
  if (char === separator) {
    return (
      <span key={index} style={{ color: "red" }}>
        {char}
      </span>
    );
  } else if (char === "^") {
    return (
      <span key={index} style={{ color: "blue" }}>
        {char}
      </span>
    );
  } else {
    return char;
  }
};

const renderSegment = (segment: string, index: number, showLineNumbers: boolean, prettify: boolean, dataType: string) => {
  let content;
  if (dataType === "HL7v2") {
    const separator = detectSeparator(segment);
    const chars = segment.split("").map((char, charIndex) => colorizeChar(char, charIndex, separator));
    content = prettify ? <pre className="m-0">{chars}</pre> : <pre className="m-0">{segment}</pre>;
  } else {
    content = <pre className={dataType === "DICOM" ? "m-0 font-mono whitespace-pre-wrap break-words" : "m-0"}>{segment}</pre>;
  }

  return (
    <div key={index} className="flex gap-4">
      {showLineNumbers && (
        <span className="flex min-w-8 font-mono tabular-nums text-line_number">
          {index.toString().padStart(4, "0")}
        </span>
      )}
      {content}
    </div>
  );
};
