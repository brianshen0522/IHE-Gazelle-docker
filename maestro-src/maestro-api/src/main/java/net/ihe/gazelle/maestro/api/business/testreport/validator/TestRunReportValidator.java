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

package net.ihe.gazelle.maestro.api.business.testreport.validator;

import net.ihe.gazelle.errorhandling.business.UnexpectedErrorValidator;
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilder;
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.framework.modelvalidator.business.utils.AbstractValidator;
import net.ihe.gazelle.maestro.api.business.testreport.TestRunReport;

/**
 * Validator implementation for {@link TestRunReport}, responsible for defining
 */
public class TestRunReportValidator extends AbstractValidator<TestRunReport> {

   private final TestValidator testValidator;
   private final PropertyInReportValidator propertyValidator;
   private final ByteArrayItemPropertyValidator itemPropertyValidator;
   private final StepRunReportValidator stepRunReportValidator;
   private final UnexpectedErrorValidator unexpectedErrorValidator;

   /**
    * Default constructor
    */
   public TestRunReportValidator() {
      super();
      testValidator = new TestValidator();
      propertyValidator = new PropertyInReportValidator();
      itemPropertyValidator = new ByteArrayItemPropertyValidator();
      stepRunReportValidator = new StepRunReportValidator();
      unexpectedErrorValidator = new UnexpectedErrorValidator();
   }

   /**
    * Constructs a new {@code TestRunReportValidator} utilizing the provided {@link ValidatorBuilderFactory}
    *
    * @param validatorBuilderFactory the factory responsible for supplying
    *                                {@link ValidatorBuilder} instances for specific types
    */
   public TestRunReportValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
      testValidator = new TestValidator(validatorBuilderFactory);
      propertyValidator = new PropertyInReportValidator(validatorBuilderFactory);
      itemPropertyValidator = new ByteArrayItemPropertyValidator(validatorBuilderFactory);
      stepRunReportValidator = new StepRunReportValidator(validatorBuilderFactory);
      unexpectedErrorValidator = new UnexpectedErrorValidator(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends TestRunReport> instantiateValidatorBuilder(
         ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(TestRunReport.class);
   }

   @Override
   @SuppressWarnings("unchecked")
   protected void addRulesAndMembers(ValidatorBuilder<? extends TestRunReport> validatorBuilder) {
      validatorBuilder
            .addRule(TestRunReport::isDateTimeNotInTheFuture, "DateTime must be set and not in the future")
            .addRule(TestRunReport::resultMatchesComputedResult, "Result must be set adn reflect global steps result")
            .addRule(TestRunReport::isTestDefined, "Test must be set")
            .addRule(TestRunReport::isUrlToTestRunDefinedIfPresent, "URL must be set and not empty if present")
            .addMember("test", TestRunReport::getTest, testValidator::getAssembledValidatorBuilder)
            .addMember("inputs", TestRunReport::getInputs,
                  propertyValidator::getAssembledValidatorBuilder,
                  itemPropertyValidator::getAssembledValidatorBuilder)
            .addMember("outputs", TestRunReport::getOutputs,
                  propertyValidator::getAssembledValidatorBuilder,
                  itemPropertyValidator::getAssembledValidatorBuilder)
            .addMember("stepRunReports", TestRunReport::getStepRunReports,
                  stepRunReportValidator::getAssembledValidatorBuilder)
            .addMember("unexpectedErrors", TestRunReport::getUnexpectedErrors,
                  unexpectedErrorValidator::getAssembledValidatorBuilder);
   }
}
