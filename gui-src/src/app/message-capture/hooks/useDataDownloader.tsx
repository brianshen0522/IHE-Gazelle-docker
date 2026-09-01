import { useState, useEffect } from "react";

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
      console.error("Error in processing the content:", error);
    }
    return () => {
      if (url) {
        globalThis.URL.revokeObjectURL(url);
      }
    };
  }, [appType, data]);

  const download = () => {
    const link = document.createElement("a");
    link.href = downloadUrl;
    link.download = fileName;
    link.click();
  };

  return download;
};

export default useDataDownloader;
