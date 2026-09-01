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

import net.ihe.gazelle.lang.Event;
import net.ihe.gazelle.maestro.api.business.testreport.TestRunReport;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;

import java.io.Serial;

/**
 * A Message to notify that a TestRun execution ended
 */
@Event
public class TestRunFinished implements Message {

   @Serial
   private static final long serialVersionUID = 1L;

   /**
    * The session id of the execution
    */
   private String sessionId;

   /**
    * The test run that has ended
    */
   private TestRun testRun;

   /**
    * The report of the test run
    */
   private TestRunReport testRunReport;

   /**
    * Default constructor
    */
   public TestRunFinished() {
   }

   /**
    * Creates a new instance of TestRunFinished, representing the completion of a TestRun execution,
    * along with its associated session ID and test run report.
    *
    * @param sessionId     the session ID of the test run execution
    * @param testRun       the completed TestRun instance
    * @param testRunReport the report generated for the completed test run
    */
   public TestRunFinished(String sessionId, TestRun testRun, TestRunReport testRunReport) {
      this.sessionId = sessionId;
      this.testRun = testRun;
      this.testRunReport = testRunReport;
   }

   /**
    * Retrieves the session ID of the test run execution.
    *
    * @return the session ID as a string, or null if no session ID is set.
    */
   public String getSessionId() {
      return sessionId;
   }

   /**
    * Sets the session ID of the test run execution.
    *
    * @param sessionId the session ID to be set for the test run execution
    * @return the current instance of {@code TestRunFinished} for method chaining
    */
   public TestRunFinished setSessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
   }

   /**
    * Retrieves the {@code TestRun} instance associated with this object, representing
    * an execution of a specific test with defined inputs and configurations.
    *
    * @return the {@code TestRun} instance, or null if no test run is currently set
    */
   public TestRun getTestRun() {
      return testRun;
   }

   /**
    * Sets the {@code TestRun} instance associated with this object.
    *
    * @param testRun the {@code TestRun} instance to set
    * @return the current instance of {@code TestRunFinished} for method chaining
    */
   public TestRunFinished setTestRun(TestRun testRun) {
      this.testRun = testRun;
      return this;
   }

   /**
    * Retrieves the test run report generated for the completed test run execution.
    *
    * @return the {@code TestRunReport} associated with the completed test run, or null if no report is set
    */
   public TestRunReport getTestRunReport() {
      return testRunReport;
   }

   /**
    * Sets the {@code TestRunReport} associated with the completed test run execution.
    * This report contains details about the results and execution of the test run.
    *
    * @param testRunReport the {@code TestRunReport} instance to be set
    * @return the current instance of {@code TestRunFinished} for method chaining
    */
   public TestRunFinished setTestRunReport(TestRunReport testRunReport) {
      this.testRunReport = testRunReport;
      return this;
   }
}