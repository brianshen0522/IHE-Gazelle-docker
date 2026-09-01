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
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.testreport.Result;
import net.ihe.gazelle.maestro.api.business.testreport.TestRunReport;
import net.ihe.gazelle.maestro.api.technical.dto.property.PropertyDTO;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import net.ihe.gazelle.modelmarshaller.technical.dto.SubTypingDTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Schema(
      name = "TestRunReport",
      description = "The report containing the result of a test run."
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
      "runId",
      "dateTime",
      "result",
      "test",
      "inputs",
      "outputs",
      "stepRunReports",
      "urlToTestRun",
      "unexpectedErrors"
})
public class TestRunReportDTO implements DTO<TestRunReport> {

   @JsonIgnore
   private final TestRunReport testRunReport;

   public TestRunReportDTO() {
      this(new TestRunReport());
   }

   public TestRunReportDTO(TestRunReport testRunReport) {
      this.testRunReport = testRunReport;
   }

   @Override
   @JsonIgnore
   public TestRunReport getBusinessObject() {
      return testRunReport;
   }

   @Schema(
         name = "runId",
         description = "The unique identifier of the test run."
   )
   @JsonProperty(value = "runId")
   public String getRunId() {
      return testRunReport.getRunId();
   }

   public void setRunId(String runId) {
      testRunReport.setRunId(runId);
   }

   @Schema(
         name = "dateTime",
         description = "The zoned date time of the test execution.",
         examples = "2025-01-01T16:30:00.387+01:00",
         required = true
   )
   @JsonProperty(value = "dateTime")
   public String getDateTime() {
      return ZonedDateTime.ofInstant(getBusinessObject().getDateTime(), ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
   }

   public void setDateTime(String dateTime) {
      getBusinessObject().setDateTime(
            Instant.from(ZonedDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
      );
   }

   @Schema(
         name = "result",
         description = "The result of the test execution.",
         enumeration = {Result.RESULT_PASSED, Result.RESULT_FAILED, Result.RESULT_UNDEFINED},
         required = true
   )
   @JsonProperty(value = "result")
   public String result() {
      return testRunReport.getResult().name();
   }

   public void setResult(String result) {
      testRunReport.setResult(Result.valueOf(result));
   }

   @Schema(
         name = "test",
         description = "The test that has been executed.",
         required = true
   )
   @JsonProperty(value = "test")
   public TestDTO test() {
      return new TestDTO(testRunReport.getTest());
   }

   public void setTest(TestDTO testDTO) {
      testRunReport.setTest(testDTO.getBusinessObject());
   }

   @Schema(
         name = "inputs",
         description = "The list of inputs used to execute the test."
   )
   @SuppressWarnings("rawtypes")
   @JsonProperty(value = "inputs")
   public List<PropertyDTO> getInputs() {
      return testRunReport.getInputs().stream()
            .map(SubTypingDTO::fromBusinessObject)
            .map(PropertyDTO.class::cast)
            .toList();
   }

   @SuppressWarnings("rawtypes")
   public void setInputs(List<PropertyDTO> inputs) {
      testRunReport.setInputs(
            inputs != null ?
                  inputs.stream()
                        .map(PropertyDTO::getBusinessObject)
                        .map(Property.class::cast)
                        .toList() :
                  null
      );
   }

   @Schema(
         name = "outputs",
         description = "The list of outputs produced by the test execution."
   )
   @SuppressWarnings("rawtypes")
   @JsonProperty(value = "outputs")
   public List<PropertyDTO> getOutputs() {
      return testRunReport.getOutputs().stream()
            .map(SubTypingDTO::fromBusinessObject)
            .map(PropertyDTO.class::cast)
            .toList();
   }

   @SuppressWarnings("rawtypes")
   public void setOutputs(List<PropertyDTO> outputs) {
      testRunReport.setOutputs(
            outputs != null ?
                  outputs.stream()
                        .map(PropertyDTO::getBusinessObject)
                        .map(Property.class::cast)
                        .toList() :
                  null
      );
   }

   @Schema(
         name = "stepRunReports",
         description = "The list reports for each step executed in the test."
   )
   @JsonProperty(value = "stepRunReports")
   public List<StepRunReportDTO> getStepRunReports() {
      return testRunReport
            .getStepRunReports()
            .stream()
            .map(StepRunReportDTO::new)
            .toList();
   }

   public void setStepRunReports(List<StepRunReportDTO> stepRunReports) {
      testRunReport.setStepRunReports(
            stepRunReports.stream().map(StepRunReportDTO::getBusinessObject).toList()
      );
   }

   @Schema(
         name = "urlToTestRun",
         description = "The link to retrieve the test execution report."
   )
   @JsonProperty(value = "urlToTestRun")
   public String getUrlToTestRun() {
      return testRunReport.getUrlToTestRun();
   }

   public void setUrlToTestRun(String urlToTestRun) {
      testRunReport.setUrlToTestRun(urlToTestRun);
   }

   @Schema(
         name = "unexpectedErrors",
         description = "The list of unexpected errors that can occur during test execution."
   )
   @JsonProperty(value = "unexpectedErrors")
   public List<UnexpectedErrorDTO> getUnexpectedErrors() {
      return testRunReport
            .getUnexpectedErrors()
            .stream()
            .map(UnexpectedErrorDTO::new)
            .toList();
   }

   public void setUnexpectedErrors(List<UnexpectedErrorDTO> unexpectedErrors) {
      testRunReport.setUnexpectedErrors(
            unexpectedErrors.stream().map(UnexpectedErrorDTO::getBusinessObject).toList()
      );
   }
}
