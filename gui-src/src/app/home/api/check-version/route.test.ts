// @vitest-environment node
import { describe, it, expect, vi, beforeEach } from "vitest";
import { NextRequest } from "next/server";
import axios from "axios";
import { GET } from "./route";

vi.mock("axios");

const request = (url: string) =>
  new NextRequest(`https://gazelle.example.com/gazelle/home/api/check-version?version=${encodeURIComponent(url)}`);

describe("GET /home/api/check-version", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubEnv("BASE_URL", "https://gazelle.example.com/gazelle");
  });

  it("rejects URLs outside the deployment", async () => {
    const response = await GET(request("http://keycloak:9000/metrics"));

    expect(response.status).toBe(403);
    expect(axios.get).not.toHaveBeenCalled();
  });

  it("returns version data from the public origin", async () => {
    vi.mocked(axios.get).mockResolvedValue({ status: 200, data: { version: "1.0.0" } });

    const response = await GET(request("https://gazelle.example.com/gum/version"));

    await expect(response.json()).resolves.toEqual({ data: { version: "1.0.0" } });
  });
});
