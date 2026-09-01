package net.ihe.gazelle.validation.gateway.migration.evs;

import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationTestResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class LegacyReportParserTest {

   private final LegacyReportParser parser = new LegacyReportParser();

   @Test
   void parseValidationReportMapsLegacyXmlHierarchyAndAssertions() {
      Optional<ValidationReport> parsed = parser.parseValidationReport(legacyXmlPayload().getBytes(StandardCharsets.UTF_8));

      assertThat(parsed.isPresent(), is(true));
      ValidationReport report = parsed.orElseThrow();
      assertThat(report.getUuid(), equalTo("legacy-uuid-1"));
      assertThat(report.getReports().size(), equalTo(1));
      assertThat(report.getReports().get(0).getSubReports().size(), equalTo(1));
      assertThat(report.getReports().get(0).getSubReports().get(0).getAssertionReports().size(), equalTo(1));
      assertThat(report.getReports().get(0).getSubReports().get(0).getAssertionReports().get(0).getResult(),
            equalTo(ValidationTestResult.FAILED));
      assertThat(report.getCounters().getNumberOfAssertions(), equalTo(1));
      assertThat(report.getCounters().getNumberOfFailedWithErrors(), equalTo(1));
   }

   @Test
   void parseValidationReportMapsLegacyJsonHierarchy() {
      Optional<ValidationReport> parsed = parser.parseValidationReport(legacyJsonPayload().getBytes(StandardCharsets.UTF_8));

      assertThat(parsed.isPresent(), is(true));
      ValidationReport report = parsed.orElseThrow();
      assertThat(report.getUuid(), equalTo("legacy-json-1"));
      assertThat(report.getReports().size(), equalTo(1));
      assertThat(report.getReports().get(0).getAssertionReports().size(), equalTo(1));
      assertThat(report.getReports().get(0).getAssertionReports().get(0).getSubjectLocations().size(), equalTo(1));
      assertThat(report.getCounters().getNumberOfAssertions(), equalTo(1));
   }

   @Test
   void parseValidationReportFromArchiveSkipsInvalidFirstEntry(@TempDir Path tempDir) throws IOException {
      Path archive = tempDir.resolve("validation-report.zip");
      try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(archive))) {
         zos.putNextEntry(new ZipEntry("notes.txt"));
         zos.write("not-a-report".getBytes(StandardCharsets.UTF_8));
         zos.closeEntry();
         zos.putNextEntry(new ZipEntry("validation-report.xml"));
         zos.write(legacyXmlPayload().getBytes(StandardCharsets.UTF_8));
         zos.closeEntry();
      }

      Optional<ValidationReport> parsed = parser.parseValidationReport(archive.toString());

      assertThat(parsed.isPresent(), is(true));
      ValidationReport report = parsed.orElseThrow();
      assertThat(report.getReports(), is(notNullValue()));
      assertThat(report.getReports().size(), equalTo(1));
   }

   private String legacyXmlPayload() {
      return """
            <?xml version="1.0" encoding="UTF-8"?>
            <gvr:validationReport xmlns:gvr="http://validationreport.gazelle.ihe.net/" result="FAILED" uuid="legacy-uuid-1">
                <gvr:validationOverview validationDateTime="2025-09-30T12:44:06.137Z" validationOverallResult="FAILED">
                    <gvr:disclaimer>legacy disclaimer</gvr:disclaimer>
                    <gvr:validationServiceName>Legacy Service</gvr:validationServiceName>
                    <gvr:validationServiceVersion>1.0</gvr:validationServiceVersion>
                    <gvr:validatorID>1.2.3</gvr:validatorID>
                    <gvr:validatorVersion>2.0</gvr:validatorVersion>
                </gvr:validationOverview>
                <gvr:subReport name="Parent" subReportResult="FAILED">
                    <gvr:subReport name="Child" subReportResult="FAILED">
                        <gvr:constraint constraintID="A-1" constraintType="Failed" priority="MANDATORY" severity="ERROR" testResult="FAILED">
                            <gvr:constraintDescription>error</gvr:constraintDescription>
                            <gvr:locationInValidatedObject>line 1, column 2</gvr:locationInValidatedObject>
                        </gvr:constraint>
                    </gvr:subReport>
                </gvr:subReport>
            </gvr:validationReport>
            """;
   }

   private String legacyJsonPayload() {
      return """
            {
              "uuid": "legacy-json-1",
              "result": "PASSED",
              "validationOverview": {
                "validationDateTime": "2025-09-30T12:44:06.137Z",
                "validationServiceName": "Legacy Json Service",
                "validatorID": "9.8.7"
              },
              "subReports": [
                {
                  "name": "Only",
                  "subReportResult": "PASSED",
                  "constraints": [
                    {
                      "constraintID": "CID-1",
                      "constraintDescription": "desc",
                      "testResult": "PASSED",
                      "priority": "MANDATORY",
                      "subjectLocations": [
                        {
                          "type": "xpath",
                          "value": "/A/B"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """;
   }
}
