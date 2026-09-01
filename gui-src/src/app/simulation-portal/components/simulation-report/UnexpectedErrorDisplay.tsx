import { useTranslation } from "react-i18next";
import { UnexpectedError } from "@maestro/types/report/UnexpectedError";

const UnexpectedErrorsDisplay = ({ errors }: { errors: UnexpectedError[] }) => {
  const titleCss = "min-w-28 flex-shrink-0";
  const { t } = useTranslation();

  if (errors.length === 0) return null;

  const displayError = (error: UnexpectedError, index: number, causedBy = false) => (
    <div key={index}>
      <span className="flex break-words">{`${(causedBy && t("gzl.texec.caused_by")) || ""}${error.name ?? ""} : ${error.message}`}</span>
      {error.cause && displayError(error.cause, index, false)}
    </div>
  );

  return (
    <div className="flex flex-col mt-1.5 border-1 border-red rounded-small p-2 bg-red bg-opacity-10 text-red unexpected-error">
      <b className={titleCss}>{t("gzl.texec.unexpected_errors")}</b>
      {errors.map((error, index) => displayError(error, index))}
    </div>
  );
};

export default UnexpectedErrorsDisplay;
