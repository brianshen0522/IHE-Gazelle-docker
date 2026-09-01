import { ValidationReportDTO } from "@/shared/types/validation/types";
import { useTranslation } from "react-i18next";
import { hasUnexpectedErrorsInReportTree } from "@/shared/utils/validation/reportTree";

function scrollIntoFirstUnexpectedError() {
  const element = document.getElementsByClassName("unexpected-error");
  if (element && element.length > 0) {
    (element[0] as HTMLElement).scrollIntoView({ behavior: "smooth", block: "center" });
  }
}

const UnexpectedErrorValidationAlert = ({ validationReport }: { validationReport: ValidationReportDTO }) => {
  const { t } = useTranslation();

  function hasUnexpectedErrors(validationReport: ValidationReportDTO): boolean {
    if (validationReport.counters?.numberOfUnexpectedErrors > 0) {
      return true;
    }

    return hasUnexpectedErrorsInReportTree(validationReport.reports);
  }

  if (!hasUnexpectedErrors(validationReport)) {
    return null;
  }

  return (
    <div className="border-1 border-red rounded-small px-1.5 py-1 my-3 w-fit">
      <p className="flex gap-2 text-red">
        {t("gzl.texec.one_or_more_issues_have_been_encountered_while_processing_the_validation")}
        <button className="text-blue hover:text-visited_link cursor-pointer" onClick={scrollIntoFirstUnexpectedError}>
          {t("gzl.texec.read_more_details")}.
        </button>
      </p>
    </div>
  );
};

export default UnexpectedErrorValidationAlert;
