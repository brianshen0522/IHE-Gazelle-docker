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

import net.ihe.gazelle.errorhandling.business.UnexpectedErrorBuilder;
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.framework.modelvalidator.business.utils.AbstractBuilder;
import net.ihe.gazelle.framework.modelvalidator.business.utils.AbstractValidator;
import net.ihe.gazelle.maestro.api.business.testreport.validator.TestReportValidator;
import net.ihe.gazelle.security.business.acl.AccessControlListBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builder for {@link TestReport}.
 */
public class TestReportBuilder extends AbstractBuilder<TestReport> {

   private String reportVersion;
   private final String uuid;
   private Instant dateTime;
   private TestServiceBuilder testService;
   private List<SystemUnderTestBuilder> systemsUnderTest;
   private String testSuiteName;
   private String note;
   private String urlToTestSuiteResult;
   private List<TestReportBuilder> subReports;
   private List<TestRunReport> testRunReports;
   private List<TestRunReportBuilder> testRunReportBuilders;
   private List<UnexpectedErrorBuilder> unexpectedErrors;
   private AccessControlListBuilder accessControlListBuilder;

   /**
    * Default constructor.
    */
   public TestReportBuilder() {
      super();
      uuid = UUID.randomUUID().toString();
      dateTime = Instant.now();
      systemsUnderTest = new ArrayList<>();
      subReports = new ArrayList<>();
      testRunReports = new ArrayList<>();
      testRunReportBuilders = new ArrayList<>();
      unexpectedErrors = new ArrayList<>();
   }

   /**
    * Constructs an instance of {@code TestReportBuilder} with the specified {@code ValidatorBuilderFactory}.
    *
    * @param validatorBuilderFactory the factory to create validators for objects; must not be null
    */
   public TestReportBuilder(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
      uuid = UUID.randomUUID().toString();
      dateTime = Instant.now();
      systemsUnderTest = new ArrayList<>();
      subReports = new ArrayList<>();
      testRunReports = new ArrayList<>();
      testRunReportBuilders = new ArrayList<>();
      unexpectedErrors = new ArrayList<>();
   }

   /**
    * Sets the report version for the current instance of {@code TestReportBuilder}.
    *
    * @return the current instance of {@code TestReportBuilder} with the report version set
    */
   public TestReportBuilder setReportVersion() {
      this.reportVersion = TestReport.REPORT_VERSION;
      return this;
   }

   /**
    * Sets the date and time for the current instance of {@code TestReportBuilder}.
    *
    * @param dateTime the {@code Instant} representing the date and time to be set
    * @return the current instance of {@code TestReportBuilder} with the date and time set
    */
   public TestReportBuilder setDateTime(Instant dateTime) {
      this.dateTime = dateTime;
      return this;
   }

   /**
    * Sets the test service for the current instance of {@code TestReportBuilder}.
    *
    * @param testService the instance of {@code TestServiceBuilder} representing the test service to be set
    * @return the current instance of {@code TestReportBuilder} with the test service set
    */
   public TestReportBuilder setTestService(TestServiceBuilder testService) {
      this.testService = testService;
      return this;
   }

   /**
    * Sets the list of systems under test for the current instance of {@code TestReportBuilder}.
    *
    * @param systemsUnderTest the list of {@code SystemUnderTestBuilder} instances representing
    *                         the systems under test; must not be null
    * @return the current instance of {@code TestReportBuilder} with the specified systems under test set
    */
   public TestReportBuilder setSystemsUnderTest(List<SystemUnderTestBuilder> systemsUnderTest) {
      this.systemsUnderTest = new ArrayList<>(systemsUnderTest);
      return this;
   }

   /**
    * Adds a system under test to the current instance of {@code TestReportBuilder}.
    *
    * @param systemUnderTest the instance of {@code SystemUnderTestBuilder} representing the system under test to be added; must not be null
    * @return the current instance of {@code TestReportBuilder} with the specified system under test added
    */
   public TestReportBuilder addSystemUnderTest(SystemUnderTestBuilder systemUnderTest) {
      this.systemsUnderTest.add(systemUnderTest);
      return this;
   }

