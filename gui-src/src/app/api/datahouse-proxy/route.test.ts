// @vitest-environment node
import { describe, it, expect, vi, beforeEach } from "vitest";
import { NextRequest } from "next/server";
import axios from "axios";
import { GET } from "./route";

vi.mock("axios");
vi.mock("@shared/services/getServerAuthHeader", () => ({
  getAuthorizationHeader: vi.fn().mockResolvedValue("Bearer user-token"),
}));

const request = (url: string) =>
  new NextRequest(`https://gazelle.example.com/gazelle/api/datahouse-proxy?url=${encodeURIComponent(url)}`);

describe("GET /api/datahouse-proxy", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubEnv("BASE_URL", "https://gazelle.example.com/gazelle");
    vi.stubEnv("GZL_DTH_API_URL", "https://datahouse.example.com/rest/v1");
  });

  it.each(["http://mailpit:8025/mailpit/api/v1/messages", "https://attacker.test/collect"])(
    "does not contact or send the user's token to %s",
    async (url) => {
      const response = await GET(request(url));

      expect(response.status).toBe(403);
      expect(axios.get).not.toHaveBeenCalled();
    }
  );

  it("forwards requests for the configured Datahouse without following redirects", async () => {
    vi.mocked(axios.get).mockResolvedValue({ status: 200, data: { id: "1" } });

    const response = await GET(request("https://datahouse.example.com/rest/v1/items/1"));

    expect(response.status).toBe(200);
    expect(axios.get).toHaveBeenCalledWith(
      "https://datahouse.example.com/rest/v1/items/1",
      expect.objectContaining({ maxRedirects: 0, headers: { Authorization: "Bearer user-token" } })
    );
  });
});
