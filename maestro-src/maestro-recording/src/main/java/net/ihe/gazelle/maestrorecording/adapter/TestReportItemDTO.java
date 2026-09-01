/*
 * Copyright 2026 IHE International.
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

package net.ihe.gazelle.maestrorecording.adapter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.errorhandling.technical.UnexpectedErrorDTO;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.api.technical.dto.report.*;

import java.util.ArrayList;
import java.util.List;

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
      "subReportsReferences",
      "unexpectedErrors"
})
class TestReportItemDTO {

    private final TestReportDTO testReport;

    private List<TestReportReferenceDTO> subReportsReferences = new ArrayList<>();

    TestReportItemDTO() {
        this.testReport = new TestReportDTO();
    }

    TestReportItemDTO(TestReport testReport) {
        this.testReport = new TestReportDTO(testReport);
    }

   @JsonProperty("reportVersion")
    String getReportVersion() {
        return testReport.getReportVersion();
    }

    TestReportItemDTO setReportVersion(String reportVersion) {
        this.testReport.setReportVersion(reportVersion);
        return this;
    }

    @JsonProperty("uuid")
    String getUuid() {
        return testReport.getUuid();
    }

    TestReportItemDTO setUuid(String uuid) {
        this.testReport.setUuid(uuid);
        return this;
    }

   @JsonProperty("result")
   String getResult() {
      return testReport.getResult();
   }

   TestReportItemDTO setResult(String result) {
      this.testReport.setResult(result);
      return this;
   }

    @JsonProperty("dateTime")
    String getDateTime() {
        return testReport.getDateTime();
    }

    TestReportItemDTO setDateTime(String dateTime) {
        this.testReport.setDateTime(dateTime);
        return this;
    }

    @JsonProperty("testService")
    TestServiceDTO getTestService() {
        return testReport.getTestService();
    }

    TestReportItemDTO setTestService(TestServiceDTO testService) {
        this.testReport.setTestService(testService);
        return this;
    }

    @JsonProperty("systemsUnderTest")
    List<SystemUnderTestDTO> getSystemsUnderTest() {
        return new ArrayList<>(testReport.getSystemsUnderTest());
    }

    TestReportItemDTO setSystemsUnderTest(List<SystemUnderTestDTO> systemsUnderTest) {
        this.testReport.setSystemsUnderTest(new ArrayList<>(systemsUnderTest));
        return this;
    }

    @JsonProperty("testSuiteName")
    String getTestSuiteName() {
        return testReport.getTestSuiteName();
    }

    TestReportItemDTO setTestSuiteName(String testSuiteName) {
        this.testReport.setTestSuiteName(testSuiteName);
        return this;
    }

    @JsonProperty("testCounters")
    TestCountersDTO getTestCounters() {
        return testReport.getTestCounters();
    }

    TestReportItemDTO setTestCounters(TestCountersDTO testCounters) {
        this.testReport.setTestCounters(testCounters);
        return this;
    }

    @JsonProperty("note")
    String getNote() {
        return testReport.getNote();
    }

    TestReportItemDTO setNote(String note) {
        this.testReport.setNote(note);
        return this;
    }

    @JsonProperty("urlToTestSuiteResult")
    String getUrlToTestSuiteResult() {
        return testReport.getUrlToTestSuiteResult();
    }

    TestReportItemDTO setUrlToTestSuiteResult(String urlToTestSuiteResult) {
        this.testReport.setUrlToTestSuiteResult(urlToTestSuiteResult);
        return this;
    }

    @JsonProperty("testRunReports")
    List<TestRunReportDTO> getTestRunReports() {
       return new ArrayList<>(testReport.getTestRunReports());
    }

    TestReportItemDTO setTestRunReports(List<TestRunReportDTO> testRunReports) {
       this.testReport.setTestRunReports(new ArrayList<>(testRunReports));
       return this;
    }

    @JsonProperty("subReportsReferences")
    List<TestReportReferenceDTO> getSubReportsReferences() {
        return new ArrayList<>(subReportsReferences);
    }

    TestReportItemDTO setSubReportsReferences(List<TestReportReferenceDTO> subReports) {
        this.subReportsReferences = new ArrayList<>(subReports);
        return this;
    }

    @JsonProperty("unexpectedErrors")
    List<UnexpectedErrorDTO> getUnexpectedErrors() {
        return new ArrayList<>(testReport.getUnexpectedErrors());
    }

    TestReportItemDTO setUnexpectedErrors(List<UnexpectedErrorDTO> unexpectedErrors) {
        this.testReport.setUnexpectedErrors(new ArrayList<>(unexpectedErrors));
        return this;
    }

}
