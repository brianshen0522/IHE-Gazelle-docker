import { useState } from "react";
import { mutate } from "swr";
import { Input } from "@gazelle/gazelle-component-ui";
import { archiveOrganizationAction } from "../actions";
import ConfirmModal from "../../ConfirmModal";
import { useTranslation } from "react-i18next";

interface OrganizationArchiveModalProps {
  organization: {
    id: string;
    name: string;
  };
  isOpen: boolean;
  toggleModal: () => void;
}

const OrganizationArchiveModal = ({ organization, isOpen, toggleModal }: OrganizationArchiveModalProps) => {
  const { t } = useTranslation();
  const [archiveConfirmText, setArchiveConfirmText] = useState("");

  const handleArchiveConfirm = async () => {
    if (organization) {
      const result = await archiveOrganizationAction(organization.id);
      if (result.success) {
        // Invalidate SWR cache for organization data
        await mutate([`/gazelle/api/organizations/${organization.id}`]);
        // Invalidate SWR cache for organizations list
        await mutate((key) => Array.isArray(key) && key[0]?.toString().startsWith("/gazelle/api/organizations"));
        toggleModal();
        setArchiveConfirmText("");
      }
    }
  };

  const toggleArchiveModal = () => {
    toggleModal();
    if (!isOpen) {
      setArchiveConfirmText("");
    }
  };

  return (
    <ConfirmModal
      title={t("gzl.user.interface.confirm_archive_operation")}
      isOpen={isOpen}
      onCancel={() => {
        toggleModal();
        setArchiveConfirmText("");
      }}
      onContinue={handleArchiveConfirm}
      toggleModal={toggleArchiveModal}
      textOnContinue={`${t("gzl.user.interface.archive")} ${organization.name}`}
      disableConfirm={archiveConfirmText !== "ARCHIVE"}
      confirmVariant="danger"
    >
      <div className="flex flex-col gap-4">
        <div className="flex gap-1">
          <p>{t("gzl.user.interface.archive_organization_info_action", { organizationName: organization.name })}</p>
          <p className="font-semibold">{t("gzl.user.interface.action_irreversible")}</p>
        </div>
        <p>{t("gzl.user.interface.archive_organization_info_action_2", { organizationName: organization.name })}</p>
        <p>{t("gzl.user.interface.no_data_will_be_lost")}</p>
        <div>
          <Input
            id="archive-confirm-input"
            type="text"
            label={t("gzl.user.interface.type_archive_to_confirm")}
            value={archiveConfirmText}
            setValue={setArchiveConfirmText}
            placeholder="ARCHIVE"
          />
        </div>
      </div>
    </ConfirmModal>
  );
};

export default OrganizationArchiveModal;
