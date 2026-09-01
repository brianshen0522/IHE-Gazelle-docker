import { StepRunReport } from "@maestro/types/report/TestReport";
import CollapsableSubCard from "@shared/components/boxes/CollapsableSubCard";
import { ResultBadge } from "@shared/components/report/ResultBadge";
import { useParams } from "next/navigation";
import { useTranslation } from "react-i18next";
import { DatahouseItemReference } from "@shared/types/datahouse/DatahouseItem";
import { PropertyRenderer } from "./PropertyRenderer";
import { UnexpectedErrorDisplay } from "@shared/components/report/UnexpectedErrorDisplay";

export function TestReportSteps({
  testRunIndex,
  stepRunReports,
  references,
}: Readonly<{
  testRunIndex: number;
  stepRunReports: StepRunReport[];
  references: DatahouseItemReference[];
}>) {
  const { reportId: itemId } = useParams<{ reportId: string }>();
  const { t } = useTranslation();

  return (
    <div className="flex flex-col gap-3">
      {stepRunReports.map((step, stepIndex) => {
        const stepTitle = (
          <div id={`test-run-${testRunIndex}-step-${stepIndex}`} className="flex flex-row w-full justify-between items-center mr-3">
            <div className="flex flex-row w-full justify-start items-center gap-8">
              <h4>{step.stepName}</h4>
              <i className="mt-1">{step.type}</i>
            </div>
            <ResultBadge result={step.result} />
          </div>
        );

        const stepKey = `${step.stepName}-${step.type}`;

        return (
          <CollapsableSubCard key={stepKey} title={stepTitle} expanded={["FAILED", "UNDEFINED"].includes(step.result)}>
            <div className="py-2 px-3">
              {/* Unexpected errors */}
              <UnexpectedErrorDisplay errors={step.unexpectedErrors} id={`test-run-${testRunIndex}-step-${stepIndex}-unexpected-errors`} />

              {/* Outputs */}
              {step.outputs && step.outputs.length > 0 ? (
                <>
                  <p id={`test-run-${testRunIndex}-step-${stepIndex}-outputs`}>{t("gzl.texec.outputs")}:</p>
                  <ul className="list-disc pl-5">
                    {step.outputs.map((output, outputIndex) => (
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
              ) : (
                <p className="text-grey-500 ml-5">{t("gzl.texec.this_step_does_not_have_any_output")}.</p>
              )}
            </div>
          </CollapsableSubCard>
        );
      })}
    </div>
  );
}
