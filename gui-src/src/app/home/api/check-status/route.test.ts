// @vitest-environment node
import { describe, it, expect, vi, beforeEach } from "vitest";
import { NextRequest } from "next/server";
import { GET } from "./route";

const request = (url: string) =>
  new NextRequest(`https://gazelle.example.com/gazelle/home/api/check-status?status=${encodeURIComponent(url)}`);

describe("GET /home/api/check-status", () => {
  const fetchMock = vi.fn();

  beforeEach(() => {
    fetchMock.mockReset();
    vi.stubGlobal("fetch", fetchMock);
    vi.stubEnv("BASE_URL", "https://gazelle.example.com/gazelle");
  });

  it("rejects URLs outside the deployment", async () => {
    const response = await GET(request("http://jboss:9990/management"));

    expect(response.status).toBe(403);
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("checks URLs on the public origin", async () => {
    fetchMock.mockResolvedValue({ status: 204 });

    const response = await GET(request("https://gazelle.example.com/tm/"));

    await expect(response.json()).resolves.toEqual({ status: 204 });
  });
});
