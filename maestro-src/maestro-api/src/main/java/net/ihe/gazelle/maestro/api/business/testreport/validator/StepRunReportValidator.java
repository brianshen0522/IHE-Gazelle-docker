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
import net.ihe.gazelle.maestro.api.business.property.PropertyValidator;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;

/**
 * Validator implementation for {@link StepRunReport}, responsible for defining
 */
public class StepRunReportValidator extends AbstractValidator<StepRunReport> {

   private final PropertyValidator propertyValidator;
   private final UnexpectedErrorValidator unexpectedErrorValidator;

   /**
    * Default constructor
    */
   public StepRunReportValidator() {
      super();
      propertyValidator = new PropertyValidator();
      unexpectedErrorValidator = new UnexpectedErrorValidator();
   }

   /**
    * Constructs a new {@code StepRunReportValidator} with the specified {@link ValidatorBuilderFactory}.
    *
    * @param validatorBuilderFactory the factory used to create a validator builder
    */
   public StepRunReportValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
      propertyValidator = new PropertyValidator(validatorBuilderFactory);
      unexpectedErrorValidator = new UnexpectedErrorValidator(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends StepRunReport> instantiateValidatorBuilder(ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(StepRunReport.class);
   }

   @Override
   @SuppressWarnings("unchecked")
   protected void addRulesAndMembers(ValidatorBuilder<? extends StepRunReport> validatorBuilder) {
      validatorBuilder
            .addRule(StepRunReport::isStepNameDefined, "Step Name must be set and not empty")
            .addRule(StepRunReport::isTypeDefined, "Type must be set and not empty")
            .addRule(StepRunReport::isResultDefined, "Result must be set")
            .addMember("outputs", StepRunReport::getOutputs, propertyValidator::getAssembledValidatorBuilder)
            .addMember("unexpectedErrors", StepRunReport::getUnexpectedErrors, unexpectedErrorValidator::getAssembledValidatorBuilder);
   }
}
