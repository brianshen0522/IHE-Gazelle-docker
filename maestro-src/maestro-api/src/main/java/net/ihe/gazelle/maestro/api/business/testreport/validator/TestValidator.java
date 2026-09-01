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
import net.ihe.gazelle.maestro.api.business.testreport.Test;

/**
 * Validator implementation for {@link Test}, responsible for defining
 */
public class TestValidator extends AbstractValidator<Test> {

   /**
    * Default constructor
    */
   public TestValidator() {
      super();
   }

   /**
    * Constructs a new TestValidator instance with the specified ValidatorBuilderFactory.
    *
    * @param validatorBuilderFactory the factory used to create validator builders
    */
   public TestValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends Test> instantiateValidatorBuilder(ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(Test.class);
   }

   @Override
   protected void addRulesAndMembers(ValidatorBuilder<? extends Test> validatorBuilder) {
      validatorBuilder
            .addRule(Test::isIdDefined, "Id must be set and not empty")
            .addRule(Test::isNameDefinedIfPresent, "Name must be set if present")
            .addRule(Test::isVersionDefinedIfPresent, "Version must be set if present")
            .addRule(Test::isDescriptionDefinedIfPresent, "Description must be set id present");
   }
}
