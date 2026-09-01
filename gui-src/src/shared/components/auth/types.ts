import { DefaultSession, DefaultUser } from "next-auth";
import { DefaultJWT } from "next-auth/jwt";

declare module "next-auth" {
  export interface Session extends DefaultSession {
    access_token: string;
    refresh_token: string;
    expires_in: number;
    id_token: string;
    user: {
      gazelleId: string;
      id: string;
      name: string;
      groups: string[];
      organization: string;
      email: string;
    };
    error: string;
  }

  export interface User extends DefaultUser {
    gazelleId: string;
    name: string;
    groups: string[];
    organization: string;
    email: string;
  }
}

declare module "next-auth/jwt" {
  export interface JWT extends DefaultJWT {
    access_token: string;
    refresh_token: string;
    expires_at: number; //The timestamp of when the token expires
    id_token: string;
    gazelleId: string;
    name: string;
    groups: string[];
    organization: string;
    email: string;
    error: string;
  }
}
