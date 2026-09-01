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
import net.ihe.gazelle.maestro.api.business.testreport.validator.StepRunReportValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link StepRunReport}.
 */
public class StepRunReportBuilder extends AbstractBuilder<StepRunReport> {

   private String stepName;
   private String type;
   private StepResult result;
   private List<Property> outputs;
   private List<UnexpectedErrorBuilder> unexpectedErrors;

   /**
    * Default constructor.
    */
   public StepRunReportBuilder() {
      super();
      outputs = new ArrayList<>();
      unexpectedErrors = new ArrayList<>();
   }

   /**
    * Constructs a new instance of {@code StepRunReportBuilder} with the specified validator factory.
    *
    * @param validatorBuilderFactory A factory to create validators for the object being built.
    */
   public StepRunReportBuilder(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
      outputs = new ArrayList<>();
      unexpectedErrors = new ArrayList<>();
   }

   /**
    * Sets the name of the step for the report being built.
    *
    * @param stepName the name of the step to be set
    * @return the current instance of {@code StepRunReportBuilder}, enabling method chaining
    */
   public StepRunReportBuilder setStepName(String stepName) {
      this.stepName = stepName;
      return this;
   }

   /**
    * Sets the type of the step run report being built.
    *
    * @param type the type of the step run report to be set
    * @return the current instance of {@code StepRunReportBuilder}, enabling method chaining
    */
   public StepRunReportBuilder setType(String type) {
      this.type = type;
      return this;
   }

   /**
    * Sets the result for the step run report being built.
    *
    * @param result the result of the step to be set, represented by an instance of {@code StepResult}
    * @return the current instance of {@code StepRunReportBuilder}, enabling method chaining
    */
   public StepRunReportBuilder setResult(StepResult result) {
      this.result = result;
      return this;
   }

   /**
    * Sets the list of outputs for the step run report being built.
    *
    * @param outputs the list of {@code Property} objects representing the outputs to be set
    * @return the current instance of {@code StepRunReportBuilder}, enabling method chaining
    */
   public StepRunReportBuilder setOutputs(List<Property> outputs) {
      this.outputs = new ArrayList<>(outputs);
      return this;
   }

   /**
    * Adds a single {@code Property} object to the list of outputs being built.
    *
    * @param output the {@code Property} object to be added to the outputs list
    * @return the current instance of {@code StepRunReportBuilder}, allowing for method chaining
    */
   public StepRunReportBuilder addOutput(Property output) {
      this.outputs.add(output);
      return this;
   }

   /**
    * Adds a list of {@code Property} objects to the outputs being built.
    *
    * @param outputs the list of {@code Property} objects to be added to the outputs list
    * @return the current instance of {@code StepRunReportBuilder}, allowing for method chaining
    */
   public StepRunReportBuilder addOutputs(List<Property> outputs) {
      this.outputs.addAll(outputs);
      return this;
   }

   /**
    * Retrieves the list of output properties associated with the step run report.
    *
    * @return a list of {@code Property} objects representing the outputs of the step run report
    */
   public List<Property> getOutputs() {
      return new ArrayList<>(outputs);
   }

   /**
    * Sets the list of unexpected errors for the step run report being built.
    *
    * @param unexpectedErrors the list of {@code UnexpectedErrorBuilder} instances
    *                         representing unexpected errors to be included in the report
    * @return the current instance of {@code StepRunReportBuilder}, allowing for method chaining
    */
   public StepRunReportBuilder setUnexpectedErrors(List<UnexpectedErrorBuilder> unexpectedErrors) {
      this.unexpectedErrors = new ArrayList<>(unexpectedErrors);
      return this;
   }

   /**
    * Adds a list of unexpected errors to the current step run report being built.
    *
    * @param unexpectedErrors the list of {@code UnexpectedErrorBuilder} instances
    *                         to be added to the report
    * @return the current instance of {@code StepRunReportBuilder}, allowing for method chaining
    */
   public StepRunReportBuilder addUnexpectedErrors(List<UnexpectedErrorBuilder> unexpectedErrors) {
      this.unexpectedErrors.addAll(unexpectedErrors);
      return this;
   }

   /**
    * Adds a single unexpected error to the step run report being built.
    *
    * @param unexpectedError the {@code UnexpectedErrorBuilder} instance representing the unexpected
    *                        error to be added to the report
    * @return the current instance of {@code StepRunReportBuilder}, enabling method chaining
    */
   public StepRunReportBuilder addUnexpectedError(UnexpectedErrorBuilder unexpectedError) {
      unexpectedErrors.add(unexpectedError);
      return this;
   }

   @Override
   protected AbstractValidator<StepRunReport> instantiateValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      return new StepRunReportValidator(validatorBuilderFactory);
   }

   @Override
   @SuppressWarnings("unchecked")
   protected StepRunReport instantiate() {
      return new StepRunReport();
   }

   @Override
   protected void make(StepRunReport stepRunReport) {
      stepRunReport
            .setStepName(stepName)
            .setType(type)
            .setResult(result)
            .setOutputs(outputs)
            .setUnexpectedErrors(
                  !unexpectedErrors.isEmpty() ? unexpectedErrors.stream()
                        .map(AbstractBuilder::staticBuildWithoutValidation)
                        .toList() : new ArrayList<>()
            ).computeResult();
   }
}
