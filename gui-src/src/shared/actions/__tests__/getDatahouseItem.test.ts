import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { getDatahouseItem } from "../getDatahouseItem";

vi.mock("next-auth", () => ({ getServerSession: vi.fn(async () => ({ access_token: "token", user: { name: "u" } })) }));
vi.mock("@auth/authOptions", () => ({ authOptions: {} }));

describe("getDatahouseItem", () => {
  const fetchMock = vi.fn();

  beforeEach(() => {
    process.env.BASE_URL = "https://gazelle.example.com/gazelle";
    process.env.GZL_DTH_API_URL = "http://datahouse:8080/rest";
    fetchMock.mockReset();
    vi.stubGlobal("fetch", fetchMock);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    delete process.env.GZL_DTH_API_URL;
  });

  it.each(["http://keycloak:8180/auth", "http://169.254.169.254/latest/meta-data/", "https://attacker.test/x"])(
    "does not fetch location %s",
    async (location) => {
      const result = await getDatahouseItem({ location });

      expect(fetchMock).not.toHaveBeenCalled();
      expect(result?.error).toContain("URL not allowed");
    },
  );

  it("fetches a location on the configured Datahouse without following redirects", async () => {
    fetchMock.mockResolvedValue({ ok: true, json: async () => ({ id: "1" }) });

    const result = await getDatahouseItem({ location: "http://datahouse:8080/rest/items/1", parseContent: false });

    expect(result?.datahouseItem).toEqual({ id: "1" });
    expect(fetchMock.mock.calls[0][1]).toMatchObject({ redirect: "manual" });
  });
});
