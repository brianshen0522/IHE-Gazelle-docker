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

package net.ihe.gazelle.maestro.api.business.testreport;

import net.ihe.gazelle.errorhandling.business.UnexpectedError;
import net.ihe.gazelle.security.business.ProtectedResource;
import net.ihe.gazelle.security.business.acl.AccessControlList;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static net.ihe.gazelle.maestro.api.business.testreport.Result.UNDEFINED;


/**
 * A structure for a test report.
 * Business rules <br>
 * required fields :
 * uuid -> generation in builder random uuid <br>
 * dateTime <br>
 * result <br>
 * testCounters <br>
 * at least 1 subReport or 1 testRunReport or 1 unexpectedError <br>
 * <p>
 * Result computation to be done here: <br>
 * if 0 unexpectedError and 0 FAILED and 0 PASSED and 0 UNDEFINED -> UNDEFINED <br>
 * if 1 unexpectedError -> UNDEFINED <br>
 * if 1 UNDEFINED -> UNDEFINED <br>
 * if 0 unexpectedError and 0 UNDEFINED and at least 1 FAILED -> FAILED <br>
 * if 0 unexpectedError and 0 UNDEFINED and 0 FAILED and at least 1 PASSED -> PASSED <br>
 * <p>
 * Counters computation: <br>
 * count all testRunReport result + all sub testreport counters <br>
 * count all unexpectedErrors in testRuns + root + subReport error counters, <br>
 * <p>
 * Counters must be valid
 */
public class TestReport implements ProtectedResource, Serializable {

   @Serial
   private static final long serialVersionUID = 6609223753209152140L;

   /**
    * The version of the report.
    */
   public static final String REPORT_VERSION = "1.0";

   /**
    * The report version.
    */
   private String reportVersion;

   /**
    * The UUID of the report.
    */
   private String uuid;

   /**
    * The date and time at which the report was generated.
    */
   private Instant dateTime;

   /**
    * The service used to execute the test.
    */
   private TestService testService;

   /**
    * The systems under test.
    */
   private List<SystemUnderTest> systemsUnderTest;

   /**
    * The result of the test execution.
    */
   private Result result;

   /**
    * The name of the test suite.
    */
   private String testSuiteName;

   /**
    * The counters of the test execution.
    */
   private TestCounters testCounters;

   /**
    * A note to be added to the report.
    */
   private String note;

   /**
    * A link to the test suite result.
    */
   private String urlToTestSuiteResult;

   /**
    * The sub reports of the test execution.
    */
   private List<TestReport> subReports;

   /**
    * The test run reports of the test execution.
    */
   private List<TestRunReport> testRunReports;

   /**
    * The unexpected errors that occurred during the test execution.
    */
   private List<UnexpectedError> unexpectedErrors;

   /**
    * The access control list of the report.
    */
   private AccessControlList accessControlList;

   /**
    * Default constructor.
    */
   public TestReport() {
      reportVersion = REPORT_VERSION;
      dateTime = Instant.now();
      result = Result.UNDEFINED;
      systemsUnderTest = new ArrayList<>();
      subReports = new ArrayList<>();
      testRunReports = new ArrayList<>();
      unexpectedErrors = new ArrayList<>();
      accessControlList = new AccessControlList();
   }

   /**
    * Creates a new instance of TestReport by copying the properties from an existing TestReport instance.
    *
    * @param copy the existing TestReport instance whose properties are to be copied
    */
   public TestReport(TestReport copy) {
      this();
      setReportVersion(copy.getReportVersion());
      setUuid(copy.getUuid());
      setDateTime(copy.getDateTime());
      setTestService(copy.getTestService());
      setSystemsUnderTest(copy.getSystemsUnderTest());
      setResult(copy.getResult());
      setTestSuiteName(copy.getTestSuiteName());
      setTestCounters(copy.getTestCounters());
      setNote(copy.getNote());
      setUrlToTestSuiteResult(copy.getUrlToTestSuiteResult());
      setSubReports(copy.getSubReports());
      setTestRunReports(copy.getTestRunReports());
      setUnexpectedErrors(copy.getUnexpectedErrors());
      setAccessControlList(copy.getAccessControlList());
   }

