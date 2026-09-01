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
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.spi.business.StepRun;

import java.io.Serial;

/**
 * A message used to notify that a step execution ended
 */
@Event
public class StepRunFinished implements Message {

   @Serial
   private static final long serialVersionUID = 1L;

   /**
    * The session id of the execution
    */
   private String sessionId;

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
    * The report builder used to build the step report.
    */
   private StepRunReport stepRunReport;

   /**
    * Default constructor
    */
   public StepRunFinished() {
   }

   /**
    * Constructs a StepRunFinished message that represents the end of a step's execution,
    * carrying details about the step run and its report.
    *
    * @param sessionId     the session ID of the execution
    * @param testId        the test ID of the step
    * @param stepIndex     the index of the step in the test
    * @param stepRun       the execution information of the step
    * @param stepRunReport the report generated for the step execution
    */
   public StepRunFinished(String sessionId, String testId, int stepIndex, StepRun stepRun, StepRunReport stepRunReport) {
      this.sessionId = sessionId;
      this.testId = testId;
      this.stepIndex = stepIndex;
      this.stepRun = stepRun;
      this.stepRunReport = stepRunReport;
   }

   /**
    * Retrieves the session ID associated with the execution.
    *
    * @return the session ID as a string.
    */
   public String getSessionId() {
      return sessionId;
   }

   /**
    * Sets the session ID associated with the step execution.
    *
    * @param sessionId the session ID to be set.
    * @return the current instance of {@code StepRunFinished} for method chaining.
    */
   public StepRunFinished setSessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
   }

   /**
    * Retrieves the test ID of the step to be executed.
    *
    * @return the test ID as a string.
    */
   public String getTestId() {
      return testId;
   }

   /**
    * Sets the test ID associated with the step execution.
    *
    * @param testId the test ID to be set.
    * @return the current instance of {@code StepRunFinished} for method chaining.
    */
   public StepRunFinished setTestId(String testId) {
      this.testId = testId;
      return this;
   }

   /**
    * Retrieves the index of the step associated with the execution.
    *
    * @return the step index as an integer.
    */
   public int getStepIndex() {
      return stepIndex;
   }

   /**
    * Sets the index of the step associated with the execution.
    *
    * @param stepIndex the index of the step to be set.
    * @return the current instance of {@code StepRunFinished} for method chaining.
    */
   public StepRunFinished setStepIndex(int stepIndex) {
      this.stepIndex = stepIndex;
      return this;
   }

   /**
    * Retrieves the StepRun instance associated with the execution.
    *
    * @return the StepRun instance containing execution details of the step.
    */
   public StepRun getStepRun() {
      return stepRun;
   }

   /**
    * Sets the StepRun instance associated with the execution.
    *
    * @param stepRun the StepRun instance containing execution details of the step
    * @return the current instance of {@code StepRunFinished} for method chaining
    */
   public StepRunFinished setStepRun(StepRun stepRun) {
      this.stepRun = stepRun;
      return this;
   }

   /**
    * Retrieves the report generated for the execution of a step.
    *
    * @return the {@code StepRunReport} instance containing details about the step execution report.
    */
   public StepRunReport getStepRunReport() {
      return stepRunReport;
   }

   /**
    * Sets the {@code StepRunReport} instance associated with the step execution.
    *
    * @param stepRunReport the {@code StepRunReport} containing details about the step execution report
    * @return the current instance of {@code StepRunFinished} for method chaining
    */
   public StepRunFinished setStepRunReport(StepRunReport stepRunReport) {
      this.stepRunReport = stepRunReport;
      return this;
   }
}
