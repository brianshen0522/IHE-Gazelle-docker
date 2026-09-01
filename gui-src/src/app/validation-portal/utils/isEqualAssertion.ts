import { AssertionReportDTO } from "@/shared/types/validation/types";
import { normalizeSubjectLocations } from "./normalizeSubjectLocations";

export function isEqualAssertion(a?: AssertionReportDTO, b?: AssertionReportDTO): boolean {
  if (!a || !b) return false;

  return a.assertionID === b.assertionID && normalizeSubjectLocations(a.subjectLocations) === normalizeSubjectLocations(b.subjectLocations);
}
