import React from "react";
import { Part } from "@/app/message-capture/components/proxy/messages/multiparts/Types";

interface MultiPartHeadersProps {
  content: Part;
  index: number;
}

const HeaderInfo = ({ label, value }: { label: string; value: any }) => (
  <div key={label} className="flex justify-start gap-2">
    <p className="font-semibold">{label}:</p> <span>{value}</span>
  </div>
);

const MultiPartHeaders = ({ content, index }: MultiPartHeadersProps) => {
  const { unexpectedErrors, headers } = content;
  const isErrorMultiPart = unexpectedErrors?.rootType;
  return (
    <>
      {!isErrorMultiPart && headers ? (
        <div className="flex flex-col select-text">
          <h4 className="flex justify-start font-semibold">{"PART N°" + (index + 1)}</h4>
          {headers["name"] && <HeaderInfo label="Name" value={headers["name"]} />}
          {headers["Content-Type"] && <HeaderInfo label="Content-Type" value={headers["Content-Type"]} />}
          {headers["filename"] && <HeaderInfo label="Filename" value={headers["filename"]} />}
          {headers["Content-ID"] && <HeaderInfo label="Content-ID" value={headers["Content-ID"]} />}
        </div>
      ) : (
        <h2 className="flex justify-start font-semibold">{content.type}</h2>
      )}
    </>
  );
};

export default MultiPartHeaders;
