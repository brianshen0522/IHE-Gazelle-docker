import CollapsableCard from "@shared/components/boxes/CollapsableCard";
import { TestRunReport } from "@maestro/types/report/TestReport";
import CollapsableSubCard from "@shared/components/boxes/CollapsableSubCard";
import { ResultBadge } from "@shared/components/report/ResultBadge";
import { useParams } from "next/navigation";
import { useTranslation } from "react-i18next";
import { DatahouseItemReference } from "@shared/types/datahouse/DatahouseItem";
import useDateFormat from "@/shared/hooks/useDateFormat";
import { PropertyRenderer } from "./PropertyRenderer";
import { UnexpectedErrorDisplay } from "@shared/components/report/UnexpectedErrorDisplay";
import { TestReportSteps } from "@test-execution/components/test-report/maestroReport/TestReportSteps";

export function TestReportRuns({
  testRunReports,
  references,
}: Readonly<{
  testRunReports: TestRunReport[];
  references: DatahouseItemReference[];
}>) {
  const { reportId: itemId } = useParams<{ reportId: string }>();
  const { t } = useTranslation();
  const formatDate = useDateFormat(false);

  if (!testRunReports || testRunReports.length === 0) {
    return (
      <CollapsableCard title="Test Runs" numberOfElements={0}>
        <p className="text-grey-500 ml-5">{t("gzl.texec.this_test_report_does_not_contain_any_test_run")}.</p>
      </CollapsableCard>
    );
  }

  return (
    <CollapsableCard title={t("gzl.texec.test_runs")} numberOfElements={testRunReports.length}>
      <div className="flex flex-col gap-4 p-2">
        {testRunReports.map((testRun, testRunIndex) => {
          const title = (
            <div className="flex flex-row w-full justify-between items-center">
              <h4 id={`test-run-${testRun.test.id}`}>{testRun.test.name}</h4>
              <div className="flex flex-row gap-10 items-center mr-5">
                <div>{formatDate(testRun.dateTime)}</div>
                <ResultBadge result={testRun.result} />
              </div>
            </div>
          );

          // Prefer testRun.id, fallback to testRun.test.id, fallback to index
          const testRunKey = testRun.test.id || testRunIndex;

          return (
            <CollapsableSubCard key={testRunKey} title={title} expanded={testRun.result !== "PASSED"}>
              <p className="text-grey-500 mx-1">{testRun.test.description}</p>

              {/* Unexpected errors */}
              <UnexpectedErrorDisplay errors={testRun.unexpectedErrors} id={`test-run-${testRunKey}-unexpected-errors`} />

              {/* Inputs */}
              {testRun.inputs && testRun.inputs.length > 0 && (
                <>
                  <p id={`test-run-${testRunKey}-inputs`} className="mt-3 font-semibold">
                    {t("gzl.texec.test_inputs")}
                  </p>
                  <ul className="list-disc pl-5">
                    {testRun.inputs.map((input, inputIndex) => (
                      <PropertyRenderer
                        key={input.name || inputIndex}
                        property={input}
                        defaultName={`Input item ${inputIndex + 1}`}
                        isInput={true}
                        itemId={itemId}
                        references={references}
                      />
                    ))}
                  </ul>
                </>
              )}

              {/* Outputs */}
              {testRun.outputs && testRun.outputs.length > 0 && (
                <>
                  <p id={`test-run-${testRunKey}-outputs`} className="mt-3 font-semibold">
                    {t("gzl.texec.test_outputs")}
                  </p>
                  <ul className="list-disc pl-5">
                    {testRun.outputs.map((output, outputIndex) => (
                      <PropertyRenderer
                        key={output.name || outputIndex}
                        property={output}
                        defaultName={`Output item ${outputIndex + 1}`}
                        isInput={false}
                        itemId={itemId}
                        references={references}
                      />
                    ))}
                  </ul>
                </>
              )}

              {/*Steps*/}
              {testRun.stepRunReports && testRun.stepRunReports.length > 0 && (
                <>
                  <p className="my-3 font-semibold">{t("gzl.texec.test_steps")}</p>
                  <TestReportSteps testRunIndex={testRunIndex} stepRunReports={testRun.stepRunReports} references={references} />
                </>
              )}
            </CollapsableSubCard>
          );
        })}
      </div>
    </CollapsableCard>
  );
}
