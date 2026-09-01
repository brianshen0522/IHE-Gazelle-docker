import { Messages } from "../../Types";

export type TcpMessage = Messages & {
  content: {
    sizeOfMessage: string;
  };
};
