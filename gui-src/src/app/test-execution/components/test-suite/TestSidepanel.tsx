"use client";
import { Badge, SidePanel, useSidePanel } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import { CirclePlay } from "lucide-react";
import TestSidepanelContent from "./TestSidepanelContent";
import { Test } from "../../types/TestCase";

export default function TestSidepanel() {
  const { t } = useTranslation();
  const { isOpen, setIsOpen, selectedRow: testCase } = useSidePanel<Test>();

  const handleClose = () => {
    setIsOpen(false);
  };

  const accessDetailsProps = {
    id: testCase?.testId || "",
    redirectLink: <>{t("gzl.texec.new_execution")}</>,
    pathname: "/test-execution/test-run-execution",
    query: { testId: testCase?.testId || "" },
    icon: CirclePlay,
    iconSize: 16,
  };

  return (
    <SidePanel isOpen={isOpen} className="p-1">
      {testCase ? (
        <>
          <SidePanel.Header accessDetailsProps={testCase.inScope ? accessDetailsProps : undefined} onClose={handleClose} />
          {!testCase.inScope && (
            <Badge id="scope" variant="default">
              {t("gzl.user.interface.deprecated")}
            </Badge>
          )}
          <TestSidepanelContent test={testCase} />
        </>
      ) : null}
    </SidePanel>
  );
}
