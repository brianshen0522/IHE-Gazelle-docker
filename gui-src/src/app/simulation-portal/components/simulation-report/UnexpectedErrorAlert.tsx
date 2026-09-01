import { useTranslation } from "react-i18next";

const UnexpectedErrorAlert = ({ hasUnexpectedErrors }: { hasUnexpectedErrors: () => boolean }) => {
  const { t } = useTranslation();

  if (!hasUnexpectedErrors()) {
    return null;
  }

  const scrollIntoFirstUnexpectedError = () => {
    const element = document.getElementsByClassName("unexpected-error");
    if (element && element.length > 0) {
      (element[0] as HTMLElement).scrollIntoView({ behavior: "smooth", block: "center" });
    }
  }

  return (
    <div className="border-1 border-red rounded-small px-1.5 py-1 my-3 w-fit">
      <p className="flex gap-2 text-red">
        {"Unexpected error were encountered during execution."}
        <a className="text-blue hover:text-visited_link cursor-pointer" onClick={scrollIntoFirstUnexpectedError}>
          {t("gzl.texec.read_more_details")}.
        </a>
      </p>
    </div>
  );
};

export default UnexpectedErrorAlert;