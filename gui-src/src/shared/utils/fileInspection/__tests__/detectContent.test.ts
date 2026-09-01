import { describe, it, expect } from "vitest";
import { inspectForRendering } from "../detectContent";

describe("inspectForRendering", () => {
  describe("JSON detection", () => {
    it("should detect valid JSON object", () => {
      const jsonObj = { name: "test", value: 123 };
      const base64 = btoa(JSON.stringify(jsonObj));
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("JSON");
      expect(result.base64Data).toBe(base64);
    });

    it("should detect valid JSON array", () => {
      const jsonArray = [1, 2, 3, { key: "value" }];
      const base64 = btoa(JSON.stringify(jsonArray));
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("JSON");
      expect(result.base64Data).toBe(base64);
    });

    it("should detect JSON with leading whitespace", () => {
      const json = "  \n  { \"name\": \"test\" }";
      const base64 = btoa(json);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("JSON");
    });

    it("should handle invalid JSON-like content", () => {
      const invalid = "{ this is not valid json";
      const base64 = btoa(invalid);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("RAW");
    });

    it("should handle large JSON files", () => {
      const largeJson = {
        data: Array.from({ length: 1000 }, (_, i) => ({ id: i, value: `item-${i}` }))
      };
      const base64 = btoa(JSON.stringify(largeJson));
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("JSON");
    });
  });

  describe("XML detection", () => {
    it("should detect valid XML", () => {
      const xml = "<?xml version=\"1.0\"?><root><item>test</item></root>";
      const base64 = btoa(xml);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("XML");
      expect(result.base64Data).toBe(base64);
    });

    it("should detect XML without declaration", () => {
      const xml = "<root><item>test</item></root>";
      const base64 = btoa(xml);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("XML");
    });

    it("should detect self-closing XML tags", () => {
      const xml = "<root><item /></root>";
      const base64 = btoa(xml);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("XML");
    });

    it("should detect XML with whitespace", () => {
      const xml = "  \n  <?xml version=\"1.0\"?>\n<root></root>";
      const base64 = btoa(xml);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("XML");
    });

    it("should detect DICOM XML", () => {
      const xml = "<DICOM><patient>John Doe</patient></DICOM>";
      const base64 = btoa(xml);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("XML");
      expect(result.dataType).toBe("DICOM");
    });

    it("should detect DICOM with case insensitivity", () => {
      const xml = "<dicom><patient>John Doe</patient></dicom>";
      const base64 = btoa(xml);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("XML");
      expect(result.dataType).toBe("DICOM");
    });

    it("should detect HTTP XML", () => {
      const xml = "<http><request>GET /api</request></http>";
      const base64 = btoa(xml);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("XML");
      expect(result.dataType).toBe("HTTP");
    });

    it("should handle invalid XML-like content", () => {
      const invalid = "<this is not valid xml";
      const base64 = btoa(invalid);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("RAW");
    });
  });

  describe("HL7v2 detection", () => {
    it("should detect HL7v2 message", () => {
      const hl7 = String.raw`MSH|^~\&|SENDING_APP|SENDING_FAC|RECEIVING_APP|RECEIVING_FAC|20240101120000||ADT^A01|MSG00001|P|2.5`;
      const base64 = btoa(hl7);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("RAW");
      expect(result.dataType).toBe("HL7v2");
    });

    it("should detect HL7v2 with leading whitespace", () => {
      const hl7 = "  \n MSH|^~\\&|APP|FAC|APP|FAC|20240101||ADT^A01|1|P|2.5";
      const base64 = btoa(hl7);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("RAW");
      expect(result.dataType).toBe("HL7v2");
    });

    it("should not confuse HL7v2 with XML", () => {
      const hl7 = "MSH|^~\\&|SENDING_APP|SENDING_FAC|RECEIVING_APP|RECEIVING_FAC|20240101120000||ADT^A01|MSG00001|P|2.5\nPID|1||12345||Doe^John";
      const base64 = btoa(hl7);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("RAW");
      expect(result.dataType).toBe("HL7v2");
    });
  });

  describe("RAW fallback", () => {
    it("should handle plain text", () => {
      const text = "This is just plain text content";
      const base64 = btoa(text);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("RAW");
      expect(result.base64Data).toBe(base64);
    });

    it("should handle empty content", () => {
      const base64 = btoa("");
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("RAW");
    });

    it("should handle invalid base64", () => {
      const invalidBase64 = "not-valid-base64!!!";
      const result = inspectForRendering(invalidBase64);

      expect(result.renderer).toBe("RAW");
      expect(result.base64Data).toBe(invalidBase64);
    });

    it("should handle binary-like content", () => {
      const binary = String.fromCodePoint(0, 1, 2, 3, 255, 254);
      const base64 = btoa(binary);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("HEX");
    });
  });

  describe("Edge cases", () => {
    it("should handle content that starts with { but is not JSON", () => {
      const text = "{ hello world this is not json }";
      const base64 = btoa(text);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("RAW");
    });

    it("should handle content that starts with < but is not XML", () => {
      const text = "< hello world";
      const base64 = btoa(text);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("RAW");
    });

    it("should handle mixed content", () => {
      const text = "Some text before\n<xml><root>data</root></xml>\nSome text after";
      const base64 = btoa(text);
      const result = inspectForRendering(base64);

      expect(result.renderer).toBe("RAW");
    });
  });
});
