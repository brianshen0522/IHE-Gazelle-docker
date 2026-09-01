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
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;

import java.io.Serial;
import java.io.Serializable;

/**
 * A message used to notify that a test or a test suite execution ended
 */
@Event
public class ExecutionFinished implements Message {

   @Serial
   private static final long serialVersionUID = 1L;

   /**
    * The session id of the execution
    */
   private String sessionId;

   /**
    * The Test suite run or the test run
    */
   private Serializable run;

   /**
    * The location of the report
    */
   private String reportLocation;

   /**
    * The report
    */
   private TestReport report;

   /**
    * Default constructor
    */
   public ExecutionFinished() {
      // Empty
   }

   /**
    * Constructs an ExecutionFinished message.
    *
    * @param sessionId the session id of the execution
    * @param run the test suite run or individual test run
    * @param report the test report associated with the execution
    */
   public ExecutionFinished(String sessionId, Serializable run, TestReport report) {
      this.sessionId = sessionId;
      this.run = run;
      this.report = report;
   }

   /**
    * Retrieves the session ID associated with the execution.
    *
    * @return the session ID as a {@code String}
    */
   public String getSessionId() {
      return sessionId;
   }

   /**
    * Sets the session ID for the execution and returns the updated object.
    *
    * @param sessionId the session ID to associate with the execution
    * @return the updated {@code ExecutionFinished} instance
    */
   public ExecutionFinished setSessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
   }

   /**
    * Retrieves the test suite run or individual test run associated with the execution.
    *
    * @return the associated test suite run or test run as a {@code Serializable} object
    */
   public Serializable getRun() {
      return run;
   }

   /**
    * Sets the test suite run or individual test run associated with the execution.
    *
    * @param run the test suite run or individual test run to associate with the execution
    * @return the updated {@code ExecutionFinished} instance
    */
   public ExecutionFinished setRun(Serializable run) {
      this.run = run;
      return this;
   }

   /**
    * Retrieves the location of the report associated with the test or test suite execution.
    *
    * @return the report location as a {@code String}
    */
   public String getReportLocation() {
      return reportLocation;
   }

   /**
    * Sets the location of the report associated with the test or test suite execution.
    *
    * @param reportLocation the location of the report as a String
    * @return the updated {@code ExecutionFinished} instance
    */
   public ExecutionFinished setReportLocation(String reportLocation) {
      this.reportLocation = reportLocation;
      return this;
   }

   /**
    * Retrieves the test report associated with the execution.
    *
    * @return the test report as a {@code TestReport} object
    */
   public TestReport getReport() {
      return report;
   }

   /**
    * Sets the test report associated with the execution and returns the updated object.
    *
    * @param report the test report to associate with the execution
    * @return the updated {@code ExecutionFinished} instance
    */
   public ExecutionFinished setReport(TestReport report) {
      this.report = report;
      return this;
   }

}
