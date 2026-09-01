import { Item } from "@/app/message-capture/components/proxy/Types";

export type ConnectionDetailsProps = {
  selectedRow?: any;
  data?: Record<string, any>;
  connection?: Record<string, any>;
};

export interface ConnectionInfoWrapperProps {
  connectionItem: Record<string, any>;
  messageItem: Item;
}

export type HostTypeProps = {
  data: Record<string, any>;
  hostType: "initiator" | "responder";
  connection: Record<string, any>;
};

export type Certificate = {
  subject: string;
};

export type SenderReceiverProps = {
  ip?: string;
  hostname: string;
  port?: number;
  host: string;
};
