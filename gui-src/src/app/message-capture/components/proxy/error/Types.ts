import { Messages } from "../Types";

export type ConnectionError = Messages & {
  content: {
    rootType: string;
  };
};