   /**
    * Retrieves the version of the report.
    *
    * @return the version of the report as a String
    */
   public String getReportVersion() {
      return reportVersion;
   }

   /**
    * Sets the version of the report.
    *
    * @param reportVersion the version of the report as a String
    * @return the current instance of {@code TestReport} to allow method chaining
    */
   public TestReport setReportVersion(String reportVersion) {
      this.reportVersion = reportVersion;
      return this;
   }

   /**
    * Retrieves the universally unique identifier (UUID) of the test report.
    *
    * @return the UUID of the test report as a String
    */
   public String getUuid() {
      return uuid;
   }

   /**
    * Sets the universally unique identifier (UUID) for the test report.
    *
    * @param uuid the UUID to be set for the test report as a String
    * @return the current instance of {@code TestReport} to allow method chaining
    */
   public TestReport setUuid(String uuid) {
      this.uuid = uuid;
      return this;
   }

   /**
    * Retrieves the date and time associated with the test report.
    *
    * @return the date and time as an {@code Instant}
    */
   public Instant getDateTime() {
      return dateTime;
   }

   /**
    * Sets the date and time associated with the test report.
    *
    * @param dateTime the date and time to be set as an {@code Instant}
    * @return the current instance of {@code TestReport} to allow method chaining
    */
   public TestReport setDateTime(Instant dateTime) {
      this.dateTime = dateTime;
      return this;
   }

   /**
    * Retrieves the associated {@code TestService} instance for this {@code TestReport}.
    *
    * @return the {@code TestService} instance, which contains the service identification
    *         and disclaimer information relevant to this report.
    */
   public TestService getTestService() {
      return testService;
   }

   /**
    * Sets the associated {@code TestService} instance for this {@code TestReport}.
    *
    * @param testService the {@code TestService} instance to associate with this report
    * @return the current instance of {@code TestReport} to allow method chaining
    */
   public TestReport setTestService(TestService testService) {
      this.testService = testService;
      return this;
   }

   /**
    * Retrieves the list of systems under test associated with the test report.
    *
    * @return a new {@code List} containing the {@code SystemUnderTest} objects. Modifications to
    *         the returned list will not affect the internal list of the {@code TestReport}.
    */
   public List<SystemUnderTest> getSystemsUnderTest() {
      return new ArrayList<>(systemsUnderTest);
   }

   /**
    * Sets the list of systems under test associated with this test report.
    *
    * @param systemsUnderTest the list of {@code SystemUnderTest} objects to be set
    * @return the current instance of {@code TestReport} to allow method chaining
    */
   public TestReport setSystemsUnderTest(List<SystemUnderTest> systemsUnderTest) {
      this.systemsUnderTest = new ArrayList<>(systemsUnderTest);
      return this;
   }

   /**
    * Adds a {@code SystemUnderTest} to the list of systems under test associated with this {@code TestReport}.
    *
    * @param systemUnderTest the {@code SystemUnderTest} instance to be added to the test report
    * @return the current instance of {@code TestReport} to allow method chaining
    */
   public TestReport addSystemUnderTest(SystemUnderTest systemUnderTest) {
      systemsUnderTest.add(systemUnderTest);
      return this;
   }

   /**
    * Adds a list of systems under test to the current {@code TestReport}.
    *
    * @param systemsUnderTest the list of {@code SystemUnderTest} objects to be added to the test report
    * @return the current instance of {@code TestReport} to allow method chaining
    */
   public TestReport addSystemsUnderTest(List<SystemUnderTest> systemsUnderTest) {
      this.systemsUnderTest.addAll(systemsUnderTest);
      return this;
   }

   /**
    * Retrieves the result associated with the test report.
    *
    * @return the {@code Result} indicating the outcome of the test.
    */
   public Result getResult() {
      return result;
   }

   /**
    * Sets the result for the test report.
    *
    * @param result the {@code Result} indicating the outcome of the test report
    * @return the current instance of {@code TestReport} to allow method chaining
    */
   public TestReport setResult(Result result) {
      this.result = result;
      return this;
   }

   /**
    * Retrieves the name of the test suite associated with this test report.
    *
    * @return the name of the test suite as a String
    */
   public String getTestSuiteName() {
      return testSuiteName;
   }

