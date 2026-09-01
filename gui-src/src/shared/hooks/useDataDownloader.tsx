import { useEffect, useState } from "react";
import { toast } from "react-toastify";

const useDataDownloader = (data: string, fileName: string, appType: string) => {
  const [downloadUrl, setDownloadUrl] = useState("");

  useEffect(() => {
    if (!data) return;
    let url: string | undefined;
    try {
      const blob = new Blob([data], { type: appType });
      url = globalThis.URL.createObjectURL(blob);
      setDownloadUrl(url);
    } catch (error) {
      toast.error("Error in processing the content: " + error);
    }
    return () => {
      if (url) {
        globalThis.URL.revokeObjectURL(url);
      }
    };
  }, [appType, data]);

  return () => {
    const link = document.createElement("a");
    link.href = downloadUrl;
    link.download = fileName;
    link.click();
  };
};

export default useDataDownloader;
