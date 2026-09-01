import { Messages } from "../../Types";

export type HL7V2Message = Messages & {
  content: {
    hl7MessageType: string;
    hl7Version: string;
  };
};