   /**
    * Sets the name of the test suite for the current instance of {@code TestReportBuilder}.
    *
    * @param testSuiteName the name of the test suite to be set; must not be null or empty
    * @return the current instance of {@code TestReportBuilder} with the test suite name set
    */
   public TestReportBuilder setTestSuiteName(String testSuiteName) {
      this.testSuiteName = testSuiteName;
      return this;
   }

   /**
    * Sets the note for the current instance of {@code TestReportBuilder}.
    *
    * @param note the note to be associated with this test report
    * @return the current instance of {@code TestReportBuilder} with the note set
    */
   public TestReportBuilder setNote(String note) {
      this.note = note;
      return this;
   }

   /**
    * Sets the URL to the test suite result for the current instance of {@code TestReportBuilder}.
    *
    * @param urlToTestSuiteResult the URL of the test suite result to be set; must not be null or empty
    * @return the current instance of {@code TestReportBuilder} with the URL to the test suite result set
    */
   public TestReportBuilder setUrlToTestSuiteResult(String urlToTestSuiteResult) {
      this.urlToTestSuiteResult = urlToTestSuiteResult;
      return this;
   }

   /**
    * Sets the list of sub-reports for the current instance of {@code TestReportBuilder}.
    *
    * @param subReports the list of {@code TestReportBuilder} instances representing
    *                   the sub-reports to be set; must not be null
    * @return the current instance of {@code TestReportBuilder} with the sub-reports set
    */
   public TestReportBuilder setSubReports(List<TestReportBuilder> subReports) {
      this.subReports = new ArrayList<>(subReports);
      return this;
   }

   /**
    * Adds a sub-report to the current instance of {@code TestReportBuilder}.
    *
    * @param subReport the instance of {@code TestReportBuilder} representing the sub-report to be added; must not be null
    * @return the current instance of {@code TestReportBuilder} with the specified sub-report added
    */
   public TestReportBuilder addSubReport(TestReportBuilder subReport) {
      this.subReports.add(subReport);
      return this;
   }

   /**
    * Adds a list of {@code TestRunReportBuilder} instances to the current instance of {@code TestReportBuilder}.
    *
    * @param testRunReports the list of {@code TestRunReportBuilder} instances to be added; must not be null
    * @return the current instance of {@code TestReportBuilder} with the specified test run report builders added
    */
   public TestReportBuilder addTestRunReportBuilders(List<TestRunReportBuilder> testRunReports) {
      this.testRunReportBuilders.addAll(testRunReports);
      return this;
   }

   /**
    * Adds a list of {@code TestRunReport} instances to the current instance of {@code TestReportBuilder}.
    *
    * @param testRunReports the list of {@code TestRunReport} instances to be added; must not be null
    * @return the current instance of {@code TestReportBuilder} with the specified test run reports added
    */
   public TestReportBuilder addTestRunReports(List<TestRunReport> testRunReports) {
      this.testRunReports.addAll(testRunReports);
      return this;
   }

   /**
    * Adds a {@code TestRunReportBuilder} instance to the current instance of {@code TestReportBuilder}.
    *
    * @param testRunReport the {@code TestRunReportBuilder} instance to be added; must not be null
    * @return the current instance of {@code TestReportBuilder} with the specified test run report builder added
    */
   public TestReportBuilder addTestRunReport(TestRunReportBuilder testRunReport) {
      this.testRunReportBuilders.add(testRunReport);
      return this;
   }

   /**
    * Adds a {@code TestRunReport} instance to the current instance of {@code TestReportBuilder}.
    *
    * @param testRunReport the {@code TestRunReport} instance to be added; must not be null
    * @return the current instance of {@code TestReportBuilder} with the specified test run report added
    */
   public TestReportBuilder addTestRunReport(TestRunReport testRunReport) {
      this.testRunReports.add(testRunReport);
      return this;
   }

