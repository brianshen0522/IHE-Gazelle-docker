import { useMemo } from "react";
import { UploadFile, CollapsableSubCard, Skeleton } from "@gazelle/gazelle-component-ui";
import { JsonRenderer } from "@shared/components/renderers/json/JsonRenderer";
import { XmlRenderer } from "@shared/components/renderers/xml/XmlRenderer";
import { RawRenderer } from "@shared/components/renderers/raw/RawRenderer";
import { HexRenderer } from "@message-capture/components/proxy/renderers/hex/HexRenderer";
import { inspectForRendering } from "@/shared/utils/fileInspection/detectContent";
import { SupportedInput } from "../../types/ValidationProfile";
import { useTranslation } from "react-i18next";

const MAX_PREVIEW_SIZE = 2 * 1024 * 1024; // 2MB

const ConfigurationInputItem = ({
  input,
  data,
  isPending,
  reviewEnabled,
  onFileChange,
}: {
  input: SupportedInput;
  data: { file: File; content: string } | undefined;
  isPending: boolean;
  reviewEnabled: boolean;
  onFileChange: (file: File | null) => Promise<void>;
}) => {
  const { t } = useTranslation();
  const isFileTooLarge = data && data.file.size > MAX_PREVIEW_SIZE;

  // Only compute when data exists and not pending - useMemo to cache the result
  const { base64Content, fileType } = useMemo(() => {
    if (!data || isPending || isFileTooLarge) {
      return { base64Content: "", fileType: null };
    }

    const type = inspectForRendering(data.content);

    return { base64Content: data.content, fileType: type };
  }, [data, isPending, isFileTooLarge]);

  return (
    <CollapsableSubCard
      title={
        <div className="font-semibold">
          {input.label}
          {input.required !== false && <span className="ml-1">*</span>}
          {data?.file.name && <span className="ml-2 font-normal text-sm text-gray-600">{data.file.name}</span>}
        </div>
      }
      className="space-y-1"
    >
      <UploadFile id={input.id} fileName={data?.file.name} onFileChange={onFileChange} />

      {isPending && <Skeleton className="h-48 w-full" />}
      {reviewEnabled && data && !isPending && isFileTooLarge && (
        <div className="bg-lightgrey rounded-lg p-4 text-center text-gray-600">
          {t("gzl.user.interface.file_too_large_to_preview", { size: (data.file.size / 1024 / 1024).toFixed(2) })}
        </div>
      )}
      {reviewEnabled && data && fileType && !isPending && !isFileTooLarge && (
        <div className="bg-lightgrey rounded-lg overflow-hidden">
          <div className="max-h-96 overflow-y-auto">
            {fileType.renderer === "JSON" && <JsonRenderer base64Data={base64Content} dataType={fileType.dataType ?? "HTTP"} linesProperties={[]} />}
            {fileType.renderer === "XML" && (
              <XmlRenderer base64Data={base64Content} dataType={fileType.dataType ?? "HTTP"} contentType="application/xml" linesProperties={[]} />
            )}
            {fileType.renderer === "HEX" && <HexRenderer base64Data={base64Content} dataType={fileType.dataType ?? "HTTP"} />}
            {fileType.renderer === "RAW" && <RawRenderer base64Data={base64Content} dataType={fileType.dataType ?? "HTTP"} linesProperties={[]} />}
          </div>
        </div>
      )}
    </CollapsableSubCard>
  );
};

export default ConfigurationInputItem;
