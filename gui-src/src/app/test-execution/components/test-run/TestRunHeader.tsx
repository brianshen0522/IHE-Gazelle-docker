import { useTranslation } from "react-i18next";
import ContentHeaderWrapper from "@shared/components/layout/ContentHeaderWrapper";

interface TestRunProps {
  testName: string;
}

export const TestRunHeader = ({ testName }: TestRunProps) => {
  const { t } = useTranslation();

  const testRunBreadCrumbs = [
    { label: "Home", url: "/home" },
    { label: "Test execution", url: `/test-execution/test-suite` },
    { label: testName, url: "" },
  ];

  const testRunTitle = (
    <div className="flex justify-start items-center gap-4 mb-4">
      {t("gzl.texec.test_run")} : {testName}
    </div>
  );

  return <ContentHeaderWrapper id="test-run" title={testRunTitle} breadcrumbs={testRunBreadCrumbs} />;
};
