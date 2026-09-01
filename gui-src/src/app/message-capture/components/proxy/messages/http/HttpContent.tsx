import React from "react";
import { useSearchParams } from "next/navigation";
import MultiParts from "@message-capture/multiparts/MultiParts";
import HttpInformation from "./HttpInformation";
import HttpHeader from "./HttpHeader";
import { DataMessageProps, HttpData } from "@/app/message-capture/components/proxy/Types";
import HttpBody from "./HttpBody";
import { getHttpContentType } from "@/app/message-capture/utils/httpHeaders";

const HttpContent = ({ data }: DataMessageProps) => {
  const searchParams = useSearchParams();
  const id = searchParams.get("id");
  const content = data.content;
  const headers = content.headers || {};
  const contentTypeHeader = getHttpContentType(headers);

  const hasHTTPParts: boolean = contentTypeHeader.toLowerCase().includes("multipart");

  const isDecoderError = !!content?.unexpectedErrors?.rootType && content?.unexpectedErrors?.rootType === "DECODER_ERROR";

  // Cast data to HttpData type since this component is specifically for HTTP content
  const httpData = data as unknown as HttpData;

  return (
    <>
      <HttpInformation data={data} />
      {!isDecoderError && (
        <>
          <HttpHeader headers={headers} content={content} />
          <HttpBody id={id} data={httpData} contentTypeHeader={contentTypeHeader} searchParams={searchParams} />
        </>
      )}
      {hasHTTPParts && <MultiParts hasHTTPParts={hasHTTPParts} id={id} />}
    </>
  );
};

export default HttpContent;
