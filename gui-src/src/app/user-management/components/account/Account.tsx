"use client";
import { ContentHeader, Skeleton } from "@gazelle/gazelle-component-ui";
import EditUser from "@user-management/components/user-management/EditUser";
import { useSearchParams, useRouter } from "next/navigation";
import { useTranslation } from "react-i18next";
import { Session } from "next-auth";
import { useSession } from "next-auth/react";
import { EditUserContextProvider } from "@user-management/context/EditUserContext";
import { useGetUserById, useGetUserPicture, useGetUserPreferencesById } from "@/app/user-management/hooks/swr/useGetUser";
import { useUnsavedChanges } from "@shared/context/UnsavedChangeContext";
import { Route } from "next";

const Account = () => {
  const { t } = useTranslation();
  const searchParams = useSearchParams();
  const router = useRouter();
  const { data: session } = useSession() as { data: Session };
  const userId = session?.user.gazelleId;
  const { handleNavigation } = useUnsavedChanges();
  const { data: currentUser, isLoading } = useGetUserById(userId);
  const { isLoading: isLoadingPref } = useGetUserPreferencesById(userId);
  const { data: userPictureUrl, isLoading: isLoadingPicture } = useGetUserPicture(userId, "normal");

  const originSearchParam: string | null = searchParams.get("origin") ?? null;

  if (!session || isLoading || isLoadingPref || isLoadingPicture) {
    return (
      <div className="flex flex-col gap-2 w-full p-2">
        <Skeleton className="h-8" />
        <Skeleton className="h-screen" />
      </div>
    );
  }

  return (
    <EditUserContextProvider user={currentUser?.data}>
      <ContentHeader
        id={t("gzl.gum.my_account")}
        title={t("gzl.gum.my_account")}
        onGoBack={() => {
          handleNavigation(() => {
            originSearchParam === null || originSearchParam === undefined ? router.back() : router.push(originSearchParam as Route);
          });
        }}
        content={t("gzl.gum.go_back")}
        isGoBack={true}
      />

      <EditUser userPictureUrl={userPictureUrl?.data} isSidePanelContext={false} account={true} />
    </EditUserContextProvider>
  );
};

export default Account;
