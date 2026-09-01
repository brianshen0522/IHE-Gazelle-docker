import { JSX } from "react";
import { ReferenceType } from "@/app/message-capture/components/proxy/Types";
import { Renderers } from "../../Types";

export type PartType = "DICOM_PART" | "BINARY_PART" | "TEXT_PART" | "PART" | "XOP_PART";

// Type definitions for the HTTP multiparts
export type Part = {
  id: string;
  type: string;
  content: string;
  headers: Record<string, string>;
  xmlDataSetDump: string;
  date: string;
  references: ReferenceType[];
  unexpectedErrors: { rootType: string; list?: { message: string; cause: { message: string } }[] };
  contentType: string;
  raw: string;
  syntax: string;
};

export type MultiPartsProps = {
  hasHTTPParts: boolean;
  id: string | null;
};

export interface MultiPartContentProps {
  content: Part;
  sectionId: string;
}

// Define and export the type alias
export type MultiPartRenderersConfig = Record<
  PartType,
  {
    specificRenderers: Renderers[];
    renderers: Record<string, JSX.Element>;
  }
>;
