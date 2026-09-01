import { Info } from "lucide-react";
import { useTranslation } from "react-i18next";

const InfoTruncateData = () => {
  const { t } = useTranslation();
  return (
    <div className="flex items-center gap-2 ">
      <Info size={16} />
      {t("gzl.message.capture.for_performance_reason_large_node_not_expandable")}.
    </div>
  );
};

export default InfoTruncateData;
