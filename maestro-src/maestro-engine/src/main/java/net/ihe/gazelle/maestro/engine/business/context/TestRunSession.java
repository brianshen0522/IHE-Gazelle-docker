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

package net.ihe.gazelle.maestro.engine.business.context;

import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;

import java.util.ArrayList;
import java.util.List;

/**
 * TestRunSessionsStore is a structure used to store TestRun's context and StepRunReportBuilder of an executed step
 * during TestRun execution. It also contains a reference to the TestRun.
 */
public class TestRunSession {

   private final TestRun testRun;
   private final List<Property> context;
   private final StepCursor stepCursor;
   private final List<StepRunReport> stepReports;

   /**
    * Creates a new {@code TestRunSession} for the specified {@link TestRun}.
    *
    * @param testRun the test run associated with this session
    */
   public TestRunSession(TestRun testRun) {
      this.testRun = testRun;
      this.context = new ArrayList<>(testRun.getInputs());
      this.stepCursor = StepCursor.from(testRun.getTest().getSteps());
      this.stepReports = new ArrayList<>();
   }

   /**
    * Retrieves the {@link TestRun} associated with this session.
    *
    * @return the test run
    */
   public TestRun getTestRun() {
      return testRun;
   }

   /**
    * Returns a copy of the current context properties for this session.
    *
    * @return a list of context properties
    */
   public List<Property> getContext() {
      return new ArrayList<>(context);
   }

   /**
    * Adds properties to the current context.
    *
    * @param properties the list of properties to add
    * @return the current {@code TestRunSession} instance for method chaining
    */
   public TestRunSession addPropertiesToContext(List<Property> properties) {
      context.addAll(properties);
      return this;
   }

   /**
    * Indicates whether there is a next step to execute.
    *
    * @return {@code true} if a next step exists, {@code false} otherwise
    */
   public boolean hasNextStep() {
      return stepCursor.hasNext();
   }

   /**
    * Advances to the next step and returns it.
    *
    * @return the next {@link Step} to execute
    */
   public Step nextStep() {
      return stepCursor.next();
   }

   /**
    * Returns the current step that is being executed.
    *
    * @return the current {@link Step}
    */
   public Step currentStep() {
      return stepCursor.current();
   }

   /**
    * Returns the index of the current step.
    *
    * @return the index of the current step
    */
   public int currentStepIndex() {
      return stepCursor.currentIndex();
   }

   /**
    * Returns a copy of the list of executed step reports.
    *
    * @return a list of {@link StepRunReport} instances
    */
   public List<StepRunReport> getStepReports() {
      return new ArrayList<>(stepReports);
   }

   /**
    * Adds a {@link StepRunReport} to the list of executed step reports.
    *
    * @param stepReportBuilder the step report to add
    */
   public void addStepRunReport(StepRunReport stepReportBuilder) {
      stepReports.add(stepReportBuilder);
   }
}