   /**
    * Sets the list of unexpected errors for the current instance of {@code TestReportBuilder}.
    *
    * @param unexpectedErrors the list of {@code UnexpectedErrorBuilder} instances representing
    *                         the unexpected errors to be set; if null, an empty list will be used
    * @return the current instance of {@code TestReportBuilder} with the specified unexpected errors set
    */
   public TestReportBuilder setUnexpectedErrors(List<UnexpectedErrorBuilder> unexpectedErrors) {
      this.unexpectedErrors = unexpectedErrors != null
            ? new ArrayList<>(unexpectedErrors)
            : new ArrayList<>();
      return this;
   }

   /**
    * Adds an unexpected error to the current instance of {@code TestReportBuilder}.
    *
    * @param unexpectedError the {@code UnexpectedErrorBuilder} instance representing the unexpected error to be added; must not be null
    * @return the current instance of {@code TestReportBuilder} with the specified unexpected error added
    */
   public TestReportBuilder addUnexpectedError(UnexpectedErrorBuilder unexpectedError) {
      this.unexpectedErrors.add(unexpectedError);
      return this;
   }

   /**
    * Adds a list of unexpected errors to the current instance of {@code TestReportBuilder}.
    *
    * @param unexpectedErrors the list of {@code UnexpectedErrorBuilder} instances representing
    *                         the unexpected errors to be added; must not be null
    * @return the current instance of {@code TestReportBuilder} with the specified unexpected errors added
    */
   public TestReportBuilder addUnexpectedErrors(List<UnexpectedErrorBuilder> unexpectedErrors) {
      this.unexpectedErrors.addAll(unexpectedErrors);
      return this;
   }

   /**
    * Sets the access control list for the current instance of {@code TestReportBuilder}.
    *
    * @param accessControlListBuilder the instance of {@code AccessControlListBuilder} representing
    *                                 the access control list to be set; must not be null
    * @return the current instance of {@code TestReportBuilder} with the access control list set
    */
   public TestReportBuilder setAccessControlList(AccessControlListBuilder accessControlListBuilder) {
      this.accessControlListBuilder = accessControlListBuilder;
      return this;
   }

   @Override
   protected AbstractValidator<TestReport> instantiateValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      return new TestReportValidator(validatorBuilderFactory);
   }

   @Override
   protected TestReport instantiate() {
      return new TestReport();
   }

   @Override
   protected void make(TestReport testReport) {
      testReport
            .setReportVersion(reportVersion)
            .setUuid(uuid)
            .setDateTime(dateTime)
            .setTestService(AbstractBuilder.staticBuildWithoutValidation(testService))
            .setSystemsUnderTest(
                  !systemsUnderTest.isEmpty() ? systemsUnderTest.stream()
                        .map(AbstractBuilder::staticBuildWithoutValidation)
                        .toList() : new ArrayList<>()
            )
            .setTestSuiteName(testSuiteName)
            .setNote(note)
            .setUrlToTestSuiteResult(urlToTestSuiteResult)
            .setSubReports(
                  !subReports.isEmpty() ? subReports.stream()
                        .map(AbstractBuilder::staticBuildWithoutValidation)
                        .toList() : new ArrayList<>()
            )
            .setTestRunReports(
                  !testRunReportBuilders.isEmpty() ? testRunReportBuilders.stream()
                        .map(AbstractBuilder::staticBuildWithoutValidation)
                        .toList() : new ArrayList<>()
            )
            .addTestRunReports(
                  !testRunReports.isEmpty() ? testRunReports : new ArrayList<>()
            )
            .setUnexpectedErrors(
                  !unexpectedErrors.isEmpty() ? unexpectedErrors.stream()
                        .map(AbstractBuilder::staticBuildWithoutValidation)
                        .toList() : new ArrayList<>()
            )
            .setAccessControlList(AbstractBuilder.staticBuildWithoutValidation(accessControlListBuilder))
      ;
      testReport.computeResult();
      testReport.computeCounters();
   }
}
