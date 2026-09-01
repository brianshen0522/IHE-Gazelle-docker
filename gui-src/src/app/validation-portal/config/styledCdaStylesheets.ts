export type StyledCdaStylesheetConfig =
  | {
      id: string;
      sourceType: "url";
      location: string;
    }
  | {
      id: string;
      sourceType: "path";
      location: string;
    };

export const STYLED_CDA_STYLESHEET: StyledCdaStylesheetConfig = {
  id: "test-bed-cda-stylesheet",
  sourceType: "url",
  location: process.env.GZL_STYLED_CDA_STYLESHEET_URL ?? "https://gazelle.ehdsi.eu/xsl/CDA/hl7/CDA%202.xsl",
};
