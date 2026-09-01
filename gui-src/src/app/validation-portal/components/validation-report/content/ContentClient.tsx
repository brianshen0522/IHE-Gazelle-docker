"use client";
import ContentHeaderWrapper from "@/shared/components/layout/ContentHeaderWrapper";
import FileRenderer from "./FileRenderer";
import { useSearchParams } from "next/navigation";
import { useTranslation } from "react-i18next";
import { ScrollTop } from "@gazelle/gazelle-component-ui";

const ContentClient = () => {
  const { t } = useTranslation();
  const searchParams = useSearchParams();
  const itemId = searchParams.get("itemId") ?? "";
  const attachmentId = searchParams.get("attachmentId") ?? "";
  const fileName = searchParams.get("fileName") ?? "";
  const readAccessKey = searchParams.get("readAccessKey") ?? "";

  return (
    <>
      <ContentHeaderWrapper
        id="validated-content"
        title={t("gzl.texec.validated_file") + (itemId && `${" "}(${fileName})`)}
        breadcrumbs={[]}
      />
      <FileRenderer fileName={fileName} itemId={itemId} attachmentId={attachmentId} readAccessKey={readAccessKey || undefined} />
      <ScrollTop />
    </>
  );
};

export default ContentClient;
