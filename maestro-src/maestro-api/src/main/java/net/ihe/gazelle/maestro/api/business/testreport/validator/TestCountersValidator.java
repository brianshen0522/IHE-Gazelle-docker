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

import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilder;
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.framework.modelvalidator.business.utils.AbstractValidator;
import net.ihe.gazelle.maestro.api.business.testreport.TestCounters;

/**
 * Validator implementation for {@link TestCounters}, responsible for defining
 */
public class TestCountersValidator extends AbstractValidator<TestCounters> {

   /**
    * Default constructor
    */
   public TestCountersValidator() {
      super();
   }

   /**
    * Constructs a new TestCountersValidator with the specified ValidatorBuilderFactory.
    *
    * @param validatorBuilderFactory the factory to create the validator builder
    */
   public TestCountersValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends TestCounters> instantiateValidatorBuilder(ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(TestCounters.class);
   }

   @Override
   protected void addRulesAndMembers(ValidatorBuilder<? extends TestCounters> validatorBuilder) {
      validatorBuilder
            .addRule(TestCounters::isTotalEqualsToSumOfPassedFailedAndUndefined,
            "Total must be equal to the sum of passed, failed and undefined tests")
            .addRule(TestCounters::isPassedPositive, "Passed must be positive")
            .addRule(TestCounters::isFailedPositive, "Failed must be positive")
            .addRule(TestCounters::isUndefinedPositive, "Undefined must be positive")
            .addRule(TestCounters::isUnexpectedPositive, "Unexpected must be positive");
   }
}
