"use client";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { ContentHeader, Skeleton } from "@gazelle/gazelle-component-ui";
import { EditUserContextProvider } from "@user-management/context/EditUserContext";
import EditUser from "@user-management/components/user-management/EditUser";
import { useTranslation } from "react-i18next";
import { useRouter, useSearchParams } from "next/navigation";
import { Session } from "next-auth";
import { useSession } from "next-auth/react";
import { useGetUserById, useGetUserPicture } from "@/app/user-management/hooks/swr/useGetUser";

const UserInformation = () => {
  const { t } = useTranslation();
  const router = useRouter();
  const searchParams = useSearchParams();
  const { data: session, status } = useSession() as { data: Session; status: string };
  const userId = searchParams.get("userId");
  const { data: currentUser, isLoading } = useGetUserById(userId ?? "");
  const { data: userPictureUrl, isLoading: isLoadingPicture } = useGetUserPicture(userId ?? "", "normal");

  const breadcrumbItems = [
    {
      label: t("gzl.gum.users_list"),
      url: "/user-management/users",
    },
    {
      label: `${currentUser?.data?.firstName ?? ""} ${currentUser?.data?.lastName ?? ""}`.trim(),
      url: "",
    },
  ];

  if (status === "loading" || !session || isLoading || isLoadingPicture) {
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
        id={t("gzl.gum.go_back")}
        onGoBack={() => {
          router.back();
        }}
        breadcrumbs={breadcrumbItems}
        title={t("gzl.gum.user_details")}
        content={t("gzl.gum.go_back")}
        isGoBack={true}
      />
      <EditUser userPictureUrl={userPictureUrl?.data} isSidePanelContext={false} account={false} />
      <ToastContainer />
    </EditUserContextProvider>
  );
};

export default UserInformation;
