import { useState, useId } from "react";
import { Button, Modal } from "@gazelle/gazelle-component-ui";
import { Trash, TriangleAlert } from "lucide-react";
import { signOut } from "next-auth/react";
import { deleteUser } from "../actions";
import { toast } from "react-toastify";
import { DeleteUserModalProps } from "./types";
import { useTranslation } from "react-i18next";

const DeleteUserModal = ({ user, session, account }: DeleteUserModalProps) => {
  const { t } = useTranslation();
  const id = useId();
  const [openModal, setOpenModal] = useState(false);

  const handleDeleteUser = async () => {
    if (user?.id) {
      try {
        await deleteUser(user.id);
        if (account) {
          toast.success(t("gzl.gum.your_account_deleted"));
          await signOut({ callbackUrl: "/", redirect: true });
        } else {
          toast.success(t("gzl.gum.account_deleted"));
          globalThis.location.replace("/gazelle/user-management/users");
        }
      } catch (error: unknown) {
        console.error(error);
        toast.error(error instanceof Error ? error.message : t("gzl.gum.account_deletion_failed"));
      }
    }
  };

  return (
    <Modal
      id={id}
      title={
        <div className="flex items-center gap-2">
          <TriangleAlert /> {t("gzl.gum.warning")}
        </div>
      }
      size="md"
      isOpen={openModal}
      toggleModal={() => setOpenModal(!openModal)}
      trigger={
        <Button
          id={id}
          variant="danger"
          title={t("gzl.gum.delete_account_warning")}
          ariaLabel="delete-account"
          type="button"
          onClick={() => setOpenModal(!openModal)}
        >
          <Trash size={16} />
          {session?.user.gazelleId === user?.id ? t("gzl.gum.delete_my_account") : t("gzl.gum.delete_account")}
        </Button>
      }
    >
      <div className="flex flex-col gap-4 w-full">
        <div className="text-md">{t("gzl.user.interface.delete_account_warning", { userName: user?.firstName + " " + user?.lastName })}</div>

        <div className="flex gap-4 justify-between">
          <Button
            id={id}
            variant="default"
            title={t("gzl.user.interface.keep_user_account")}
            ariaLabel="cancel-delete"
            type="button"
            onClick={() => setOpenModal(false)}
          >
            {t("gzl.user.interface.keep_user_account")}
          </Button>
          <Button
            id={id}
            variant="danger"
            title={t("gzl.user.interface.delete_user_account")}
            ariaLabel="delete-account"
            type="button"
            onClick={() => handleDeleteUser()}
          >
            {t("gzl.user.interface.delete_user_account")}
          </Button>
        </div>
      </div>
    </Modal>
  );
};

export default DeleteUserModal;