   /**
    * Sets the name of the test suite associated with this test report.
    *
    * @param testSuiteName the name of the test suite to be set as a String
    * @return the current instance of {@code TestReport} to allow method chaining
    */
   public TestReport setTestSuiteName(String testSuiteName) {
      this.testSuiteName = testSuiteName;
      return this;
   }

   /**
    * Retrieves the test counters associated with the test report.
    *
    * @return a new instance of {@code TestCounters} containing the current state of test counters.
    */
   public TestCounters getTestCounters() {
      return new TestCounters(testCounters);
   }

   /**
    * Sets the test counters for the test report by creating a new instance of {@code TestCounters}
    * and assigning it to the current test report.
    *
    * @param testCounters the {@code TestCounters} instance containing the new state of test counters
    *                     to be associated with this test report
    * @return the current instance of {@code TestReport} to allow method chaining
    */
   public TestReport setTestCounters(TestCounters testCounters) {
      this.testCounters = new TestCounters(testCounters);
      return this;
   }

   /**
    * Retrieves the note associated with the test report.
    *
    * @return the note as a String.
    */
   public String getNote() {
      return note;
   }

   /**
    * Sets the note associated with this test report.
    *
    * @param note the note to be set for the test report as a String
    * @return the current instance of {@code TestReport} to allow method chaining
    */
   public TestReport setNote(String note) {
      this.note = note;
      return this;
   }

   /**
    * Retrieves the URL pointing to the test suite result associated with this test report.
    *
    * @return the URL to the test suite result as a String
    */
   public String getUrlToTestSuiteResult() {
      return urlToTestSuiteResult;
   }

   /**
    * Sets the URL pointing to the test suite result associated with this test report.
    *
    * @param urlToTestSuiteResult the URL to the test suite result to be set, as a String
    * @return the current instance of {@code TestReport} to allow method chaining
    */
   public TestReport setUrlToTestSuiteResult(String urlToTestSuiteResult) {
      this.urlToTestSuiteResult = urlToTestSuiteResult;
      return this;
   }

   /**
    * Retrieves the list of sub-reports associated with this test report.
    *
    * @return a new {@code List} containing the {@code TestReport} objects representing the sub-reports.
    */
   public List<TestReport> getSubReports() {
      return new ArrayList<>(subReports);
   }

   /**
    * Sets the list of sub-reports associated with this test report.
    *
    * @param subReports the list of {@code TestReport} objects to be set as sub-reports
    * @return the current instance of {@code TestReport} to allow method chaining
    */
   public TestReport setSubReports(List<TestReport> subReports) {
      this.subReports = new ArrayList<>(subReports);
      return this;
   }

   /**
    * Adds a sub-report to the list of sub-reports associated with this {@code TestReport}.
    *
    * @param subReport the {@code TestReport} instance to be added as a sub-report
    * @return the current instance of {@code TestReport} to allow method chaining
    */
   public TestReport addSubReport(TestReport subReport) {
      subReports.add(subReport);
      return this;
   }

   /**
    * Adds a list of sub-reports to the current {@code TestReport} instance.
    *
    * @param subReports the list of {@code TestReport} objects to be added as
    *                   sub-reports to the current test report
    * @return the updated instance of {@code TestReport} to allow method chaining
    */
   public TestReport addSubReports(List<TestReport> subReports) {
      this.subReports.addAll(subReports);
      return this;
   }

   /**
    * Retrieves the list of test run reports.
    *
    * @return a list of TestRunReport objects representing the reports of test runs.
    */
   public List<TestRunReport> getTestRunReports() {
      return new ArrayList<>(testRunReports);
   }

   /**
    * Sets the list of test run reports for this instance.
    *
    * @param testRunReports the list of TestRunReport objects to be assigned
    * @return the current TestReport instance with the updated testRunReports
    */
   public TestReport setTestRunReports(List<TestRunReport> testRunReports) {
      this.testRunReports = new ArrayList<>(testRunReports);
      return this;
   }

   /**
    * Adds a test run report to the current test report and returns the updated test report.
    *
    * @param testRunReport the test run report to be added
    * @return the updated test report after adding the test run report
    */
   public TestReport addTestRunReport(TestRunReport testRunReport) {
      testRunReports.add(testRunReport);
      return this;
   }

