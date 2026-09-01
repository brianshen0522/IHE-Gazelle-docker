import { useTranslation } from "react-i18next";

const DecompressedMsg = () => {
  const { t } = useTranslation();

  return (
    <div className="text-orange border-2 border-yellow rounded-lg p-2">
      {t("gzl.message.capture.for_convenience_content_decoded_can_download_original")}
    </div>
  );
};

export default DecompressedMsg;
