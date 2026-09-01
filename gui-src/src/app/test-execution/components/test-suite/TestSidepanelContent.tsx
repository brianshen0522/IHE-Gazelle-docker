import { InfoRow, SidePanel, TagSection, CopyToClipboard } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import { Test } from "../../types/TestCase";
import TestRunHistoryTable from "@test-execution/components/test-run/TestRunHistoryTable";
import { TestRoleCard } from "./TestRoleCard";
import { useState } from "react";
import { SquareMinus, SquarePlus } from "lucide-react";
import { toast } from "react-toastify";

interface TestSidepanelContentProps {
  test: Test;
}

const TestSidepanelContent = ({ test }: TestSidepanelContentProps) => {
  const { t } = useTranslation();
  const [isExpanded, setIsExpanded] = useState(false);

  if (!test) {
    return null;
  }

  function buildTestRoleDisplay() {
    return (
      <div>
        <div className="flex flex-row gap-3">
          <InfoRow label={t("gzl.user.interface.test_roles")} value=" " />
          <button onClick={() => setIsExpanded(!isExpanded)} className="flex flex-row gap-1 items-center text-sm">
            {!isExpanded && (
              <>
                <SquarePlus size={15} className="text-blue" />
                <span className="text-blue">{t("gzl.user.interface.expand")}</span>
              </>
            )}
            {isExpanded && (
              <>
                <SquareMinus size={15} className="text-blue" />
                <span className="text-blue">{t("gzl.user.interface.collapse")}</span>
              </>
            )}
          </button>
        </div>
        {test.testRoles.map((role, index) => (
          <TestRoleCard key={`${role.name}-${index}`} name={role.name} capabilities={role.capabilities} isExpanded={isExpanded} />
        ))}
      </div>
    );
  }

  return (
    <>
      <SidePanel.Section id="test-details" title={t("gzl.user.interface.test_case")}>
        {test.name && <InfoRow label={t("gzl.user.interface.test_name")} value={test.name} />}
        {test.testId && (
          <InfoRow
            label={t("gzl.user.interface.test_id")}
            value={
              <CopyToClipboard
                text={test.testId}
                label={test.testId}
                onCopySuccess={() => toast.success(t("gzl.user.interface.test_id_copied"))}
                onCopyError={() => toast.error(t("gzl.user.interface.failed_to_copy_test_id"))}
              />
            }
          />
        )}
        {test.version && <InfoRow label={t("gzl.user.interface.version")} value={test.version} />}
        {test.summary && <InfoRow label={t("gzl.user.interface.summary")} value={test.summary} />}
        {test.testRoles?.length > 0 && buildTestRoleDisplay()}

        <TagSection items={test.tags} labelKey={t("gzl.user.interface.tags")} keyPrefix="tag" />
      </SidePanel.Section>
      <SidePanel.Section id="last-executions" title={t("gzl.texec.history.last_executions")}>
        <TestRunHistoryTable testId={test.testId} />
      </SidePanel.Section>
    </>
  );
};

export default TestSidepanelContent;
