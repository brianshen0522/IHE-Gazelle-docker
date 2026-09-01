import CheckboxOption from "@shared/CheckboxOption";
import { useTranslation } from "react-i18next";

interface GroupingToggleAssertionProps {
  value: "report" | "severity";
  onChange: (value: "report" | "severity") => void;
}

const GroupingToggleAssertion = ({ value, onChange }: GroupingToggleAssertionProps) => {
  const { t } = useTranslation();

  return (
    <div className="flex items-center gap-5 my-3 ml-5">
      <CheckboxOption
        id="groupBySeverity"
        type="checkbox"
        htmlFor="groupBySeverity"
        checked={value === "severity"}
        onChange={() => onChange("severity")}
      >
        {t("gzl.user.interface.group_by_severity")}
      </CheckboxOption>

      <CheckboxOption id="groupByReport" type="checkbox" htmlFor="groupByReport" checked={value === "report"} onChange={() => onChange("report")}>
        {t("gzl.user.interface.group_by_report")}
      </CheckboxOption>
    </div>
  );
};

export default GroupingToggleAssertion;
