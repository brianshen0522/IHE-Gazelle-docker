import { describe, it, expect } from "vitest";
import { base64Decode, base64DecodeComplete } from "../base64";

describe("base64Decode", () => {
  it("decodes valid base64 string", () => {
    const base64 = btoa("Hello, World!");
    const result = base64Decode(base64);
    expect(result).toBe("Hello, World!");
  });

  it("decodes empty string", () => {
    const base64 = btoa("");
    const result = base64Decode(base64);
    expect(result).toBe("");
  });

  it("returns null for invalid base64 string", () => {
    const result = base64Decode("not-valid-base64!");
    expect(result).toBeNull();
  });

  it("truncates content longer than maxDecodedLength", () => {
    const longText = "A".repeat(5000);
    const base64 = btoa(longText);
    const result = base64Decode(base64, 100);
    expect(result).toBe("A".repeat(100));
    expect(result?.length).toBe(100);
  });

  it("returns full content when shorter than maxDecodedLength", () => {
    const shortText = "Short content";
    const base64 = btoa(shortText);
    const result = base64Decode(base64, 100);
    expect(result).toBe(shortText);
  });

  it("uses default maxDecodedLength of 4096", () => {
    const text = "X".repeat(5000);
    const base64 = btoa(text);
    const result = base64Decode(base64);
    expect(result?.length).toBe(4096);
    expect(result).toBe("X".repeat(4096));
  });

  it("returns full content when exactly at maxDecodedLength", () => {
    const text = "B".repeat(4096);
    const base64 = btoa(text);
    const result = base64Decode(base64);
    expect(result).toBe(text);
    expect(result?.length).toBe(4096);
  });

  it("decodes special characters", () => {
    const specialChars = "!@#$%^&*()_+{}[]|:;<>?,./~`";
    const base64 = btoa(specialChars);
    const result = base64Decode(base64);
    expect(result).toBe(specialChars);
  });

  it("decodes multiline content", () => {
    const multiline = "Line 1\nLine 2\nLine 3";
    const base64 = btoa(multiline);
    const result = base64Decode(base64);
    expect(result).toBe(multiline);
  });

  it("decodes JSON content", () => {
    const json = JSON.stringify({ key: "value", number: 123 });
    const base64 = btoa(json);
    const result = base64Decode(base64);
    expect(result).toBe(json);
  });

  it("handles base64 with padding", () => {
    const text = "A";
    const base64 = btoa(text); // Will have padding
    const result = base64Decode(base64);
    expect(result).toBe(text);
  });

  it("returns null for malformed base64", () => {
    const result = base64Decode("SGVsbG8gV29ybGQ=@#$");
    expect(result).toBeNull();
  });
});

describe("base64DecodeComplete", () => {
  it("decodes valid base64 string completely", () => {
    const base64 = btoa("Hello, World!");
    const result = base64DecodeComplete(base64);
    expect(result).toBe("Hello, World!");
  });

  it("decodes empty string", () => {
    const base64 = btoa("");
    const result = base64DecodeComplete(base64);
    expect(result).toBe("");
  });

  it("returns null for invalid base64 string", () => {
    const result = base64DecodeComplete("not-valid-base64!");
    expect(result).toBeNull();
  });

  it("decodes very long content without truncation", () => {
    const longText = "A".repeat(10000);
    const base64 = btoa(longText);
    const result = base64DecodeComplete(base64);
    expect(result).toBe(longText);
    expect(result?.length).toBe(10000);
  });

  it("decodes content longer than default maxDecodedLength", () => {
    const longText = "X".repeat(5000);
    const base64 = btoa(longText);
    const result = base64DecodeComplete(base64);
    expect(result?.length).toBe(5000);
    expect(result).toBe(longText);
  });

  it("decodes special characters", () => {
    const specialChars = "!@#$%^&*()_+{}[]|:;<>?,./~`";
    const base64 = btoa(specialChars);
    const result = base64DecodeComplete(base64);
    expect(result).toBe(specialChars);
  });

  it("decodes multiline content", () => {
    const multiline = "Line 1\nLine 2\nLine 3\nLine 4\nLine 5";
    const base64 = btoa(multiline);
    const result = base64DecodeComplete(base64);
    expect(result).toBe(multiline);
  });

  it("decodes large JSON content", () => {
    const json = JSON.stringify({ items: new Array(100).fill({ id: 1, name: "test", value: 123 }) });
    const base64 = btoa(json);
    const result = base64DecodeComplete(base64);
    expect(result).toBe(json);
  });

  it("handles base64 with padding", () => {
    const text = "AB";
    const base64 = btoa(text);
    const result = base64DecodeComplete(base64);
    expect(result).toBe(text);
  });

  it("returns null for malformed base64", () => {
    const result = base64DecodeComplete("SGVsbG8gV29ybGQ=@#$");
    expect(result).toBeNull();
  });

  it("decodes XML content", () => {
    const xml = '<?xml version="1.0"?><root><item>value</item></root>';
    const base64 = btoa(xml);
    const result = base64DecodeComplete(base64);
    expect(result).toBe(xml);
  });
});

describe("base64Decode vs base64DecodeComplete", () => {
  it("base64Decode truncates while base64DecodeComplete does not", () => {
    const longText = "Z".repeat(5000);
    const base64 = btoa(longText);

    const truncated = base64Decode(base64, 1000);
    const complete = base64DecodeComplete(base64);

    expect(truncated?.length).toBe(1000);
    expect(complete?.length).toBe(5000);
    expect(truncated).toBe(longText.slice(0, 1000));
    expect(complete).toBe(longText);
  });

  it("both return null for invalid input", () => {
    const invalid = "invalid!!!base64";

    expect(base64Decode(invalid)).toBeNull();
    expect(base64DecodeComplete(invalid)).toBeNull();
  });

  it("both decode short content identically", () => {
    const text = "Short text";
    const base64 = btoa(text);

    expect(base64Decode(base64)).toBe(text);
    expect(base64DecodeComplete(base64)).toBe(text);
  });
});
