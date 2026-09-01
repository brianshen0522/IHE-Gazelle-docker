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

package net.ihe.gazelle.maestro.api.business.message;

import net.ihe.gazelle.lang.Event;

import java.io.Serial;

/**
 * A message to notify that a test suite execution started
 */
@Event
public class TestSuiteRunStarted implements Message {

   @Serial
   private static final long serialVersionUID = 1L;

   /**
    * The id of the test suite run
    */
   private String testSuiteRunId;

   /**
    * Default constructor
    */
   public TestSuiteRunStarted() {
   }

   /**
    * Constructs a new TestSuiteRunStarted instance with the specified test suite run ID.
    *
    * @param testSuiteRunId the ID of the test suite run that has started.
    */
   public TestSuiteRunStarted(String testSuiteRunId) {
      this.testSuiteRunId = testSuiteRunId;
   }

   /**
    * Retrieves the ID of the test suite run.
    *
    * @return the ID of the test suite run as a string.
    */
   public String getTestSuiteRunId() {
      return testSuiteRunId;
   }

   /**
    * Sets the ID of the test suite run and returns the current instance for method chaining.
    *
    * @param testSuiteRunId the ID of the test suite run to be set.
    * @return the current instance of {@code TestSuiteRunStarted} for method chaining.
    */
   public TestSuiteRunStarted setTestSuiteRunId(String testSuiteRunId) {
      this.testSuiteRunId = testSuiteRunId;
      return this;
   }
}
