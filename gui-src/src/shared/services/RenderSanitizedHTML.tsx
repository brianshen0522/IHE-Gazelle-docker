"use client";
import React, { useEffect, useState } from "react";
import DOMPurify from "dompurify";

interface RenderSanitizedHTMLProps {
  untrustedHTML?: string;
}

const RenderSanitizedHTML: React.FC<RenderSanitizedHTMLProps> = ({ untrustedHTML }) => {
  const [cleanHTML, setCleanHTML] = useState<string>("");

  useEffect(() => {
    if (untrustedHTML && globalThis.window !== undefined) {
      const clean = DOMPurify.sanitize(untrustedHTML);
      setCleanHTML(clean);
    }
  }, [untrustedHTML]);

  if (!untrustedHTML) {
    return <div></div>;
  }

  return <div className="w-full" dangerouslySetInnerHTML={{ __html: cleanHTML }} />;
};

export default RenderSanitizedHTML;
