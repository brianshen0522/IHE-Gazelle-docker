import { describe, it, expect } from "vitest";
import { isBinaryEncodedString } from "../binary/detectBinary";

describe("isBinaryEncodedString", () => {
  describe("Binary content detection", () => {
    it("should detect binary content with null bytes", () => {
      // PNG header: 89 50 4E 47 0D 0A 1A 0A 00 00
      const binaryData = new Uint8Array([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x00, 0x00]);
      const base64 = btoa(String.fromCodePoint(...binaryData));
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(true);
    });

    it("should detect binary content with high ratio of non-printable characters", () => {
      // Mix of binary bytes (>30% non-printable)
      const binaryData = new Uint8Array([0xff, 0xfe, 0x01, 0x02, 0x03, 0x80, 0x81, 0x82, 0x83, 0x84]);
      const base64 = btoa(String.fromCodePoint(...binaryData));
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(true);
    });

    it("should detect binary with embedded null bytes in longer content", () => {
      const data = new Uint8Array(100);
      data.fill(65); // Fill with 'A'
      data[50] = 0; // Add null byte in the middle
      const base64 = btoa(String.fromCodePoint(...data));
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(true);
    });

    it("should detect binary JPEG header", () => {
      // JPEG magic bytes: FF D8 FF E0
      const jpegHeader = new Uint8Array([0xff, 0xd8, 0xff, 0xe0, 0x00, 0x10, 0x4a, 0x46]);
      const base64 = btoa(String.fromCodePoint(...jpegHeader));
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(true);
    });

    it("should detect binary PDF header", () => {
      // PDF header: %PDF-1.4 with binary marker
      const pdfData = new Uint8Array([0x25, 0x50, 0x44, 0x46, 0x2d, 0x31, 0x2e, 0x34, 0x0a, 0xe2, 0xe3, 0xcf, 0xd3]);
      const base64 = btoa(String.fromCodePoint(...pdfData));
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(true);
    });

    it("should detect compressed/encrypted data", () => {
      // Simulated compressed data with high entropy
      const compressedData = new Uint8Array([
        0x1f, 0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0xff, 0xed, 0xbd, 0x07, 0x60, 0x1c, 0x49, 0x96, 0x25, 0x26, 0x2f,
      ]);
      const base64 = btoa(String.fromCodePoint(...compressedData));
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(true);
    });
  });

  describe("Text content detection", () => {
    it("should not detect plain ASCII text as binary", () => {
      const text = "Hello, World! This is plain text.";
      const base64 = btoa(text);
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(false);
    });

    it("should not detect JSON as binary", () => {
      const json = JSON.stringify({ name: "test", value: 123, nested: { key: "value" } });
      const base64 = btoa(json);
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(false);
    });

    it("should not detect XML as binary", () => {
      const xml = '<?xml version="1.0"?><root><item id="1">Test</item></root>';
      const base64 = btoa(xml);
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(false);
    });

    it("should not detect text with newlines and tabs as binary", () => {
      const text = "Line 1\nLine 2\n\tIndented line\r\nWindows line";
      const base64 = btoa(text);
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(false);
    });

    it("should not detect binary number representations as binary", () => {
      const binaryText = "01110100 01100101 01110011 01110100";
      const base64 = btoa(binaryText);
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(false);
    });

    it("should not detect HL7v2 message as binary", () => {
      const hl7 = "MSH|^~\\&|SENDER|FACILITY|RECEIVER|FACILITY|20230101120000||ADT^A01|MSG001|P|2.5\r";
      const base64 = btoa(hl7);
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(false);
    });

    it("should not detect multiline code as binary", () => {
      const code = `function test() {
  const x = 10;
  const y = 20;
  return x + y;
}`;
      const base64 = btoa(code);
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(false);
    });

    it("should not detect CSV data as binary", () => {
      const csv = "Name,Age,City\nJohn,30,NYC\nJane,25,LA\nBob,35,Chicago";
      const base64 = btoa(csv);
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(false);
    });
  });

  describe("Edge cases", () => {
    it("should return false for invalid base64", () => {
      const invalidBase64 = "not-valid-base64!@#$%";
      const result = isBinaryEncodedString(invalidBase64);

      expect(result).toBe(false);
    });

    it("should return false for empty string", () => {
      const base64 = btoa("");
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(false);
    });

    it("should handle content at 30% threshold (just below - text)", () => {
      // Create content with exactly 29% non-printable characters
      const data = new Uint8Array(100);
      data.fill(65); // Fill with 'A' (printable)
      for (let i = 0; i < 29; i++) {
        data[i] = 0xff; // Non-printable
      }
      const base64 = btoa(String.fromCodePoint(...data));
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(false);
    });

    it("should handle content at 30% threshold (just above - binary)", () => {
      // Create content with exactly 31% non-printable characters
      const data = new Uint8Array(100);
      data.fill(65); // Fill with 'A' (printable)
      for (let i = 0; i < 31; i++) {
        data[i] = 0xff; // Non-printable
      }
      const base64 = btoa(String.fromCodePoint(...data));
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(true);
    });

    it("should handle very short binary content", () => {
      const binaryData = new Uint8Array([0x00, 0xff]);
      const base64 = btoa(String.fromCodePoint(...binaryData));
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(true);
    });

    it("should handle single null byte", () => {
      const binaryData = new Uint8Array([0x00]);
      const base64 = btoa(String.fromCodePoint(...binaryData));
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(true);
    });
  });

  describe("Performance and sampling", () => {
    it("should only sample first 512 bytes for detection", () => {
      // Create large file: first 512 bytes are binary, rest is text
      const data = new Uint8Array(1024);
      // First 512 bytes: binary
      for (let i = 0; i < 512; i++) {
        data[i] = i % 2 === 0 ? 0x00 : 0xff;
      }
      // Rest: printable text
      for (let i = 512; i < 1024; i++) {
        data[i] = 65; // 'A'
      }
      const base64 = btoa(String.fromCodePoint(...data));
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(true);
    });

    it("should handle large text files efficiently", () => {
      // Create large text content (>512 bytes)
      const largeText = "A".repeat(10000);
      const base64 = btoa(largeText);
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(false);
    });

    it("should handle large binary files efficiently", () => {
      // Create large binary content (>512 bytes)
      const largeBinary = new Uint8Array(10000);
      largeBinary.fill(0xff);
      largeBinary[0] = 0x00; // Ensure null byte at start
      const base64 = btoa(String.fromCodePoint(...largeBinary));
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(true);
    });
  });

  describe("Special characters and encodings", () => {
    it("should handle text with special ASCII characters", () => {
      const text = "Hello! @#$%^&*() []{} <> /? ~`";
      const base64 = btoa(text);
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(false);
    });

    it("should detect control characters as binary", () => {
      // Mix of control characters (excluding allowed whitespace)
      const data = new Uint8Array([0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08]);
      const base64 = btoa(String.fromCodePoint(...data));
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(true);
    });

    it("should allow tab, newline, and carriage return in text", () => {
      // Only allowed control characters
      const text = "Text\twith\ntabs\rand\r\nnewlines";
      const base64 = btoa(text);
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(false);
    });

    it("should detect DEL character (127) as binary", () => {
      const data = new Uint8Array(10);
      data.fill(65); // Fill with 'A'
      data[5] = 127; // DEL character
      data[6] = 127;
      data[7] = 127;
      data[8] = 127; // 40% non-printable
      const base64 = btoa(String.fromCodePoint(...data));
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(true);
    });

    it("should detect extended control characters (128-159) as binary", () => {
      const data = new Uint8Array([0x80, 0x81, 0x82, 0x90, 0x9f, 65, 65, 65]);
      const base64 = btoa(String.fromCodePoint(...data));
      const result = isBinaryEncodedString(base64);

      expect(result).toBe(true);
    });
  });
});
