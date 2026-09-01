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
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;

import java.io.Serial;

/**
 * A message to start TestRun Execution
 */
@Command
public class StartTestRun implements Message {

   @Serial
   private static final long serialVersionUID = 1L;

   /**
    * The session id of the execution
    */
   private String sessionId;

   /**
    * The test run to be executed
    */
   private TestRun testRun;

   /**
    * Default constructor
    */
   public StartTestRun() {
      this(null, null);
   }

   /**
    * Constructs a new {@code StartTestRun} instance with the specified session ID and test run.
    *
    * @param sessionId the session ID associated with the test run execution.
    * @param testRun the {@code TestRun} instance representing the test to be executed.
    */
   public StartTestRun(String sessionId, TestRun testRun) {
      this.sessionId = sessionId;
      this.testRun = testRun;
   }

   /**
    * Retrieves the session ID associated with the test run execution.
    *
    * @return the session ID as a string, or null if no session ID is set.
    */
   public String getSessionId() {
      return sessionId;
   }

   /**
    * Sets the session ID associated with the test run execution.
    *
    * @param sessionId the session ID to be set.
    * @return the current instance of {@code StartTestRun} for method chaining.
    */
   public StartTestRun setSessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
   }

   /**
    * Retrieves the {@code TestRun} instance associated with this execution.
    *
    * @return the {@code TestRun} to be executed, or null if no {@code TestRun} is set.
    */
   public TestRun getTestRun() {
      return testRun;
   }

   /**
    * Sets the {@code TestRun} instance to be executed.
    *
    * @param testRun the {@code TestRun} instance representing the test to be executed.
    * @return the current instance of {@code StartTestRun} for method chaining.
    */
   public StartTestRun setTestRun(TestRun testRun) {
      this.testRun = testRun;
      return this;
   }

}
