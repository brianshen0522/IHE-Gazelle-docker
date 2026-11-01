import { describe, it, expect } from "vitest";
import { isAllowedOutboundUrl } from "./outboundUrl";

const BASE = "https://gazelle.example.com/gazelle";

describe("isAllowedOutboundUrl", () => {
  it("accepts a URL on a configured origin", () => {
    expect(isAllowedOutboundUrl(new URL("https://gazelle.example.com/datahouse/items/1"), [BASE])).toBe(true);
  });

  it.each([
    "http://keycloak:8180/auth",
    "http://gum:8081/gum/rest",
    "http://169.254.169.254/latest/meta-data/",
    "https://gazelle.example.com.attacker.test/",
    "https://gazelle.example.com@attacker.test/",
    "http://gazelle.example.com/",
    "https://gazelle.example.com:8443/",
    "file:///etc/passwd",
  ])("rejects %s", (url) => {
    expect(isAllowedOutboundUrl(new URL(url), [BASE])).toBe(false);
  });

  it("rejects everything when nothing usable is configured", () => {
    expect(isAllowedOutboundUrl(new URL("https://gazelle.example.com/"), [undefined, "", "not a url"])).toBe(false);
  });
});
