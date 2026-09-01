import { Session } from "next-auth";
import { User } from "../Types";

export interface DeleteUserModalProps {
  user: User;
  session: Session | null;
  account?: boolean;
}

export interface EditUserSecurityProps {
  user: User;
  delegated: boolean;
  account: boolean;
}
