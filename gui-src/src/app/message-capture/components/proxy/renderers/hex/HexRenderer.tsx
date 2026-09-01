import { useMemo, useState } from "react";
import CheckboxOption from "@shared/CheckboxOption";
import { LoadMoreButton } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import { DocumentRenderer } from "@shared/components/renderers/Renderers";

export const HexRenderer = ({ base64Data, bytesPerLine = 32, dataType }: DocumentRenderer) => {
  const [visibleLines, setVisibleLines] = useState(500);
  const [showLineNumbers, setShowLineNumbers] = useState(true);
  const { t } = useTranslation();
  const bufferData = useMemo(() => {
    if (!base64Data) {
      return Buffer.from("");
    }
    if (dataType === "SYSLOG" || dataType === "HL7v2") {
      return Buffer.from(base64Data);
    } else {
      return Buffer.from(base64Data, "base64");
    }
  }, [base64Data, dataType]);

  const hexDump = useMemo(() => {
    const lines: string[] = [];
    let currentLine = "";

    bufferData
      .toString("hex")
      .match(/.{1,2}/g)
      ?.forEach((val, index) => {
        currentLine += `${val} `;
        if ((index + 1) % bytesPerLine === 0) {
          lines.push(currentLine);
          currentLine = "";
        }
      });

    if (currentLine) lines.push(currentLine);

    return lines.map((line, index) => (
      <div key={index + line} className="flex gap-4">
        {showLineNumbers && <span className="text-line_number">{index.toString().padStart(5, "0")}</span>} {line}
      </div>
    ));
  }, [bufferData, bytesPerLine, showLineNumbers]);

  const utf8Dump = useMemo(() => {
    return Array.from(bufferData.values())
      .map((byte) => (byte >= 32 && byte <= 126 ? String.fromCodePoint(byte) : "·"))
      .reduce((acc: string[], char, index) => {
        if (index % bytesPerLine === 0) acc.push("");
        acc[acc.length - 1] += char;
        return acc;
      }, [])
      .map((line, index) => <div key={index + line}>{line}</div>);
  }, [bufferData, bytesPerLine]);

  if (base64Data === null) {
    return <div>No data provided.</div>;
  }

  if (!base64Data) {
    return <div>Error: The provided Base64 data is invalid.</div>;
  }

  return (
    <div className="flex flex-col gap-4">
      <CheckboxOption
        id="show-hex-line-numbers"
        type="checkbox"
        checked={showLineNumbers}
        onChange={() => setShowLineNumbers(!showLineNumbers)}
        htmlFor="show-hex-line-numbers"
      >
        {t("gzl.message.capture.show_line_numbers")}
      </CheckboxOption>
      <div className="flex items-center overflow-y-auto gap-8">
        <pre>{hexDump?.slice(0, visibleLines)}</pre>
        <pre>{utf8Dump?.slice(0, visibleLines)}</pre>
      </div>
      <LoadMoreButton dataset={hexDump} visibleLines={visibleLines} setVisibleLines={setVisibleLines} />
    </div>
  );
};
