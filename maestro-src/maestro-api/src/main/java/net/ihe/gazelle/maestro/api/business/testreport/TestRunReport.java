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

package net.ihe.gazelle.maestro.api.business.testreport;

import net.ihe.gazelle.errorhandling.business.UnexpectedError;
import net.ihe.gazelle.maestro.api.business.property.Property;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A structure for a test run report.
 * Business rules <br>
 * mandatory fields : dateTime, result, test
 */
public class TestRunReport implements Serializable {

   @Serial
   private static final long serialVersionUID = 6739258918175686572L;

   /**
    * The id of the run.
    */
   private String runId;

   /**
    * The date and time at which the test run was executed.
    */
   private Instant dateTime;

   /**
    * The result of the test run.
    */
   private Result result;

   /**
    * The test to be executed.
    */
   private Test test;

   /**
    * The inputs to the test.
    */
   private List<Property> inputs;

   /**
    * The outputs of the test.
    */
   private List<Property> outputs;

   /**
    * The step run reports.
    */
   private List<StepRunReport> stepRunReports;

   /**
    * A link to the test run result.
    */
   private String urlToTestRun;

   /**
    * Unexpected errors that occurred during the test run.
    */
   private List<UnexpectedError> unexpectedErrors;

   /**
    * Default constructor.
    */
   public TestRunReport() {
      dateTime = Instant.now();
      result = Result.UNDEFINED;
      inputs = new ArrayList<>();
      outputs = new ArrayList<>();
      stepRunReports = new ArrayList<>();
      unexpectedErrors = new ArrayList<>();
   }

   /**
    * Copy constructor that creates a new instance of {@code TestRunReport}
    * by copying the attributes from the specified {@code TestRunReport} instance.
    *
    * @param copy the {@code TestRunReport} instance to copy; must not be null
    */
   public TestRunReport(TestRunReport copy) {
      this();
      this.runId = copy.getRunId();
      this.dateTime = copy.getDateTime();
      this.result = copy.getResult();
      this.test = copy.getTest();
      this.inputs = new ArrayList<>(copy.getInputs());
      this.outputs = new ArrayList<>(copy.getOutputs());
      this.stepRunReports = new ArrayList<>(copy.getStepRunReports());
      this.urlToTestRun = copy.getUrlToTestRun();
      this.unexpectedErrors = new ArrayList<>(copy.getUnexpectedErrors());
   }

   /**
    * Retrieves the unique identifier for the test run.
    *
    * @return the run ID as a String
    */
   public String getRunId() {
      return runId;
   }

   /**
    * Sets the unique identifier for the test run.
    *
    * @param runId the unique identifier to set for the test run
    * @return the current instance of {@code TestRunReport} to allow for method chaining
    */
   public TestRunReport setRunId(String runId) {
      this.runId = runId;
      return this;
   }

   /**
    * Retrieves the date and time associated with this test run report.
    *
    * @return the date and time as an {@code Instant} object
    */
   public Instant getDateTime() {
      return dateTime;
   }

   /**
    * Sets the date and time associated with this test run report.
    *
    * @param dateTime the date and time to set for this test run report, represented as an {@code Instant}
    * @return the current instance of {@code TestRunReport} to allow for method chaining
    */
   public TestRunReport setDateTime(Instant dateTime) {
      this.dateTime = dateTime;
      return this;
   }

   /**
    * Retrieves the associated test for this test run report.
    *
    * @return the {@code Test} instance representing the test associated with this report;
    *         may be {@code null} if no test is set
    */
   public Test getTest() {
      return test;
   }

   /**
    * Sets the associated test for this test run report.
    *
    * @param test the {@code Test} instance representing the test to associate with this report
    * @return the current instance of {@code TestRunReport} to allow for method chaining
    */
   public TestRunReport setTest(Test test) {
      this.test = test;
      return this;
   }

   /**
    * Retrieves the list of input properties associated with this test run report.
    *
    * @return a list of {@code Property} objects representing the input properties;
    *         the list may be empty but will never be null
    */
   public List<Property> getInputs() {
      return new ArrayList<>(inputs);
   }

   /**
    * Sets the list of input properties associated with this test run report.
    *
    * @param inputs a list of {@code Property} objects representing the input properties;
    *               can be null, in which case an empty list will be set
    * @return the current instance of {@code TestRunReport} to allow for method chaining
    */
   public TestRunReport setInputs(List<Property> inputs) {
      this.inputs = inputs != null ? new ArrayList<>(inputs) : new ArrayList<>();
      return this;
   }

