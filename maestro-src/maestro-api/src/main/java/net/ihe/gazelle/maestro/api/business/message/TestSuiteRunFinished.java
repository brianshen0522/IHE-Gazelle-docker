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

package net.ihe.gazelle.maestro.api.business.message;

import net.ihe.gazelle.errorhandling.business.UnexpectedError;
import net.ihe.gazelle.lang.Event;
import net.ihe.gazelle.maestro.api.business.testreport.TestRunReport;
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRun;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * A message to notify that a Test suite Execution Ended
 */
@Event
public class TestSuiteRunFinished implements Message {

   @Serial
   private static final long serialVersionUID = 1L;

   /**
    * The session id of the execution
    */
   private String sessionId;

   /**
    * The test suite run that has ended
    */
   private TestSuiteRun testSuiteRun;

   /**
    * The reports of the test runs
    */
   private List<TestRunReport> testRunReports;

   /**
    * Unexpected errors that occurred during the execution
    */
   private List<UnexpectedError> unexpectedErrors;

   /**
    * Default constructor
    */
   public TestSuiteRunFinished() {
      this(null, null, null);
   }

   /**
    * Constructs a new instance of TestSuiteRunFinished.
    *
    * @param sessionId the session ID associated with the test suite execution
    * @param testSuiteRun the test suite run that has ended
    * @param testRunReports the list of reports generated from the individual test runs
    */
   public TestSuiteRunFinished(String sessionId, TestSuiteRun testSuiteRun, List<TestRunReport> testRunReports) {
      this.sessionId = sessionId;
      this.testSuiteRun = testSuiteRun;
      this.testRunReports = testRunReports != null ? new ArrayList<>(testRunReports) : new ArrayList<>();
      this.unexpectedErrors = new ArrayList<>();
   }

   /**
    * Retrieves the session ID associated with the test suite execution.
    *
    * @return the session ID as a string, or null if no session ID is set.
    */
   public String getSessionId() {
      return sessionId;
   }

   /**
    * Sets the session ID associated with the test suite execution.
    *
    * @param sessionId the session ID to be set
    * @return the current instance of {@code TestSuiteRunFinished} for method chaining
    */
   public TestSuiteRunFinished setSessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
   }

   /**
    * Retrieves the test suite run that has ended.
    *
    * @return the {@code TestSuiteRun} instance representing the test suite execution.
    */
   public TestSuiteRun getTestSuiteRun() {
      return testSuiteRun;
   }

   /**
    * Sets the test suite run associated with this instance.
    *
    * @param testSuiteRun the {@code TestSuiteRun} instance representing the test suite execution
    * @return the current instance of {@code TestSuiteRunFinished} for method chaining
    */
   public TestSuiteRunFinished setTestSuiteRun(TestSuiteRun testSuiteRun) {
      this.testSuiteRun = testSuiteRun;
      return this;
   }

   /**
    * Retrieves the list of reports generated from the individual test runs.
    *
    * @return a list of {@code TestRunReport} instances representing the test run reports.
    */
   public List<TestRunReport> getTestRunReports() {
      return new ArrayList<>(testRunReports);
   }

   /**
    * Sets the list of test run reports associated with this test suite run.
    * If the provided list is null, it initializes the list as an empty ArrayList.
    *
    * @param testRunReports the list of {@code TestRunReport} instances to be set
    * @return the current instance of {@code TestSuiteRunFinished} for method chaining
    */
   public TestSuiteRunFinished setTestRunReports(List<TestRunReport> testRunReports) {
      this.testRunReports = testRunReports != null ? new ArrayList<>(testRunReports) : new ArrayList<>();
      return this;
   }

   /**
    * Retrieves the list of unexpected errors encountered during the test suite execution.
    * A copy of the internal list is returned to ensure encapsulation.
    *
    * @return a list of {@code UnexpectedError} instances representing unexpected errors.
    */
   public List<UnexpectedError> getUnexpectedErrors() {
      return new ArrayList<>(unexpectedErrors);
   }

   /**
    * Sets the list of unexpected errors encountered during the test suite execution.
    * If the provided list is null, it initializes the list as an empty list.
    *
    * @param unexpectedErrors the list of {@code UnexpectedError} instances to be set
    * @return the current instance of {@code TestSuiteRunFinished} for method chaining
    */
   public TestSuiteRunFinished setUnexpectedErrors(List<UnexpectedError> unexpectedErrors) {
      this.unexpectedErrors = unexpectedErrors != null ? new ArrayList<>(unexpectedErrors) : new ArrayList<>();
      return this;
   }
}
