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

import net.ihe.gazelle.lang.Command;
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRun;

import java.io.Serial;

/**
 * A message to launch test set execution
 */
@Command
public class StartTestSuiteRun implements Message {

   @Serial
   private static final long serialVersionUID = 1L;

   /**
    * The session id of the execution
    */
   private String sessionId;

   /**
    * The test suite run to be executed
    */
   private TestSuiteRun testSuiteRun;

   /**
    * Default constructor
    */
   public StartTestSuiteRun() {
      this(null, null);
   }

   /**
    * Initializes a new instance of the StartTestSuiteRun class with the specified session ID and test suite run details.
    *
    * @param sessionId the unique identifier for the session during which the test suite run will be executed
    * @param testSuiteRun the details of the test suite run to be executed
    */
   public StartTestSuiteRun(String sessionId, TestSuiteRun testSuiteRun) {
      this.sessionId = sessionId;
      this.testSuiteRun = testSuiteRun;
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
    * @param sessionId the unique identifier for the session during which the test suite run will be executed
    * @return the current instance of {@code StartTestSuiteRun} for method chaining
    */
   public StartTestSuiteRun setSessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
   }

   /**
    * Retrieves the details of the test suite run to be executed.
    *
    * @return the {@code TestSuiteRun} object representing the details of the test suite run.
    */
   public TestSuiteRun getTestSuiteRun() {
      return testSuiteRun;
   }

   /**
    * Sets the test suite run details to be executed.
    *
    * @param testSuiteRun the {@code TestSuiteRun} object representing the details of the test suite run
    * @return the current instance of {@code StartTestSuiteRun} for method chaining
    */
   public StartTestSuiteRun setTestSuiteRun(TestSuiteRun testSuiteRun) {
      this.testSuiteRun = testSuiteRun;
      return this;
   }

}