   /**
    * Adds a list of test run reports to the current collection of test run reports.
    *
    * @param testRunReports the list of {@code TestRunReport} objects to be added
    * @return the updated {@code TestReport} instance
    */
   public TestReport addTestRunReports(List<TestRunReport> testRunReports) {
      this.testRunReports.addAll(testRunReports);
      return this;
   }

   /**
    * Retrieves a list of unexpected errors that have been recorded.
    *
    * @return a list containing all recorded unexpected errors
    */
   public List<UnexpectedError> getUnexpectedErrors() {
      return new ArrayList<>(unexpectedErrors);
   }

   /**
    * Sets the list of unexpected errors associated with the test report.
    *
    * @param unexpectedErrors the list of unexpected errors to be set
    * @return the current instance of TestReport for method chaining
    */
   public TestReport setUnexpectedErrors(List<UnexpectedError> unexpectedErrors) {
      this.unexpectedErrors = new ArrayList<>(unexpectedErrors);
      return this;
   }

   /**
    * Adds an unexpected error to the test report.
    *
    * @param unexpectedError the unexpected error to be added to the test report
    * @return the current instance of TestReport with the added unexpected error
    */
   public TestReport addUnexpectedError(UnexpectedError unexpectedError) {
      unexpectedErrors.add(unexpectedError);
      return this;
   }

   /**
    * Adds a list of unexpected errors to the current test report.
    *
    * @param unexpectedErrors a list of {@code UnexpectedError} objects to be added to the test report
    * @return the current instance of {@code TestReport} with the added unexpected errors
    */
   public TestReport addUnexpectedErrors(List<UnexpectedError> unexpectedErrors) {
      this.unexpectedErrors.addAll(unexpectedErrors);
      return this;
   }

   @Override
   public AccessControlList getAccessControlList() {
      return accessControlList;
   }

   /**
    * Sets the access control list for the TestReport object.
    *
    * @param accessControlList the AccessControlList object to be assigned
    * @return the current instance of TestReport with the updated access control list
    */
   public TestReport setAccessControlList(AccessControlList accessControlList) {
      this.accessControlList = accessControlList;
      return this;
   }

   /**
    * Computes and updates the test counters by aggregating data from various sources.
    * <p>
    * This method initializes a new instance of the TestCounters object and processes
    * data from the following sources (if present):
    * <p>
    * 1. Sub-reports: Aggregates counters from {@code subReports} by retrieving
    *    their respective {@code TestCounters} and combining their values.
    * <p>
    * 2. Test run reports: Increments specific counters for each {@code testRunReports}
    *    by delegating to the {@code incrementCounterForATestRun} method.
    * <p>
    * 3. Unexpected errors: Increments the count of unexpected errors in the counters
    *    for each error found in the {@code unexpectedErrors} list.
    */
   public void computeCounters() {
      this.testCounters = new TestCounters();
      if (this.subReports != null) {
         subReports.stream()
               .map(TestReport::getTestCounters)
               .filter(Objects::nonNull)
               .forEach(testCounters::addNumbersFromSubCounters);
      }
      if (testRunReports != null) {
         testRunReports.forEach(this::incrementCounterForATestRun);
      }
      if (unexpectedErrors != null) {
         unexpectedErrors.forEach(error -> testCounters.incrementUnexpectedErrors());
      }
   }

   /**
    * Computes and returns the resulting value by aggregating multiple possible results.
    *
    * @return the computed {@code Result}, which is the maximum value among valid results,
    *         or {@code UNDEFINED} if no valid results are found.
    */
   public Result computeResult() {
      List<Optional<Result>> results = new ArrayList<>();
      results.add(getErrorsResult());
      results.add(getTestRunsResult());
      results.add(getSubReportResult());
      result = results.stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .max(Result::compareTo)
            .orElse(UNDEFINED);
      return result;
   }

   private Optional<Result> getErrorsResult() {
      return unexpectedErrors.isEmpty()
            ? Optional.empty()
            : Optional.of(UNDEFINED);
   }

   private Optional<Result> getTestRunsResult() {
      return testRunReports.isEmpty()
            ? Optional.empty()
            : testRunReports.stream()
            .map(TestRunReport::getResult)
            .filter(Objects::nonNull)
            .max(Result::compareTo);
   }

