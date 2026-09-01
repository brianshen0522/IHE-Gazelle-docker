package net.ihe.gazelle.validation.gateway.evs.business.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kereval.gazelle.datahouse.api.business.record.Item;
import net.ihe.gazelle.validation.v2.api.business.report.AssertionReport;
import net.ihe.gazelle.validation.v2.api.business.report.Metadata;
import net.ihe.gazelle.validation.v2.api.business.report.RequirementPriority;
import net.ihe.gazelle.validation.v2.api.business.report.SeverityLevel;
import net.ihe.gazelle.validation.v2.api.business.report.SubjectLocation;
import net.ihe.gazelle.validation.v2.api.business.report.UnexpectedError;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationCounters;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationMethod;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationSubReport;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationTestResult;
import net.ihe.gazelle.validation.v2.api.technical.dto.report.ValidationReportDTO;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemTransformationServiceTest {

   @Test
   void toLegacyValidationReportMapsNestedStructures() {
      ItemTransformationService service = new ItemTransformationService();
      ValidationReport report = richReport();

      net.ihe.gazelle.evsapi.client.business.response.report.ValidationReport legacy =
            service.toLegacyValidationReport(report);

      assertThat(legacy, notNullValue());
      assertThat(legacy.getValidationOverview(), notNullValue());
      assertThat(legacy.getSubReport(), notNullValue());
   }

   @Test
   void readReportRejectsBlankContent() {
      ItemTransformationService service = new ItemTransformationService();
      Item item = new Item();
      item.setContent("   ");

      IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.readReport(item));

      assertThat(exception.getMessage(), containsString("content is empty"));
   }

   @Test
   void toReportPayloadSerializesJsonAndXml() {
      ValidationReport report = richReport();
      Item item = new Item();
      item.setContent("{}");

      ItemTransformationService service = new ItemTransformationService() {
         @Override
         public net.ihe.gazelle.validation.v2.api.business.report.ValidationReport readReport(Item ignored) {
            return report;
         }
      };

      String jsonPayload = service.toReportPayload(item, "application/json");
      String xmlPayload = service.toReportPayload(item, "application/xml");

      assertThat(jsonPayload, containsString("\"uuid\":\"report-1\""));
      assertThat(xmlPayload, containsString("<uuid>report-1</uuid>"));
   }

   @Test
   void readReportParsesValidDtoPayload() throws Exception {
      ItemTransformationService service = new ItemTransformationService();
      ValidationReport report = new ValidationReport().setUuid("read-1").setOverallResult(ValidationTestResult.PASSED);
      String content = new ObjectMapper().writeValueAsString(new ValidationReportDTO(report));
      Item item = new Item();
      item.setContent(content);

      ValidationReport parsed = service.readReport(item);

      assertThat(parsed, notNullValue());
      assertThat(parsed.getUuid(), org.hamcrest.Matchers.is("read-1"));
   }

   private static ValidationReport richReport() {
      ValidationMethod method = new ValidationMethod()
            .setValidationServiceName("svc")
            .setValidationServiceVersion("1.0")
            .setValidationProfileID("profile")
            .setValidationProfileVersion("2.0");

      ValidationCounters counters = new ValidationCounters()
            .setNumberOfAssertions(5)
            .setNumberOfFailedWithInfos(1)
            .setNumberOfFailedWithWarnings(2)
            .setNumberOfFailedWithErrors(3);

      UnexpectedError nestedCause = new UnexpectedError().setName("nested").setMessage("nested-msg");
      UnexpectedError error = new UnexpectedError().setName("top").setMessage("top-msg").setCause(nestedCause);

      AssertionReport assertion = new AssertionReport()
            .setAssertionID("A1")
            .setAssertionType("TYPE")
            .setDescription("description")
            .setFormalExpression("exp")
            .setSubjectValue("value")
            .setRequirementIDs(List.of("REQ-1"))
            .setResult(ValidationTestResult.FAILED)
            .setSeverity(SeverityLevel.ERROR)
            .setPriority(RequirementPriority.MANDATORY)
            .addSubjectLocation(new SubjectLocation().setValue("/root/node"))
            .addUnexpectedError(error);

      ValidationSubReport nested = new ValidationSubReport()
            .setName("nested")
            .setSubReportResult(ValidationTestResult.PASSED)
            .setSubCounters(new ValidationCounters().setNumberOfAssertions(1));

      ValidationSubReport sub = new ValidationSubReport()
            .setName("sub")
            .setStandards(List.of("XDS"))
            .setSubReportResult(ValidationTestResult.FAILED)
            .setSubCounters(counters)
            .addUnexpectedError(error)
            .addAssertionReport(assertion)
            .addSubReport(nested);

      return new ValidationReport()
            .setUuid("report-1")
            .setDateTime(new Date())
            .setDisclaimer("disclaimer")
            .setValidationMethod(method)
            .setCounters(counters)
            .setOverallResult(ValidationTestResult.FAILED)
            .addAdditionalMetadata(new Metadata().setName("k").setValue("v"))
            .addValidationSubReport(sub);
   }
}
