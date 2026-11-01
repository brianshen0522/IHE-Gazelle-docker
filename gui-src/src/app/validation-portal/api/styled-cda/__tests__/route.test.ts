// @vitest-environment node
import { describe, expect, it, vi } from "vitest";
import { NextRequest } from "next/server";
import { GET } from "../route";

vi.mock("node:fs/promises", () => {
  const fsMock = {
    mkdir: vi.fn(async () => undefined),
    stat: vi.fn(async () => ({ mtimeMs: Date.now() })),
    readFile: vi.fn(async () => "<xsl:stylesheet/>"),
    access: vi.fn(async () => {
      throw new Error("missing");
    }),
    open: vi.fn(async () => ({ close: vi.fn() })),
    writeFile: vi.fn(async () => undefined),
    rename: vi.fn(async () => undefined),
    unlink: vi.fn(async () => undefined),
  };
  return { default: fsMock };
});
vi.mock("xslt-processor", () => ({
  XmlParser: class {
    xmlParse() {
      return {};
    }
  },
  Xslt: class {
    async xsltProcess() {
      return "<html><script>1</script></html>";
    }
  },
}));
vi.mock("@/shared/actions/getDatahouseAttachment", () => ({
  getDatahouseAttachment: vi.fn(async () => ({ data: Buffer.from("<ClinicalDocument/>").toString("base64") })),
}));
vi.mock("@/shared/actions/getDatahouseItem", () => ({ getDatahouseItem: vi.fn() }));

describe("styled CDA route", () => {
  it("serves the rendered document in a CSP sandbox", async () => {
    const response = await GET(new NextRequest("http://localhost/api/styled-cda?reportItemId=1&attachmentId=2"));

    expect(response.status).toBe(200);
    expect(response.headers.get("Content-Security-Policy")).toContain("sandbox");
    expect(response.headers.get("Content-Security-Policy")).toContain("default-src 'none'");
  });
});
