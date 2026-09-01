import { SectionTitle } from "@gazelle/gazelle-component-ui";
import { HttpHeaderProps } from "./Types";
import {useTranslation} from "react-i18next";

const HttpHeader = ({ headers, content }: HttpHeaderProps) => {
  const { t } = useTranslation();
  const headersContent =
    headers &&
    Object.entries(headers)?.map(([key, value]) => (
      <div key={key} className="flex gap-2">
        <p className="font-semibold">{key}:</p> <span className="break-all">{value}</span>
      </div>
    ));

  return (
    <>
      <SectionTitle id="http-headers" title={t('gzl.message.capture.headers')} />
      <div className="flex items-center justify-between">
        <div>
          <div className="flex font-semibold">
            {content.method} {content.uri} {content.version} {content.status} {content.reasonPhrase}
          </div>
          <div>{headersContent}</div>
        </div>
      </div>
    </>
  );
};
export default HttpHeader;
