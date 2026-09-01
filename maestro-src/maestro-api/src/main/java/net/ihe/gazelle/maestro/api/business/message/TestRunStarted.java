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
 * A message to notify that a test execution started
 */
@Event
public class TestRunStarted implements Message {

   @Serial
   private static final long serialVersionUID = 1L;

   /**
    * The id of the test to execute
    */
   private String testId;

   /**
    * Default constructor
    */
   public TestRunStarted() {
   }

   /**
    * Constructs a new {@code TestRunStarted} instance with the specified test ID.
    *
    * @param testId the ID of the test to be executed
    */
   public TestRunStarted(String testId) {
      this.testId = testId;
   }

   /**
    * Retrieves the ID of the test to be executed.
    *
    * @return the test ID as a string.
    */
   public String getTestId() {
      return testId;
   }

   /**
    * Sets the ID of the test to be executed.
    *
    * @param testId the ID of the test to be set
    * @return the current instance of {@code TestRunStarted} to allow method chaining
    */
   public TestRunStarted setTestId(String testId) {
      this.testId = testId;
      return this;
   }
}
