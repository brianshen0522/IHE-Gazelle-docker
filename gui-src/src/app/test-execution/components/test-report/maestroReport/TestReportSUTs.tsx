import CollapsableCard from "@shared/components/boxes/CollapsableCard";
import { SystemUnderTest } from "@maestro/types/report/TestReport";
import CollapsableSubCard from "@shared/components/boxes/CollapsableSubCard";
import { useTranslation } from "react-i18next";

export function TestReportSUTs(testReportSUTs: Readonly<{ testReportSUTs: SystemUnderTest[] }>) {
  const { t } = useTranslation();

  if (!testReportSUTs?.testReportSUTs || testReportSUTs.testReportSUTs.length === 0) {
    return (
      <CollapsableCard title={t("gzl.texec.systems_under_test")}>
        <p className="text-grey-500 m-1">{t("gzl.texec.no_sut_available")}</p>
      </CollapsableCard>
    );
  }
  return (
    <CollapsableCard title={t("gzl.texec.systems_under_test")}>
      <div className="flex flex-col gap-3">
        {testReportSUTs.testReportSUTs.map((sut) => {
          const title = (
            <h3 id={`systems-under-test-${sut.systemIdentification.name}`} className="text-medium">
              {sut.systemIdentification.name}
            </h3>
          );
          return (
            <CollapsableSubCard key={sut.systemIdentification.name} title={title} expanded={false}>
              <p className="text-grey-500 m-1">
                {t("gzl.texec.version")}: {sut.systemIdentification.version}
              </p>
              {sut.hostNames && sut.hostNames.length > 0 && (
                <p className="text-grey-500 m-1">
                  {t("gzl.texec.hostnames")}: {sut.hostNames.join(", ")}
                </p>
              )}
              {sut.ipAddresses && sut.ipAddresses.length > 0 && (
                <p className="text-grey-500 m-1">
                  {t("gzl.texec.ip_addresses")}: {sut.ipAddresses.join(", ")}
                </p>
              )}
              {sut.macAddresses && sut.macAddresses.length > 0 && (
                <p className="text-grey-500 m-1">
                  {t("gzl.texec.mac_addresses")}: {sut.macAddresses.join(", ")}
                </p>
              )}
            </CollapsableSubCard>
          );
        })}
      </div>
    </CollapsableCard>
  );
}
