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

package net.ihe.gazelle.maestro.api.business.test;

import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilder;
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.framework.modelvalidator.business.utils.AbstractValidator;

import java.util.List;

/**
 * Validator for the {@link Test} class that ensures specific validation rules are applied.
 */
public class TestValidator extends AbstractValidator<Test> {

   private final StepValidator stepValidator;
   private final List<SupportedInputValidator<?>> supportedInputValidators;

   /**
    * Default constructor.
    */
   public TestValidator() {
      super();
      this.stepValidator = new StepValidator();
      this.supportedInputValidators = SupportedInputValidators.getAllSupportedInputValidators();
   }

   /**
    * Constructs a {@code TestValidator} using the specified {@link ValidatorBuilderFactory}.
    *
    * @param validatorBuilderFactory The factory used to create {@link ValidatorBuilder} instances
    *                                for the {@code TestValidator} and its associated components.
    */
   public TestValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
      this.stepValidator = new StepValidator(validatorBuilderFactory);
      this.supportedInputValidators = SupportedInputValidators.getAllSupportedInputValidators(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends Test> instantiateValidatorBuilder(
         ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(Test.class);
   }

   @Override
   @SuppressWarnings("unchecked")
   protected void addRulesAndMembers(ValidatorBuilder<? extends Test> validatorBuilder) {
      validatorBuilder
            .addRule(Test::isIdDefined, "ID must be set")
            .addRule(Test::isNameDefined, "Name must be set and not empty")
            .addRule(Test::atLeastOneStep, "Test must contain at least one step")
            .addRule(Test::areStepsUnique, "Steps must be unique")
            .addMember("steps", Test::getSteps, stepValidator::getAssembledValidatorBuilder)
            .addMember("supportedInputs", Test::getSupportedInputs,
                  SupportedInputValidators.getAssembledSuppliers(supportedInputValidators)
            );
   }

}
