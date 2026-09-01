// TODO: Move this component to UI library
import { useTranslation } from "react-i18next";
import { UnexpectedError } from "@maestro/types/report/UnexpectedError";

const UnexpectedErrorsDisplay = ({ errors }: { errors: UnexpectedError[] }) => {
  const { t } = useTranslation();

  const displayError = (error: UnexpectedError, index: number, causedBy = false) => (
    <span key={index} className="flex break-words">
      {`${(causedBy && t("gzl.texec.caused_by")) || ""}${error.name ?? ""} : ${error.message}`}{" "}
      {error.cause && displayError(error.cause, index, false)}{" "}
    </span>
  );

  return (
    <div className="flex flex-col mt-1.5 border-1 border-red rounded-small p-2 bg-red bg-opacity-10 text-red unexpected-error">
      <p className="min-w-28 flex-shrink-0">{t("gzl.texec.unexpected_errors")}</p>
      {errors.map((error, index) => displayError(error, index))}
    </div>
  );
};

export default UnexpectedErrorsDisplay;
