import { JSX } from "react";
import { AccessControlList } from "@/shared/types/AccessControlListTypes";
import { HttpData } from "./messages/http/Types";
import { HL7V2Message } from "./messages/hl7/Types";
import { SyslogMessage } from "./messages/syslog/Types";
import { TcpMessage } from "./messages/tcp/Types";
import { ConnectionError } from "./error/Types";
import { Part } from "./messages/multiparts/Types";
import { DicomMessage } from "./messages/dicom/Types";

export * from "./messages/http/Types";
export * from "./messages/hl7/Types";
export * from "./messages/syslog/Types";
export * from "./messages/tcp/Types";

export type Renderers = "raw" | "text dump" | "xml" | "xml dump" | "tree" | "hex" | "hex dump" | "json";

export type MsgOverviewProps = {
  selectedRow: any;
};

export type MsgOverviewPropsConnect = {
  selectedRow: any;
  id: string;
  connectionId: string;
};

// Type definitions presentation schemas
export type PresentationSchema = {
  type: string;
  name: string;
  contentPaths: string[];
  itemPaths: string[];
};

export type ProtectedResource = {
  accessControlList: AccessControlList;
};

export type ChannelType = "DICOM" | "HTTP" | "HL7v2" | "SYSLOG" | "TCP";

export type MessageRenderProps = {
  data: {
    channelType: ChannelType;
    type: string;
    id: string;
    content: {
      [key: string]: any;
      additionalParameters?: {
        [key: string]: string;
      };
    };
    references: ReferenceType[];
  };
  tab: string;
};

export type DataMessageProps = Omit<MessageRenderProps, "tab">;

// Shared type definitions for a proxy message
export type Messages = ProtectedResource & {
  id: string;
  type: string;
  date: number;
  content: {
    type: string;
    content: string | null;
    captureDate: number;
    sender: {
      ip: string;
      hostname: string;
      port: number;
      systemId: string | null;
      organizationId: string | null;
    };
    receiver: {
      ip: string;
      hostname: string;
      port: number;
      systemId: string | null;
      organizationId: string | null;
    };
    initiator: Record<string, string>;
    responder: Record<string, string>;
    unexpectedErrors: string | null;
    additionalParameters: { key: string; value: string }[] | null;
    proxyPort: number;
    channelType: string;
    secured: boolean;
    mtls: boolean;
    action?: React.ReactNode[];
    rootType?: string;
  };
  additionalParameters: { key: string; value: string }[] | null;
  references: {
    id: string;
    type: string;
    value: string;
    content: {
      channelSummary: { type: string; proxyPort: number };
    };
  }[];
};

export type RenderersConfig = Record<string, JSX.Element>;

// Type definitions for the message/current item
export type ReferenceType = {
  value: string;
  refType: string;
  type: string;
};

export type ItemType = {
  id: string;
  content: any;
  type?: string;
  references: Array<ReferenceType>;
};

export type currentItemType<T> = Readonly<T>;

export type Tabs = "Content" | "Validation";

export interface Item {
  id: string;
  channelType: ChannelType;
  content: {
    type: string;
    commandField?: string;
    sender?: { ip: string };
    receiver?: { ip: string };
    hl7MessageType?: string;
  };
  type: string;
  references: ReferenceType[];
}

export interface BaseAttachmentProps {
  itemId: string;
  attachmentId: string;
}

interface Reference {
  value: string;
}

interface HostInfo {
  hostname: string;
  ip: string;
  port: number;
}

interface ItemContent {
  sender: HostInfo;
  receiver: HostInfo;
  initiator: HostInfo;
  responder: HostInfo;
  channelType: string;
}

export interface ItemRow {
  id: string;
  content: ItemContent;
  references: Reference[];
}

export type MessageContent = { content: string; type: string };

export interface DownloadContentButtonProps {
  content: any; // Will be defined as ContentType below
  messageId?: string;
  attachmentId: string;
}

// Union type for all proxy message types - defined here to avoid circular deps
export type ProxyMessages = Messages | HttpData | HL7V2Message | SyslogMessage | DicomMessage | TcpMessage | ConnectionError;

// ContentType that depends on Part from multiparts
export type ContentType = Part | MessageContent;
