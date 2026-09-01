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

import net.ihe.gazelle.errorhandling.business.UnexpectedErrorBuilder;
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.framework.modelvalidator.business.utils.AbstractBuilder;
import net.ihe.gazelle.framework.modelvalidator.business.utils.AbstractValidator;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.testreport.validator.TestRunReportValidator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link TestRunReport}.
 */
public class TestRunReportBuilder extends AbstractBuilder<TestRunReport> {

   private String runId;
   private Instant dateTime;
   private TestBuilder test;
   private List<Property> inputs;
   private List<Property> outputs;
   private List<StepRunReportBuilder> stepRunReportBuilders;
   private List<StepRunReport> stepRunReports;
   private String urlToTestRun;
   private List<UnexpectedErrorBuilder> unexpectedErrors;

   /**
    * Default constructor.
    */
   public TestRunReportBuilder() {
      super();
      dateTime = Instant.now();
      inputs = new ArrayList<>();
      outputs = new ArrayList<>();
      stepRunReportBuilders = new ArrayList<>();
      stepRunReports = new ArrayList<>();
      unexpectedErrors = new ArrayList<>();
   }

   /**
    * Constructs a new instance of TestRunReportBuilder with the specified ValidatorBuilderFactory.
    *
    * @param validatorBuilderFactory the factory to create the validator, used for
    *                                building validation logic for the TestRunReport
    */
   public TestRunReportBuilder(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
      dateTime = Instant.now();
      inputs = new ArrayList<>();
      outputs = new ArrayList<>();
      stepRunReportBuilders = new ArrayList<>();
      stepRunReports = new ArrayList<>();
      unexpectedErrors = new ArrayList<>();
   }

   /**
    * Sets the run ID for the test run report builder.
    *
    * @param runId the unique identifier for the current test run; must not be null
    * @return the current instance of {@code TestRunReportBuilder} to allow method chaining
    */
   public TestRunReportBuilder setRunId(String runId) {
      this.runId = runId;
      return this;
   }

   /**
    * Sets the date and time for the test run report.
    *
    * @param dateTime the timestamp for the test run, represented as an {@code Instant}.
    * @return the current instance of {@code TestRunReportBuilder} to allow method chaining.
    */
   public TestRunReportBuilder setDateTime(Instant dateTime) {
      this.dateTime = dateTime;
      return this;
   }

   /**
    * Sets the test instance for the test run report builder.
    *
    * @param test the {@code TestBuilder} instance containing details of the test to be used
    * @return the current instance of {@code TestRunReportBuilder} to allow method chaining
    */
   public TestRunReportBuilder setTest(TestBuilder test) {
      this.test = test;
      return this;
   }

   /**
    * Sets the list of input properties for the test run report builder.
    *
    * @param inputs a list of {@code Property} objects representing the inputs;
    *               if null, an empty list is used
    * @return the current instance of {@code TestRunReportBuilder} to allow method chaining
    */
   public TestRunReportBuilder setInputs(List<Property> inputs) {
      this.inputs = inputs != null ? new ArrayList<>(inputs) : new ArrayList<>();
      return this;
   }

   /**
    * Adds a single input property to the list of inputs for the test run report.
    *
    * @param input the {@code Property} object to be added as input; represents
    *              a key-value pair used in the test run configuration
    * @return the current instance of {@code TestRunReportBuilder} to allow method chaining
    */
   public TestRunReportBuilder addInput(Property input) {
      inputs.add(input);
      return this;
   }

   /**
    * Sets the list of output properties for the test run report builder.
    *
    * @param outputs a list of {@code Property} objects representing the outputs;
    *                if null, an empty list is used
    * @return the current instance of {@code TestRunReportBuilder} to allow method chaining
    */
   public TestRunReportBuilder setOutputs(List<Property> outputs) {
      this.outputs = outputs != null ? new ArrayList<>(outputs) : new ArrayList<>();
      return this;
   }

   /**
    * Adds an output property to the list of outputs for the test run report.
    *
    * @param output the {@code Property} object to be added as output; represents
    *               a key-value pair resulting from the test run
    * @return the current instance of {@code TestRunReportBuilder} to allow method chaining
    */
   public TestRunReportBuilder addOutput(Property output) {
      outputs.add(output);
      return this;
   }

