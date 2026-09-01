import { Checkbox, Input, SectionTitle } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import SpokenLanguage from "./SpokenLanguage";
import { useGetUserPreferencesById } from "@user-management/hooks/swr/useGetUser";
import { useEffect, useState } from "react";

interface EditUserPreferencesProps {
  userId: string | undefined;
  userActivation: boolean;
  onPreferencesChange: () => void;
}

const EditUserPreferences = ({ userId, userActivation, onPreferencesChange }: EditUserPreferencesProps) => {
  const { t } = useTranslation();
  const { data } = useGetUserPreferencesById(userId ?? "");
  const preferences = data?.data;
  const [tableLabel, setTableLabel] = useState("");
  const [notificationByEmail, setNotificationByEmail] = useState(false);
  const [languagesSpoken, setLanguagesSpoken] = useState<string[]>([]);

  // Sync form state when SWR data changes
  useEffect(() => {
    if (preferences) {
      setTableLabel(preferences.tableLabel ?? "");
      setNotificationByEmail(preferences.notifiedByEmail ?? false);
      setLanguagesSpoken(preferences.languagesSpoken?.length === 1 && preferences.languagesSpoken[0] === "" ? [] : preferences.languagesSpoken || []);
    }
  }, [preferences]);

  return (
    <div className="flex flex-col gap-4 mb-2">
      <SectionTitle id={t("gzl.gum.user_preferences")} title={t("gzl.gum.user_preferences")} />
      <Input
        id="tableLabel"
        htmlFor="tableLabel"
        type="text"
        label={t("gzl.gum.table_label")}
        placeholder={t("gzl.gum.table_label")}
        name="tableLabel"
        value={tableLabel}
        isValidInput={tableLabel.length <= 255}
        validationMessage={t("gzl.gum.table_label_max_length")}
        setValue={(value) => {
          setTableLabel(value);
          onPreferencesChange();
        }}
        disabled={!userActivation}
      />

      <div className="font-bold">{t("gzl.gum.notifications")}</div>
      <Checkbox
        id="notificationByEmail"
        label={t("gzl.gum.send_notifications_email")}
        value={notificationByEmail}
        disabled={!userActivation}
        onChange={() => {
          setNotificationByEmail(!notificationByEmail);
          onPreferencesChange();
        }}
      />

      {/* Hidden inputs to store form values for submission */}
      <input type="hidden" name="notificationByEmail" value={notificationByEmail ? "on" : ""} />
      <input type="hidden" name="languagesSpoken" value={JSON.stringify(languagesSpoken)} />

      <SpokenLanguage
        userActivated={userActivation}
        languagesSpoken={languagesSpoken}
        setLanguagesSpoken={(languages) => {
          setLanguagesSpoken(languages);
          onPreferencesChange();
        }}
      />
    </div>
  );
};
export default EditUserPreferences;
