import { Messages } from "../../Types";

export type SyslogMessage = Messages & {
  content: {
    timestamp: string;
    hostName: string;
    appName: string;
    procId: string;
    messageId: string;
    tag: string | null;
    payLoad: string;
    facility: number;
    severity: number;
  };
};