   /**
    * Adds a {@code StepRunReportBuilder} instance to the list of step run report builders
    * for the test run report builder.
    *
    * @param stepRunReport the {@code StepRunReportBuilder} instance to be added; represents
    *                      details of a single step run in the test execution
    * @return the current instance of {@code TestRunReportBuilder} to allow method chaining
    */
   public TestRunReportBuilder addStepRunReport(StepRunReportBuilder stepRunReport) {
      this.stepRunReportBuilders.add(stepRunReport);
      return this;
   }

   /**
    * Adds a {@code StepRunReport} instance to the list of step run reports for the test run report.
    *
    * @param stepRunReports the {@code StepRunReport} instance to be added; represents
    *                       the report of a single step in the test execution
    * @return the current instance of {@code TestRunReportBuilder} to allow method chaining
    */
   public TestRunReportBuilder addStepRunReport(StepRunReport stepRunReports) {
      this.stepRunReports.add(stepRunReports);
      return this;
   }

   /**
    * Adds a list of {@code StepRunReportBuilder} instances to the list of step run report builders.
    *
    * @param stepRuns a list of {@code StepRunReportBuilder} instances to be added;
    *                 each represents the details of a step run in the test execution.
    * @return the current instance of {@code TestRunReportBuilder} to allow method chaining.
    */
   public TestRunReportBuilder addStepRunReportBuilders(List<StepRunReportBuilder> stepRuns) {
      this.stepRunReportBuilders.addAll(stepRuns);
      return this;
   }

   /**
    * Adds a list of {@code StepRunReport} instances to the list of step run reports for the test run.
    *
    * @param stepRunReports a list of {@code StepRunReport} objects to be added; each represents
    *                       the report of a single step in the test execution
    * @return the current instance of {@code TestRunReportBuilder} to allow method chaining
    */
   public TestRunReportBuilder addStepRunReports(List<StepRunReport> stepRunReports) {
      this.stepRunReports.addAll(stepRunReports);
      return this;
   }

   /**
    * Sets the URL associated with the test run in the test run report builder.
    *
    * @param urlToTestRun the URL representing the location or reference of the test run.
    * @return the current instance of {@code TestRunReportBuilder} to allow method chaining.
    */
   public TestRunReportBuilder setUrlToTestRun(String urlToTestRun) {
      this.urlToTestRun = urlToTestRun;
      return this;
   }

   /**
    * Sets the list of unexpected errors for the test run report builder.
    *
    * @param unexpectedErrors a list of {@code UnexpectedErrorBuilder} objects representing
    *                         unexpected errors to be included in the test run report
    * @return the current instance of {@code TestRunReportBuilder} to allow method chaining
    */
   public TestRunReportBuilder setUnexpectedErrors(List<UnexpectedErrorBuilder> unexpectedErrors) {
      this.unexpectedErrors = new ArrayList<>(unexpectedErrors);
      return this;
   }

   /**
    * Adds an unexpected error to the list of unexpected errors for the test run report.
    *
    * @param unexpectedError the {@code UnexpectedErrorBuilder} instance representing
    *                        the unexpected error to be added; includes details such as
    *                        the error name, message, and cause.
    * @return the current instance of {@code TestRunReportBuilder} to allow method chaining.
    */
   public TestRunReportBuilder addUnexpectedError(UnexpectedErrorBuilder unexpectedError) {
      this.unexpectedErrors.add(unexpectedError);
      return this;
   }

   @Override
   protected AbstractValidator<TestRunReport> instantiateValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      return new TestRunReportValidator(validatorBuilderFactory);
   }

   @Override
   protected TestRunReport instantiate() {
      return new TestRunReport();
   }

   @Override
   protected void make(TestRunReport testRunReport) {
      testRunReport.setRunId(runId)
            .setDateTime(dateTime)
            .setTest(AbstractBuilder.staticBuildWithoutValidation(test))
            .setInputs(inputs)
            .setOutputs(outputs)
            .setStepRunReports(
                  !stepRunReportBuilders.isEmpty() ? stepRunReportBuilders.stream()
                        .map(AbstractBuilder::staticBuildWithoutValidation)
                        .toList() : new ArrayList<>()
            )
            .addStepRunReports(
                  !stepRunReports.isEmpty() ? stepRunReports : new ArrayList<>()
            )
            .setUrlToTestRun(urlToTestRun)
            .setUnexpectedErrors(
                  !unexpectedErrors.isEmpty() ? unexpectedErrors.stream()
                        .map(AbstractBuilder::staticBuildWithoutValidation)
                        .toList() : new ArrayList<>()
            ).computeResult();
   }
}
