"use client";
import { useEffect } from "react";
import { ValidationInput } from "@/shared/types/validation/types";
import { CollapsableSubCard } from "@gazelle/gazelle-component-ui";
import { useReportAssertions } from "@validation-portal/context/selectedAssertionContext";
import FileRenderer from "@/app/validation-portal/components/validation-report/content/FileRenderer";
import { useTranslation } from "react-i18next";
import { getAttachmentIdFromItem } from "@/app/validation-portal/utils/getAttachmentIdFromItem";

function ValidationReportFiles({
  itemId,
  validationItems,
  inputFileNames,
  readAccessKey,
}: Readonly<{
  itemId: string;
  validationItems: ValidationInput[];
  inputFileNames?: Record<string, string>;
  readAccessKey?: string;
}>) {
  const { selectedAssertion } = useReportAssertions();
  const { t } = useTranslation();

  // When an assertion is selected, scroll to the first line number in the subject location if it is a failed assertion
  useEffect(() => {
    if (selectedAssertion?.result === "FAILED" && validationItems?.length > 0) {
      const numberOfSelectedLines = document.querySelectorAll(".gzl-selected-line").length;
      if (numberOfSelectedLines > 0) {
        setTimeout(() => {
          const lineElements = document.querySelectorAll(".gzl-selected-line");
          const firstElement = lineElements[0];
          firstElement.scrollIntoView({ behavior: "smooth", block: "center" });
        }, 100);
      }
    }
  }, [selectedAssertion, validationItems]);

  return (
    <div className="flex flex-col gap-4">
      {validationItems?.map((item, itemIndex) => {
        const fileNames = inputFileNames;
        const attachmentId = getAttachmentIdFromItem(item);
        const rawName =
          fileNames?.[attachmentId ?? ""] ??
          fileNames?.[item.id] ??
          (item.itemId ? fileNames?.[item.itemId] : undefined) ??
          item.id ??
          item.itemId ??
          "";
        let fileName = rawName.split("/").pop() ?? rawName;
        if ((!fileName || fileName === "contentToValidate") && inputFileNames) {
          const singleName = Object.values(inputFileNames)[0];
          if (singleName && Object.keys(inputFileNames).length === 1) {
            fileName = singleName;
          }
        }

        const title = (
          <h3 id={`validated-file-${itemIndex}`}>
            {t("gzl.texec.validated_file")} #{itemIndex + 1} {fileName && `(${fileName})`}
          </h3>
        );
        return (
          <CollapsableSubCard key={item.id} title={title} className="border-lightpurple">
            <FileRenderer
              fileName={fileName}
              content={item.content}
              attachmentId={attachmentId ?? undefined}
              inputId={item.id}
              itemId={itemId}
              readAccessKey={readAccessKey}
            />
          </CollapsableSubCard>
        );
      })}
    </div>
  );
}

export default ValidationReportFiles;
