import { InfoRow, TagSection } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import { TestModel } from "../../../types/TestModel";
import { TestRunExecution } from "../../../types/TestRunExecution";
import { getReportUrl, getTestReportUrl } from "../utils/getReportUrl";
import { Route } from "next";
import LastExecution from "./LastExecution";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { AclDisplay } from "@/shared/components/ACL/AclDisplay";
import { persistTestRunExecutionAcl } from "@/shared/utils/acl/aclActions";
import { useOwnerName } from "@/shared/hooks/useOwnerName";

interface TestRunProps {
  testModel?: TestModel;
  testSessionId?: string;
  testRun: TestRunExecution;
}

const TestRun = ({ testModel, testSessionId, testRun }: TestRunProps) => {
  const { t } = useTranslation();
  const formatDate = useDateFormat(false);

  const ownerId = testRun?.accessControlList?.owners?.[0];
  const ownerName = useOwnerName(ownerId);

  if (!testModel) {
    return null;
  }

  const lastExecutionDate = testRun?.testReportSummaries?.at(-1)?.dateTime;
  const testReportUrl = getTestReportUrl(testRun);
  const reportUrl = getReportUrl({ testReportUrl, testSessionId }) as Route;

  return (
    <div className="flex flex-row gap-10 p-2">
      <div className="space-y-5">
        <InfoRow label={t("gzl.texec.test_run_id")} value={testModel.id} />
        {testSessionId && <InfoRow label={t("gzl.texec.test_session")} value={testSessionId} />}
        {lastExecutionDate && <LastExecution testRun={testRun} reportUrl={reportUrl} />}
        <InfoRow label={t("gzl.user.interface.last_execution_date")} value={lastExecutionDate ? formatDate(lastExecutionDate) : ""} />
        <InfoRow label={t("gzl.user.interface.owner")} value={ownerName} />
        {testRun?.accessControlList && testRun.id && (
          <AclDisplay acl={testRun.accessControlList} itemId={testRun.id} customPersist={persistTestRunExecutionAcl} disableLinkPrivacy />
        )}
        <InfoRow label={t("gzl.texec.test_name")} value={testModel.name} />

        <InfoRow label={t("gzl.texec.test_summary")} value={testModel.summary} />
        {testModel.tags && testModel.tags.length > 0 && (
          <TagSection items={testModel.tags} labelKey={t("gzl.user.interface.tags")} keyPrefix="test-tags" />
        )}
      </div>
    </div>
  );
};

export default TestRun;
