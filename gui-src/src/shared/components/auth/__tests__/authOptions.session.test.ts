import { describe, expect, it } from "vitest";
import type { Session } from "next-auth";
import type { JWT } from "next-auth/jwt";
import { authOptions } from "../authOptions";

describe("authOptions session callback", () => {
  it("exposes the access token but no refresh or ID token to the browser", async () => {
    const token = {
      access_token: "access",
      refresh_token: "refresh",
      id_token: "id",
      expires_at: 1,
      groups: [],
      organization: ["/org"],
      gazelleId: "user",
    } as unknown as JWT;

    const session = await authOptions.callbacks.session({ session: { user: {} } as Session, token });

    expect(session.access_token).toBe("access");
    expect(session).not.toHaveProperty("refresh_token");
    expect(session).not.toHaveProperty("id_token");
  });
});
