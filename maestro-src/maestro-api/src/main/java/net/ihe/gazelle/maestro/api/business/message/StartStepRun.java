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
import net.ihe.gazelle.maestro.spi.business.StepRun;

import java.io.Serial;

/**
 * Step message used by the producer to notify a subscriber to launch step execution
 */
@Command
public class StartStepRun implements Message {

   @Serial
   private static final long serialVersionUID = 1L;

   /**
    * The session id of the execution
    */
   private String sessionId; // internal

   /**
    * The test id of the step to be executed
    */
   private String testId;

   /**
    * The index of the step to be executed
    */
   private int stepIndex;

   /**
    * The step run to be executed
    */
   private StepRun stepRun;

   /**
    * Default constructor
    */
   public StartStepRun() {
      this(null, null, -1, null);
   }

   /**
    * Constructs a new StartStepRun instance with the specified parameters.
    *
    * @param sessionId the session ID associated with the execution.
    * @param testId the ID of the test linked to the step to be executed.
    * @param stepIndex the index of the step in the test.
    * @param stepRun the StepRun instance representing the step to be executed.
    */
   public StartStepRun(String sessionId, String testId, int stepIndex, StepRun stepRun) {
      this.sessionId = sessionId;
      this.testId = testId;
      this.stepIndex = stepIndex;
      this.stepRun = stepRun;
   }

   /**
    * Retrieves the session ID associated with the execution.
    *
    * @return the session ID as a string, or null if no session ID is set.
    */
   public String getSessionId() {
      return sessionId;
   }

   /**
    * Sets the session ID associated with the execution.
    *
    * @param sessionId the session ID to be set.
    * @return the current instance of {@code StartStepRun} for method chaining.
    */
   public StartStepRun setSessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
   }

   /**
    * Retrieves the test ID of the step to be executed.
    *
    * @return the test ID as a string, or null if no test ID is set.
    */
   public String getTestId() {
      return testId;
   }

   /**
    * Sets the test ID of the step to be executed.
    *
    * @param testId the ID of the test linked to the step execution.
    * @return the current instance of {@code StartStepRun} for method chaining.
    */
   public StartStepRun setTestId(String testId) {
      this.testId = testId;
      return this;
   }

   /**
    * Retrieves the index of the step to be executed.
    *
    * @return the step index as an integer.
    */
   public int getStepIndex() {
      return stepIndex;
   }

   /**
    * Sets the index of the step to be executed.
    *
    * @param stepIndex the index of the step to be executed.
    * @return the current instance of {@code StartStepRun} for method chaining.
    */
   public StartStepRun setStepIndex(int stepIndex) {
      this.stepIndex = stepIndex;
      return this;
   }

   /**
    * Retrieves the StepRun instance associated with the current execution.
    *
    * @return the StepRun instance representing the step to be executed, or null if no StepRun is set.
    */
   public StepRun getStepRun() {
      return stepRun;
   }

   /**
    * Sets the {@link StepRun} instance associated with the current execution.
    * This method allows configuring the step to be executed and supports method chaining.
    *
    * @param stepRun the {@link StepRun} instance representing the step to be executed.
    * @return the current instance of {@code StartStepRun} for method chaining.
    */
   public StartStepRun setStepRun(StepRun stepRun) {
      this.stepRun = stepRun;
      return this;
   }

}
