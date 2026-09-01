import { Modal, Button } from "@gazelle/gazelle-component-ui";
import { ResultBadge } from "@/shared/components/report/ResultBadge";
import { useTranslation } from "react-i18next";
import { TestRunExecution } from "../../types/TestRunExecution";

interface ExecutionCompletionModalProps {
  isOpen: boolean;
  execution: TestRunExecution | null;
  onClose: () => void;
  onBackToTestSuite: () => void;
  onSeeDetails: () => void;
}

export default function ExecutionCompletionModal({
  isOpen,
  execution,
  onClose,
  onBackToTestSuite,
  onSeeDetails,
}: Readonly<ExecutionCompletionModalProps>) {
  const { t } = useTranslation();

  return (
    <Modal id="execution-completion-modal" title={t("gzl.texec.execution_completed")} isOpen={isOpen} toggleModal={onClose} size="md">
      <div className="flex flex-col gap-4 w-full">
        <div className="flex items-center gap-2">
          {t("gzl.texec.execution_result")} {execution?.status && <ResultBadge result={execution.status} />}
          {t("gzl.texec.execution_result_steps")}
        </div>
        <p>{t("gzl.texec.execution_message")}</p>

        <div className="flex gap-3 justify-center mt-4">
          <Button id="back-to-suite-btn" type="button" variant="secondary" onClick={onBackToTestSuite}>
            {t("gzl.texec.back_to_test_suite")}
          </Button>
          <Button id="see-details-btn" type="button" variant="primary" onClick={onSeeDetails}>
            {t("gzl.user.interface.see_details")}
          </Button>
        </div>
      </div>
    </Modal>
  );
}
