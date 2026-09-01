import { ChangeEvent, useEffect, useId, useRef, useState } from "react";
import { Button, IconButton, Tooltip } from "@gazelle/gazelle-component-ui";
import { Camera, LucideHelpCircle } from "lucide-react";
import Image from "next/image";
import { useTranslation } from "react-i18next";
import { toast } from "react-toastify";
import ppGazelle from "@/shared/assets/gazelle_pp.png";
import { useEditUserContext } from "@user-management/context/EditUserContext";
import { useGetUserPicture } from "@/app/user-management/hooks/swr/useGetUser";
import { deleteUserPicture } from "./actions";
import { useUnsavedChanges } from "@shared/context/UnsavedChangeContext";

type ProfilePictureProps = {
  userFirstName: string;
  userLastName: string;
};

const ProfilePicture = ({ userFirstName, userLastName }: ProfilePictureProps) => {
  const id = useId();
  const { t } = useTranslation();
  const { setHasUnsavedChanges } = useUnsavedChanges();
  const { user, setUserPictureUrl, setNewUserPicture, userActivation } = useEditUserContext();
  const { data: userPictureUrl, isError: isErrorPicture, key, mutate } = useGetUserPicture(user?.id ?? "", "normal");
  const fileInputRef = useRef<HTMLInputElement>(null);
  const tooltipTriggerRef = useRef<HTMLDivElement>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);

  useEffect(() => {
    if (userPictureUrl === ppGazelle.src) setUserPictureUrl(userPictureUrl ?? null);
  }, [setUserPictureUrl, userPictureUrl]);

  const handleFileChange = async (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0] || null;

    if (file) {
      if (file.type !== "image/jpeg" && file.type !== "image/png" && file.type !== "image/gif") {
        toast.error(t("gzl.gum.img_format_allowed"));
        return;
      }
      const twoMegaBytes = 2000000;
      if (file.size > twoMegaBytes) {
        toast.error(t("gzl.gum.file_size_exceeded"));
        return;
      }

      // Create a preview of the selected image
      const reader = new FileReader();
      reader.onloadend = () => {
        const result = reader.result as string;
        setPreviewUrl(result);
        setUserPictureUrl(result);
      };
      reader.readAsDataURL(file);

      // Read image bytes
      const arrayBuffer = await file.arrayBuffer();
      setNewUserPicture(arrayBuffer);
      mutate(key);
    }
    setHasUnsavedChanges(true);
  };

  const handleProfilePictureClick = () => {
    fileInputRef.current?.click();
  };

  const handleDeletePhoto = async () => {
    try {
      await deleteUserPicture(user?.id ?? "");
      setUserPictureUrl(null);
      setPreviewUrl(null);
      mutate(key);
      toast.success(t("gzl.gum.photo_deleted"));
    } catch (error) {
      console.error(error);
      toast.error(t("gzl.gum.photo_delete_error"));
    }
  };

  if (isErrorPicture) {
    return <div>{t("gzl.gum.photo_fetch_error")}</div>;
  }

  return (
    <div className="flex flex-col items-center gap-2">
      <div className="flex flex-wrap items-center justify-center gap-4">
        <div className="relative flex-shrink-0">
          <div className="border-2 border-lightgrey rounded-full w-24 h-24 xs:w-24 xs:h-24 sm:w-28 sm:h-28 md:w-32 md:h-32 overflow-hidden">
            <Image
              src={previewUrl ?? userPictureUrl?.data ?? ppGazelle.src}
              alt="Profile picture"
              priority={true}
              fill
              sizes="(max-width: 639px) 96px, (min-width: 640px) 96px, (min-width: 768px) 112px, (min-width: 1024px) 128px"
              className="rounded-full object-cover"
            />
          </div>
          <label htmlFor="profile-picture" className="absolute bottom-0 right-0 bg-white rounded-full shadow z-10 pointer-events-auto">
            <IconButton
              id={id}
              onClick={handleProfilePictureClick}
              variant="primary"
              type="button"
              title={t("gzl.gum.edit_photo")}
              ariaLabel="edit-profile-picture"
              disabled={!userActivation}
            >
              <Camera size={18} className={userActivation ? "hover:cursor-pointer" : "hover:not-allowed"} />
            </IconButton>
          </label>
          <input id="profile-picture" type="file" onChange={handleFileChange} ref={fileInputRef} className="hidden" />
        </div>

        <div className="flex items-center" ref={tooltipTriggerRef}>
          <div className="flex cursor-help">
            <LucideHelpCircle size={20} />
            <Tooltip id="image-format-tooltip" triggerRef={tooltipTriggerRef} content={t("gzl.gum.img_format_allowed")} position="bottom">
              {t("gzl.gum.img_format_allowed")}
            </Tooltip>
          </div>

          <Button
            id={id}
            className="bg-white text-red hover:underline border-none disabled:bg-white flex-shrink"
            title={t("gzl.gum.delete_photo")}
            ariaLabel="remove photo"
            type="button"
            onClick={handleDeletePhoto}
            disabled={!userActivation}
          >
            {t("gzl.gum.delete_photo")}
          </Button>
        </div>
      </div>

      <div className="text-md">
        {userFirstName} {userLastName}
      </div>
    </div>
  );
};

export default ProfilePicture;
