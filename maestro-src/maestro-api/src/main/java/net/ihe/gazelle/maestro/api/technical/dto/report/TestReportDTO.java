/*
 * Copyright 2025-2026 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.maestro.api.technical.dto.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.errorhandling.technical.UnexpectedErrorDTO;
import net.ihe.gazelle.maestro.api.business.testreport.Result;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Schema(
      name = "TestReport",
      description = "The report containing the result of a test suite or test execution."
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
      "reportVersion",
      "uuid",
      "result",
      "dateTime",
      "testService",
      "systemsUnderTest",
      "testSuiteName",
      "testCounters",
      "note",
      "urlToTestSuiteResult",
      "testRunReports",
      "subReports",
      "unexpectedErrors"
})
public class TestReportDTO implements DTO<TestReport> {

   @JsonIgnore
   private final TestReport testReport;

   public TestReportDTO() {
      this(new TestReport());
   }

   public TestReportDTO(TestReport testReport) {
      this.testReport = testReport;
   }

   @Override
   @JsonIgnore
   public TestReport getBusinessObject() {
      return testReport;
   }

   @Schema(
         name = "reportVersion",
         description = "The version of the test report model."
   )
   @JsonProperty(value = "reportVersion")
   public String getReportVersion() {
      return testReport.getReportVersion();
   }

   public void setReportVersion(String reportVersion) {
      // for deserialization
   }

   @Schema(
         name = "uuid",
         description = "The unique identifier of the test report.",
         required = true
   )
   @JsonProperty(value = "uuid")
   public String getUuid() {
      return testReport.getUuid();
   }

   public void setUuid(String uuid) {
      testReport.setUuid(uuid);
   }

   @Schema(
         name = "result",
         description = "The global result of the test suite.",
         enumeration = {Result.RESULT_PASSED, Result.RESULT_FAILED, Result.RESULT_UNDEFINED},
         required = true
   )
   @JsonProperty(value = "result")
   public String getResult() {
      return testReport.getResult().name();
   }

   public void setResult(String result) {
      testReport.setResult(Result.valueOf(result));
   }

   @Schema(
         name = "dateTime",
         description = "The zoned date time of the test suite execution.",
         examples = "2025-01-01T16:30:00.387+01:00",
         required = true
   )
   @JsonProperty(value = "dateTime")
   public String getDateTime() {
      return ZonedDateTime.ofInstant(testReport.getDateTime(), ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
   }

   public void setDateTime(String dateTime) {
      testReport.setDateTime(
            Instant.from(ZonedDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
      );
   }

   @Schema(
         name = "testService",
         description = "The service used as the test engine to execute the test suite."
   )
   @JsonProperty(value = "testService")
   public TestServiceDTO getTestService() {
      if (testReport.getTestService() != null) {
         return new TestServiceDTO(testReport.getTestService());
      }
      return null;
   }

   public void setTestService(TestServiceDTO testServiceDTO) {
      testReport.setTestService(testServiceDTO != null ? testServiceDTO.getBusinessObject() : null);
   }

   @Schema(
         name = "systemsUnderTest",
         description = "The list of system under test involved in the test suite."
   )
   @JsonProperty(value = "systemsUnderTest")
   public List<SystemUnderTestDTO> getSystemsUnderTest() {
      return testReport
            .getSystemsUnderTest()
            .stream()
            .map(SystemUnderTestDTO::new)
            .toList();
   }

   public void setSystemsUnderTest(List<SystemUnderTestDTO> systemsUnderTest) {
      testReport.setSystemsUnderTest(
            systemsUnderTest == null ? List.of() :
                  systemsUnderTest.stream().map(SystemUnderTestDTO::getBusinessObject).toList()
      );
   }

   @Schema(
         name = "testSuiteName",
         description = "The name of the test suite that has been executed."
   )
   @JsonProperty(value = "testSuiteName")
   public String getTestSuiteName() {
      return testReport.getTestSuiteName();
   }

   public void setTestSuiteName(String testSuiteName) {
      testReport.setTestSuiteName(testSuiteName);
   }

   @Schema(
         name = "testCounters",
         description = "The counters that summarize the results of test runs in the suite."
   )
   @JsonProperty(value = "testCounters")
   public TestCountersDTO getTestCounters() {
      return testReport.getTestCounters() != null
            ? new TestCountersDTO(testReport.getTestCounters())
            : new TestCountersDTO();
   }

   public void setTestCounters(TestCountersDTO testCounters) {
      testReport.setTestCounters(testCounters != null ? testCounters.getBusinessObject() : null);
   }

   @Schema(
         name = "note",
         description = "A note to attach to the test report."
   )
   @JsonProperty(value = "note")
   public String getNote() {
      return testReport.getNote();
   }

   public void setNote(String note) {
      testReport.setNote(note);
   }

   @Schema(
         name = "urlToTestSuiteResult",
         description = "A link to retrieve the test report."
   )
   @JsonProperty(value = "urlToTestSuiteResult")
   public String getUrlToTestSuiteResult() {
      return testReport.getUrlToTestSuiteResult();
   }

   public void setUrlToTestSuiteResult(String urlToTestSuiteResult) {
      testReport.setUrlToTestSuiteResult(urlToTestSuiteResult);
   }

   @Schema(
         name = "testRunReports",
         description = "The list of test run report that was part of the test suite."
   )
   @JsonProperty(value = "testRunReports")
   public List<TestRunReportDTO> getTestRunReports() {
      return testReport
            .getTestRunReports()
            .stream()
            .map(TestRunReportDTO::new)
            .toList();
   }

   public void setTestRunReports(List<TestRunReportDTO> testRunReports) {
      testReport.setTestRunReports(
            testRunReports == null ? List.of() :
                  testRunReports.stream().map(TestRunReportDTO::getBusinessObject).toList()
      );
   }

   @Schema(
         name = "subReports",
         description = "A list of included test reports."
   )
   @JsonProperty(value = "subReports")
   public List<TestReportDTO> getSubReports() {
      return testReport
            .getSubReports()
            .stream()
            .map(TestReportDTO::new)
            .toList();
   }

   public void setSubReports(List<TestReportDTO> subReports) {
      testReport.setSubReports(
            subReports == null ? List.of() :
                  subReports.stream().map(TestReportDTO::getBusinessObject).toList()
      );
   }

   @Schema(
         name = "unexpectedErrors",
         description = "The list of unexpected errors that can occur during test suite execution."
   )
   @JsonProperty(value = "unexpectedErrors")
   public List<UnexpectedErrorDTO> getUnexpectedErrors() {
      return testReport
            .getUnexpectedErrors()
            .stream()
            .map(UnexpectedErrorDTO::new)
            .toList();
   }

   public void setUnexpectedErrors(List<UnexpectedErrorDTO> unexpectedErrors) {
      testReport.setUnexpectedErrors(
            unexpectedErrors == null ? List.of() :
                  unexpectedErrors.stream().map(UnexpectedErrorDTO::getBusinessObject).toList()
      );
   }
}
