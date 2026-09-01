import { Account, Session, User } from "next-auth";
import { JWT } from "next-auth/jwt";
import { type OAuthConfig } from "next-auth/providers/oauth";
import KeycloakProvider, { type KeycloakProfile } from "next-auth/providers/keycloak";

const keycloakOptions = {
  async profile(profile: KeycloakProfile) {
    return {
      id: profile.sub,
      name: profile.name,
      gazelleId: profile.preferred_username,
      email: profile.email,
      groups: profile.groups,
      organization: profile.organization,
      id_token: profile.id_token,
      access_token: profile.access_token,
      refresh_token: profile.refresh_token,
      expires_in: profile.expires_in,
    };
  },
  clientId: process.env.KEYCLOAK_CLIENT_ID ?? "",
  clientSecret: process.env.KEYCLOAK_CLIENT_SECRET ?? "",
  issuer: process.env.KEYCLOAK_ISSUER,
  authorization: { params: { scope: "microprofile-jwt openid" } },
};

/**
 * Takes a token, and returns a new token with updated
 * `access_token` and `access_token_expires`. If an error occurs,
 * returns the old token and an error property
 */
async function refreshAccessToken(token: JWT) {
  try {
    const url = keycloakOptions.issuer + "/protocol/openid-connect/token";
    const encodedParams = new URLSearchParams();
    encodedParams.set("grant_type", "refresh_token");
    encodedParams.set("client_id", keycloakOptions.clientId ?? "");
    encodedParams.set("refresh_token", token.refresh_token);
    const response = await fetch(url, {
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      method: "POST",
      body: encodedParams,
    });

    if (!response.ok) {
      const errorData = await response.json();
      console.error("Unable to refresh token:", errorData);
      return { ...token, access_token: "", error: "RefreshAccessTokenError" };
    }

    const refreshedTokens = await response.json();
    console.warn("Token refreshed successfully, expires_at=", Date.now() + refreshedTokens.expires_in * 1000);
    return {
      ...token,
      access_token: refreshedTokens.access_token,
      expires_at: Date.now() + refreshedTokens.expires_in * 1000,
      refresh_token: refreshedTokens.refresh_token ?? token.refresh_token,
    };
  } catch (error) {
    console.error("Error refreshing token:", error);
    return { ...token, access_token: "", error: "RefreshAccessTokenError" };
  }
}

export const authOptions = {
  providers: [KeycloakProvider(keycloakOptions)],
  callbacks: {
    async jwt({ token, account, user }: { token: JWT; account: Account | null; user?: User }) {
      if (account && user) {
        token.id_token = account.id_token as string;
        token.access_token = account.access_token as string;
        token.refresh_token = account.refresh_token as string;
        token.provider = account.provider;
        token.expires_at = (account.expires_at ?? 0) * 1000; // Convert to milliseconds
        token.gazelleId = user.gazelleId;
        token.groups = user.groups;
        token.organization = user.organization;

        // Map Keycloak groups to roles for authorization
        token.roles = user.groups || [];
      }

      // console.warn("Token expires at", token.expires_at);
      // If we have spent half of the token's lifetime, refresh it
      const expiresAt = token.expires_at;
      const refreshLimit = expiresAt - (Date.now() - expiresAt) / 2;
      if (Date.now() < refreshLimit) {
        return token;
      }
      // Access token has expired, try to update it
      token = await refreshAccessToken(token);
      return token;
    },
    async session({ session, token }: { session: Session; token: JWT }) {
      if (token.error) session.error = token.error;
      if (session?.user) {
        session.user.groups = token.groups;
        session.user.organization = token.organization[0].replace("/", "");
        session.user.gazelleId = token.gazelleId;
      }
      session.id_token = token.id_token;
      session.access_token = token.access_token;
      session.refresh_token = token.refresh_token;
      session.expires_in = token.expires_at;
      return session;
    },
  },
  events: {
    async signOut({ token }: { token: JWT }) {
      if (token.provider === "keycloak") {
        const issuerUrl = (authOptions.providers.find((provider) => provider.id === "keycloak") as OAuthConfig<KeycloakProfile>).options!.issuer!;
        const logOutUrl = new URL(`${issuerUrl}/protocol/openid-connect/logout`);
        logOutUrl.searchParams.set("id_token_hint", token.id_token);
        // Add redirect_uri parameter to immediately redirect to login page after logout
        const redirectUri = `${process.env.NEXTAUTH_URL ?? ""}`;
        logOutUrl.searchParams.set("post_logout_redirect_uri", redirectUri);
        await fetch(logOutUrl);
      }
    },
  },
  session: {
    maxAge: 20 * 60, // 20 minutes
  },
  jwt: {
    maxAge: 40 * 60, // 40 minutes
  },
};
