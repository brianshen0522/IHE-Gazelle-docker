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
 * A message to notify that a step execution started
 */
@Event
public class StepRunStarted implements Message {

   @Serial
   private static final long serialVersionUID = 1L;

   /**
    * The id of the test to execute
    */
   private String testId;

   /**
    * The index of the step to execute
    */
   private int stepIndex;

   /**
    * Default constructor
    */
   public StepRunStarted() {
   }

   /**
    * Creates a new instance of StepRunStarted with the specified test ID and step index.
    *
    * @param testId the ID of the test for which the step execution has started.
    * @param stepIndex the index of the step in the test that has started execution.
    */
   public StepRunStarted(String testId, int stepIndex) {
      this.testId = testId;
      this.stepIndex = stepIndex;
   }

   /**
    * Retrieves the ID of the test to execute.
    *
    * @return the test ID as a string.
    */
   public String getTestId() {
      return testId;
   }

   /**
    * Sets the test ID for the step execution.
    *
    * @param testId the ID of the test to execute.
    * @return the current instance of {@code StepRunStarted} to allow method chaining.
    */
   public StepRunStarted setTestId(String testId) {
      this.testId = testId;
      return this;
   }

   /**
    * Retrieves the index of the step to execute.
    *
    * @return the step index as an integer.
    */
   public int getStepIndex() {
      return stepIndex;
   }

   /**
    * Sets the index of the step to execute.
    *
    * @param stepIndex the index of the step to be executed.
    * @return the current instance of {@code StepRunStarted} to allow method chaining.
    */
   public StepRunStarted setStepIndex(int stepIndex) {
      this.stepIndex = stepIndex;
      return this;
   }
}
