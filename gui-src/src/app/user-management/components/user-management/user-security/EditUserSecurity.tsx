import { Button, SectionTitle, ToggleSwitch } from "@gazelle/gazelle-component-ui";
import { Mail } from "lucide-react";
import { useRouter } from "next/navigation";
import { useSession } from "next-auth/react";
import { useTranslation } from "react-i18next";
import { toast } from "react-toastify";
import { useEditUserContext } from "@user-management/context/EditUserContext";
import useEnv from "@user-management/hooks/useEnv";
import { GAZELLE_ADMIN } from "@home/utils/permissions";
import { updateUserActivationStatus } from "../actions";
import { Route } from "next";
import DeleteUserModal from "./DeleteUserModal";
import { EditUserSecurityProps } from "./types";
import { mutate } from "swr";

const EditUserSecurity = ({ user, delegated, account }: EditUserSecurityProps) => {
  const { t } = useTranslation();
  const { data: session } = useSession();
  const { envKcBaseUrl } = useEnv();
  const { gazelleId } = session?.user || {};

  const router = useRouter();
  const { userActivation, setUserActivation } = useEditUserContext();

  const handleUpdateActivationStatusUser = async (activation: boolean) => {
    await updateUserActivationStatus(activation, user.id);
    setUserActivation(activation);
    mutate((key) => typeof key === "string" && key.includes("/gazelle/api/items") && key.includes("type=users"));
    if (activation) {
      toast.success(t("gzl.gum.account_activated"));
    } else {
      toast.success(t("gzl.gum.account_disabled"));
    }
  };

  const handleResetPassword = () => {
    router.push((envKcBaseUrl + "/login-actions/reset-credentials?client_id=gazelle-account") as Route);
  };

  const canResetPassword = user?.delegated === false && user?.id === gazelleId;

  const canDeleteUser = () => {
    const groups = session?.user.groups;
    return groups?.includes(GAZELLE_ADMIN) || user?.id === gazelleId;
  };

  return (
    <div className="flex flex-col gap-4">
      <SectionTitle id={t("gzl.gum.security")} title={t("gzl.gum.security")} />
      {!(account || delegated) && (
        <ToggleSwitch
          id="activation-toggle"
          label={userActivation ? t("gzl.gum.account_activated") : t("gzl.gum.account_disabled")}
          name="activation-toggle"
          checked={userActivation}
          onChange={() => handleUpdateActivationStatusUser(!userActivation)}
        />
      )}

      {canResetPassword && (
        <div className="flex flex-wrap items-center gap-2 w-full">
          <p>{t("gzl.gum.send_reset_password")}</p>
          <Button
            id="tmp"
            type="button"
            ariaLabel={t("gzl.gum.reset_password")}
            variant="secondary"
            onClick={handleResetPassword}
            title={t("gzl.gum.reset_password")}
          >
            <Mail className="inline mr-2" size={16}></Mail>
            {gazelleId === user?.id ? t("gzl.gum.update_password") : t("gzl.gum.reset_password")}
          </Button>
        </div>
      )}

      {canDeleteUser() ? (
        <div className="flex flex-wrap items-center gap-2 justify-between">
          <p>{t("gzl.gum.delete_user")}</p>
          <DeleteUserModal user={user} session={session} account={account} />
        </div>
      ) : (
        <br />
      )}
    </div>
  );
};
export default EditUserSecurity;