   private Optional<Result> getSubReportResult() {
      return subReports.isEmpty()
            ? Optional.empty()
            : subReports.stream()
            .map(TestReport::getResult)
            .filter(Objects::nonNull)
            .max(Result::compareTo);
   }

   private void incrementCounterForATestRun(TestRunReport testRunReport) {
      switch (testRunReport.getResult()) {
         case FAILED -> testCounters.incrementFailed();
         case PASSED -> testCounters.incrementPassed();
         case UNDEFINED -> {
            testCounters.incrementUndefined();
            if (!testRunReport.getUnexpectedErrors().isEmpty()) {
               testCounters.incrementUnexpectedErrors(testRunReport.getUnexpectedErrors().size());
            }
         }
      }
   }

   /**
    * Checks if the UUID is defined.
    *
    * @return true if the UUID is not null and not blank, false otherwise
    */
   public boolean isUuidDefined() {
      return uuid != null && !uuid.isBlank();
   }

   /**
    * Checks if the stored date-time is not in the future.
    *
    * @return true if the date-time is not in the future and not null, false otherwise
    */
   public boolean isDateTimeNotInTheFuture() {
      return dateTime != null && !dateTime.isAfter(Instant.now());
   }

   /**
    * Checks whether the computed result matches the expected result.
    *
    * @return true if the computed result equals the expected result; false otherwise
    */
   public boolean resultMatchesComputedResult() {
      TestReport tmp = new TestReport(this);
      tmp.computeResult();
      return tmp.getResult().equals(result);
   }

   /**
    * Checks if the test suite name is valid. A valid test suite name
    * is either null or non-blank.
    *
    * @return true if the test suite name is null or not blank, false otherwise
    */
   public boolean isTestSuiteNameValid() {
      return testSuiteName == null || !testSuiteName.isBlank();
   }

   /**
    * Compares the test counters from the TestReport instance with the computed counters.
    *
    * @return true if the computed counters match the testCounters field, false otherwise
    */
   public boolean testCountersMatchesComputedCounters() {
      TestReport tmp = new TestReport(this);
      tmp.computeCounters();
      return tmp.getTestCounters().equals(testCounters);
   }

   /**
    * Checks if the note is either not present (null) or is defined (not blank) if present.
    *
    * @return true if the note is null or not blank; false if the note is not null but blank.
    */
   public boolean isNoteDefinedIfPresent() {
      return note == null || !note.isBlank();
   }

   /**
    * Checks if the URL to the test suite result is defined, provided it is present.
    *
    * @return true if the URL is either null or non-blank; false otherwise
    */
   public boolean isUrlToTestSuiteResultDefinedIfPresent() {
      return urlToTestSuiteResult == null || !urlToTestSuiteResult.isBlank();
   }

   /**
    * Checks whether there is at least one result available among sub-reports, test run reports,
    * or unexpected errors.
    *
    * @return true if there is at least one result in either subReports, testRunReports, or
    * unexpectedErrors; false otherwise.
    */
   public boolean hasAtLeastOneResult() {
      return !(subReports.isEmpty() && testRunReports.isEmpty() && unexpectedErrors.isEmpty());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof TestReport that)) return false;
      return Objects.equals(reportVersion, that.reportVersion)
            && Objects.equals(uuid, that.uuid)
            && Objects.equals(dateTime, that.dateTime)
            && Objects.equals(testService, that.testService)
            && Objects.equals(systemsUnderTest, that.systemsUnderTest)
            && result == that.result
            && Objects.equals(testSuiteName, that.testSuiteName)
            && Objects.equals(testCounters, that.testCounters)
            && Objects.equals(note, that.note)
            && Objects.equals(urlToTestSuiteResult, that.urlToTestSuiteResult)
            && Objects.equals(subReports, that.subReports)
            && Objects.equals(testRunReports, that.testRunReports)
            && Objects.equals(unexpectedErrors, that.unexpectedErrors);
   }

   @Override
   public int hashCode() {
      return Objects.hash(reportVersion, uuid, dateTime, testService, systemsUnderTest, result, testSuiteName, testCounters, note, urlToTestSuiteResult, subReports, testRunReports, unexpectedErrors);
   }
}
