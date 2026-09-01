import { ValidationReportDTO } from "@/shared/types/validation/types";
import { CollapsableSubCard, InfoRow } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";

function isLink(value: string | undefined): boolean {
  return value !== undefined && (value.startsWith("http://") || value.startsWith("https://"));
}

export function ValidationServiceMetadata({ validationReport }: Readonly<{ validationReport: ValidationReportDTO }>) {
  const { t } = useTranslation();

  if (!validationReport.additionalMetadata?.length) {
    return null;
  }

  const title = <h3 id="validation-service-metadata">{t("gzl.texec.validation_service_metadata")}</h3>;

  return (
    <CollapsableSubCard title={title} defaultExpanded={false}>
      <div className="flex flex-col space-y-2">
        {validationReport.additionalMetadata.map((metadata, index) => (
          <InfoRow
            key={metadata.name + index}
            label={metadata.name}
            value={
              isLink(metadata.value) ? (
                <a className="text-blue" href={metadata.value}>
                  {metadata.value}
                </a>
              ) : (
                metadata.value
              )
            }
          />
        ))}
      </div>
    </CollapsableSubCard>
  );
}
