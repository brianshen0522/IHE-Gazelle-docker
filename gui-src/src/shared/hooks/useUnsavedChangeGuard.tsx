import React, { JSX, useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { usePathname } from "next/navigation";
import ConfirmModal from "@user-management/components/ConfirmModal";

interface NavigationAction {
  callback: () => void;
  isBackNavigation?: boolean;
}

export interface UnsavedChangesGuard {
  hasUnsavedChanges: boolean;
  setHasUnsavedChanges: (value: boolean) => void;
  handleNavigation: (callback: () => void) => boolean;
  ConfirmationModal: () => JSX.Element | null;
}

export const useUnsavedChangesGuard = (): UnsavedChangesGuard => {
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
  const [pendingAction, setPendingAction] = useState<NavigationAction | null>(null);
  const { t } = useTranslation();
  const pathname = usePathname();

  // Reset unsaved changes when URL changes
  useEffect(() => {
    setHasUnsavedChanges(false);
    setPendingAction(null);
  }, [pathname]);

  // Handle browser refresh/close
  useEffect(() => {
    const handleBeforeUnload = (e: BeforeUnloadEvent) => {
      if (hasUnsavedChanges) {
        e.preventDefault();
        // Modern browsers ignore custom messages and show their own generic message
        return "You have unsaved changes. Are you sure you want to leave?";
      }
    };

    window.addEventListener("beforeunload", handleBeforeUnload);
    return () => window.removeEventListener("beforeunload", handleBeforeUnload);
  }, [hasUnsavedChanges]);

  // Handle browser back/forward buttons
  useEffect(() => {
    if (!hasUnsavedChanges) return;

    const handlePopState = () => {
      if (hasUnsavedChanges) {
        globalThis.history.pushState(null, "", globalThis.location.pathname + globalThis.location.search);

        setPendingAction({
          callback: () => {
            setHasUnsavedChanges(false);
            globalThis.history.back();
          },
          isBackNavigation: true,
        });
      }
    };

    // Push initial state to enable popstate detection
    globalThis.history.pushState(null, "", globalThis.location.pathname + globalThis.location.search);
    globalThis.addEventListener("popstate", handlePopState);
    return () => {
      globalThis.removeEventListener("popstate", handlePopState);
    };
  }, [hasUnsavedChanges]);

  // Handle in-app navigation - uses custom modal
  const handleNavigation = useCallback(
    (callback: () => void) => {
      if (hasUnsavedChanges) {
        setPendingAction({ callback });
        return false;
      } else {
        callback();
        return true;
      }
    },
    [hasUnsavedChanges],
  );

  const confirmNavigation = useCallback(() => {
    if (pendingAction) {
      setHasUnsavedChanges(false);
      pendingAction.callback();
      // Double back for browser back button (compensates for the pushState we added)
      if (pendingAction.isBackNavigation) {
        setTimeout(() => globalThis.history.back(), 0);
      }
      setPendingAction(null);
    }
  }, [pendingAction]);

  const cancelNavigation = useCallback(() => {
    setPendingAction(null);
  }, []);

  const ConfirmationModal = useCallback(() => {
    if (!pendingAction) return null;

    const modalBody = (
      <div className="flex flex-col gap-2 w-full">
        <p>{t("gzl.gum.unsaved_changes_leave_edition")}</p>
        <p>{t("gzl.gum.confirm_leave_edition")}</p>
      </div>
    );
    return (
      <ConfirmModal
        title={t("gzl.gum.unsaved_changes")}
        isOpen={!!pendingAction}
        toggleModal={cancelNavigation}
        onCancel={cancelNavigation}
        textOnCancel="gzl.gum.keep_edition"
        textOnContinue="gzl.gum.leave_edition"
        onContinue={confirmNavigation}
      >
        {modalBody}
      </ConfirmModal>
    );
  }, [pendingAction, t, confirmNavigation, cancelNavigation]);

  return {
    hasUnsavedChanges,
    setHasUnsavedChanges,
    handleNavigation,
    ConfirmationModal,
  };
};
