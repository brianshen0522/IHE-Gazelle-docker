"use client";
import { JSX } from "react";
import { ValidationInput, ValidationReportDTO } from "@/shared/types/validation/types";
import { CircleAlert, Download } from "lucide-react";
import { AclDisplay } from "@/shared/components/ACL/AclDisplay";
import { Button, CollapsableSubCard, InfoRow, NoticeBanner } from "@gazelle/gazelle-component-ui";
import { AccessControlList } from "@/shared/types/AccessControlListTypes";
import { useParams } from "next/navigation";
import { useTranslation } from "react-i18next";
import ValidationStatSummary from "./ValidationStatSummary";
import useDateFormat from "@/shared/hooks/useDateFormat";
import UnexpectedErrorValidationAlert from "../UnexpectedErrorValidationAlert";
import { handleDownloadAttachment, handleDownloadValidatedItem, handleDownloadValidationReport } from "@/shared/utils/handleDownloadItems";
import { getAttachmentIdFromItem } from "@/app/validation-portal/utils/getAttachmentIdFromItem";

function ValidationReportSummary({
  validationReport,
  acl,
  readAccessKey,
}: Readonly<{ validationReport: ValidationReportDTO; acl: AccessControlList; readAccessKey?: string }>) {
  const { t } = useTranslation();
  const formatDate = useDateFormat(false);
  const { reportId: itemId } = useParams<{ reportId: string }>();

  const getValidatedItemLabel = (item: ValidationInput) => {
    const fileNames = validationReport.inputFileNames;
    const attachmentId = getAttachmentIdFromItem(item);
    const rawName =
      fileNames?.[attachmentId ?? ""] ?? fileNames?.[item.id] ?? (item.itemId ? fileNames?.[item.itemId] : undefined) ?? item.id ?? item.itemId;
    if (!rawName) {
      return t("gzl.texec.validated_file");
    }

    let baseName = rawName.split("/").pop() ?? rawName;
    if ((baseName === "contentToValidate" || !baseName) && validationReport.inputFileNames) {
      const values = Object.values(validationReport.inputFileNames);
      if (values.length === 1 && values[0]) {
        baseName = values[0];
      }
    }
    return baseName;
  };

  function getValidatedItemLink(item: ValidationInput, itemName: string): JSX.Element {
    const attachmentId = getAttachmentIdFromItem(item);
    const itemLocation = attachmentId ? () => handleDownloadAttachment(itemId, attachmentId, itemName, t, readAccessKey) : undefined;

    const itemContent = item.content ? () => handleDownloadValidatedItem(itemName, item.content!, t) : undefined;

    // Prefer inline content when available, fallback to attachment download.
    const handleClick = itemContent ?? itemLocation;

    if (handleClick) {
      return (
        <button type="button" title={t("gzl.user.interface.download_item")} onClick={handleClick}>
          {itemName}
        </button>
      );
    }

    return <span className="text-grey-500">{itemName}</span>;
  }

  const validationMethod = validationReport.validationMethod;
  const validationProfileDisplayName = validationMethod?.validationProfileName || validationMethod?.validationProfileID;
  const hasMultipleInputs = (validationReport.inputs?.length ?? 0) > 1;
  const title = <h3 id="validation-summary">{t("gzl.texec.validation_summary")}</h3>;

  //FIXME: Make translations generic and not specific to test execution
  return (
    <CollapsableSubCard title={title} className="border-lightpurple">
      <div className="flex flex-col xl:flex-row justify-start gap-2 mt-3">
        <div className="flex flex-col gap-4 justify-start basis-7/12">
          {validationReport.inputs && (
            <div className="flex flex-col gap-2">
              {validationReport.inputs.map((validationItem, index) => {
                const fileNumber = hasMultipleInputs ? ` ${index + 1}` : "";
                const label = `${t("gzl.texec.validated_file")}${fileNumber}`;
                return (
                  <InfoRow
                    key={validationItem.id}
                    label={label}
                    value={
                      <span className="flex items-center gap-2 text-blue hover:text-visited_link">
                        {getValidatedItemLink(validationItem, getValidatedItemLabel(validationItem))}
                        <Download size={14} />
                      </span>
                    }
                  />
                );
              })}
            </div>
          )}
          <InfoRow
            label={t("gzl.texec.validation_profile")}
            value={
              <>
                {validationProfileDisplayName}{" "}
                {validationMethod?.validationProfileVersion && `(${validationMethod?.validationProfileVersion})`}
              </>
            }
          />
          <InfoRow
            label={t("gzl.texec.validation_service")}
            value={
              <>
                {validationMethod?.validationServiceName}{" "}
                {validationMethod?.validationServiceVersion && `(${validationMethod?.validationServiceVersion})`}
              </>
            }
          />
          <InfoRow label={t("gzl.texec.validation_date")} value={formatDate(validationReport.dateTime)} />
          <AclDisplay acl={acl} itemId={itemId} />
        </div>

        <div className="flex flex-col gap-2 items-center">
          <ValidationStatSummary validationReport={validationReport} />
          <Button
            id="download-validation-report"
            type={"button"}
            variant="primary"
            onClick={() => handleDownloadValidationReport(validationReport, t)}
          >
            <Download /> {t("gzl.texec.download_validation_report")}
          </Button>
        </div>
      </div>

      <UnexpectedErrorValidationAlert validationReport={validationReport} />

      <NoticeBanner color="blue" className="flex items-center gap-2 italic">
        <CircleAlert />
        {validationReport.disclaimer}
      </NoticeBanner>
    </CollapsableSubCard>
  );
}

export default ValidationReportSummary;
