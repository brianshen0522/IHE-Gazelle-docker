import { SidePanel, useSidePanel } from "@gazelle/gazelle-component-ui";
import { useTranslation } from "react-i18next";
import { EditUserContextProvider } from "@user-management/context/EditUserContext";
import EditUser from "@user-management/components/user-management/EditUser";
import { User } from "@/app/user-management/components/user-management/Types";
import { useUnsavedChanges } from "@shared/context/UnsavedChangeContext";
import { useCreateQuery } from "@hooks/useCreateQuery";

const UserSidePanel = () => {
  const { isOpen, setIsOpen, selectedRow } = useSidePanel<User>();
  const { t } = useTranslation();
  const userId = selectedRow?.id;
  const { createQuery } = useCreateQuery();
  const { handleNavigation } = useUnsavedChanges();

  const accessDetailsProps = {
    id: userId ?? "",
    content: t("gzl.gum.user"),
    pathname: "/user-management/user",
    query: { userId: userId ?? "" },
    detailsText: "information",
  };

  const handleClose = () => {
    handleNavigation(() => {
      setIsOpen(false);
      createQuery({ row: null });
    });
  };

  // If no channel is selected, we don't render the side panel content at first render
  const hasContent = !!selectedRow;

  return (
    <SidePanel isOpen={isOpen} className="bg-white">
      {hasContent ? (
        <EditUserContextProvider key={selectedRow?.id} user={selectedRow}>
          <SidePanel.Header accessDetailsProps={accessDetailsProps} onClose={handleClose} />

          <EditUser isSidePanelContext={true} account={false} />
        </EditUserContextProvider>
      ) : (
        <></>
      )}
    </SidePanel>
  );
};

export default UserSidePanel;