   /**
    * Adds a single {@code Property} to the list of inputs associated with this test run report.
    *
    * @param input the {@code Property} object to be added to the list of inputs; must not be null
    * @return the current instance of {@code TestRunReport}, allowing for method chaining
    */
   public TestRunReport addInput(Property input) {
      inputs.add(input);
      return this;
   }

   /**
    * Adds a list of input properties to the current test run report.
    *
    * @param inputs a list of {@code Property} objects to be added to the input properties of this test run report;
    *               must not be null
    * @return the current instance of {@code TestRunReport}, allowing for method chaining
    */
   public TestRunReport addInputs(List<Property> inputs) {
      this.inputs.addAll(inputs);
      return this;
   }

   /**
    * Retrieves the list of output properties associated with this test run report.
    *
    * @return a list of {@code Property} objects representing the output properties;
    *         the list may be empty but will never be null
    */
   public List<Property> getOutputs() {
      return new ArrayList<>(outputs);
   }

   /**
    * Sets the list of output properties associated with this test run report.
    *
    * @param outputs a list of {@code Property} objects representing the output properties;
    *                can be null, in which case an empty list will be set
    * @return the current instance of {@code TestRunReport} to allow method chaining
    */
   public TestRunReport setOutputs(List<Property> outputs) {
      this.outputs = outputs != null ? new ArrayList<>(outputs) : new ArrayList<>();
      return this;
   }

   /**
    * Adds a single {@code Property} to the list of outputs associated with this test run report.
    *
    * @param output the {@code Property} object to be added to the list of outputs; must not be null
    * @return the current instance of {@code TestRunReport}, allowing for method chaining
    */
   public TestRunReport addOutput(Property output) {
      outputs.add(output);
      return this;
   }

   /**
    * Adds a list of output properties to the current test run report.
    *
    * @param outputs a list of {@code Property} objects to be added to the output properties of this test run report;
    *                must not be null
    * @return the current instance of {@code TestRunReport}, allowing for method chaining
    */
   public TestRunReport addOutputs(List<Property> outputs) {
      this.outputs.addAll(outputs);
      return this;
   }

   /**
    * Retrieves the result of the test run report.
    *
    * @return the {@code Result} representing the outcome of the test;
    *         it can be {@code PASSED}, {@code FAILED}, or {@code UNDEFINED}.
    */
   public Result getResult() {
      return result;
   }

   /**
    * Sets the result of the test run report.
    *
    * @param result the {@code Result} representing the outcome of the test;
    *               it can be {@code PASSED}, {@code FAILED}, or {@code UNDEFINED}
    * @return the current instance of {@code TestRunReport}, allowing for method chaining
    */
   public TestRunReport setResult(Result result) {
      this.result = result;
      return this;
   }

   /**
    * Retrieves the list of step run reports associated with this test run report.
    *
    * @return a list of {@code StepRunReport} objects representing the step run reports;
    *         the list may be empty but will never be null
    */
   public List<StepRunReport> getStepRunReports() {
      return new ArrayList<>(stepRunReports);
   }

   /**
    * Sets the list of step run reports associated with this test run report.
    *
    * @param stepRunReports a list of {@code StepRunReport} objects representing the step run reports;
    *                       can be null, in which case an empty list will be set
    * @return the current instance of {@code TestRunReport}, allowing for method chaining
    */
   public TestRunReport setStepRunReports(List<StepRunReport> stepRunReports) {
      this.stepRunReports = stepRunReports != null ? new ArrayList<>(stepRunReports) : new ArrayList<>();
      return this;
   }

   /**
    * Adds a single {@code StepRunReport} to the list of step run reports associated with this test run report.
    *
    * @param stepRunReport the {@code StepRunReport} object to be added to the list of step run reports; must not be null
    * @return the current instance of {@code TestRunReport}, allowing for method chaining
    */
   public TestRunReport addStepRunReport(StepRunReport stepRunReport) {
      stepRunReports.add(stepRunReport);
      return this;
   }

   /**
    * Adds a list of step run reports to the current instance of {@code TestRunReport}.
    *
    * @param stepRunReports a list of {@code StepRunReport} objects to add;
    *                       must not be null and should not contain null elements
    * @return the current instance of {@code TestRunReport}, allowing for method chaining
    */
   public TestRunReport addStepRunReports(List<StepRunReport> stepRunReports) {
      this.stepRunReports.addAll(stepRunReports);
      return this;
   }

   /**
    * Retrieves the URL associated with this test run.
    *
    * @return the URL as a String, or null if no URL is set
    */
   public String getUrlToTestRun() {
      return urlToTestRun;
   }

   /**
    * Sets the URL associated with this test run.
    *
    * @param urlToTestRun the URL to associate with the test run, represented as a String
    * @return the current instance of {@code TestRunReport}, allowing for method chaining
    */
   public TestRunReport setUrlToTestRun(String urlToTestRun) {
      this.urlToTestRun = urlToTestRun;
      return this;
   }

   /**
    * Retrieves the list of unexpected errors associated with this test run report.
    *
    * @return a list of {@code UnexpectedError} objects representing unexpected errors;
    *         the list may be empty but will never be null
    */
   public List<UnexpectedError> getUnexpectedErrors() {
      return new ArrayList<>(unexpectedErrors);
   }

   /**
    * Sets the list of unexpected errors associated with this test run report.
    *
    * @param unexpectedErrors a list of {@code UnexpectedError} objects representing the unexpected errors
    *                         to associate with this test run report; can be null
    * @return the current instance of {@code TestRunReport}, allowing for method chaining
    */
   public TestRunReport setUnexpectedErrors(List<UnexpectedError> unexpectedErrors) {
      this.unexpectedErrors = unexpectedErrors != null ? new ArrayList<>(unexpectedErrors) : new ArrayList<>();
      return this;
   }

   /**
    * Adds a single {@code UnexpectedError} to the list of unexpected errors associated
    * with this test run report.
    *
    * @param unexpectedError the {@code UnexpectedError} object to add to the list; must not be null
    * @return the current instance of {@code TestRunReport}, allowing for method chaining
    */
   public TestRunReport addUnexpectedError(UnexpectedError unexpectedError) {
      unexpectedErrors.add(unexpectedError);
      return this;
   }

   /**
    * Adds a list of unexpected errors to the current test run report.
    *
    * @param unexpectedErrors the list of unexpected errors to be added
    * @return the current instance of TestRunReport with the added unexpected errors
    */
   public TestRunReport addUnexpectedErrors(List<UnexpectedError> unexpectedErrors) {
      this.unexpectedErrors.addAll(unexpectedErrors);
      return this;
   }

   /**
    * Computes the overall result based on the step run reports and unexpected errors.
    * If there are any unexpected errors, the result is set to UNDEFINED.
    * Otherwise, the method evaluates the step run results, determines the highest-order result,
    * and updates the result accordingly.
    *
    * @return the computed Result, which could be PASSED, FAILED, or UNDEFINED
    */
   public Result computeResult() {
      if (!unexpectedErrors.isEmpty()) {
         result = Result.UNDEFINED;
      } else {
         Optional<StepResult> globalStepResult = stepRunReports.stream()
               .map(StepRunReport::getResult)
               .filter(Objects::nonNull)
               .max(StepResult::compareTo);

         if (globalStepResult.isPresent()) {
            switch (globalStepResult.get()) {
               case PASSED -> result = Result.PASSED;
               case FAILED -> result = Result.FAILED;
               case DONE, UNDEFINED -> result = Result.UNDEFINED;
            }
         }
      }
      return result;
   }

   /**
    * Checks whether the specified date-time is not in the future.
    *
    * @return true if the date-time is not in the future or is null; false otherwise
    */
   public boolean isDateTimeNotInTheFuture() {
      return dateTime != null && !dateTime.isAfter(Instant.now());
   }

   /**
    * Compares the current result with the computed result of a test run.
    *
    * @return true if the computed result matches the current result, false otherwise
    */
   public boolean resultMatchesComputedResult() {
      TestRunReport tmp = new TestRunReport(this);
      tmp.computeResult();
      return tmp.getResult().equals(result);
   }

   /**
    * Checks if the test is defined.
    *
    * @return true if the test is not null, false otherwise.
    */
   public boolean isTestDefined() {
      return test != null;
   }

   /**
    * Checks if the URL for the test run is either not defined (null) or defined but not blank.
    *
    * @return true if the URL is null or not blank, otherwise false.
    */
   public boolean isUrlToTestRunDefinedIfPresent() {
      return urlToTestRun == null || !urlToTestRun.isBlank();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof TestRunReport testRunReport)) return false;
      return Objects.equals(runId, testRunReport.runId)
            && Objects.equals(dateTime, testRunReport.dateTime)
            && result == testRunReport.result
            && Objects.equals(test, testRunReport.test)
            && Objects.equals(inputs, testRunReport.inputs)
            && Objects.equals(outputs, testRunReport.outputs)
            && Objects.equals(stepRunReports, testRunReport.stepRunReports)
            && Objects.equals(urlToTestRun, testRunReport.urlToTestRun)
            && Objects.equals(unexpectedErrors, testRunReport.unexpectedErrors);
   }

   @Override
   public int hashCode() {
      return Objects.hash(runId, dateTime, result, test, inputs, outputs, stepRunReports, urlToTestRun, unexpectedErrors);
   }
}
