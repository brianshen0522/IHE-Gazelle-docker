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
import net.ihe.gazelle.maestro.api.business.property.PropertyValidator;

/**
 * Validator implementation for {@link TestReference}, responsible for defining
 */
public class TestReferenceValidator extends AbstractValidator<TestReference> {

   private final PropertyValidator propertyValidator;

   /**
    * Default constructor.
    */
   public TestReferenceValidator() {
      super();
      this.propertyValidator = new PropertyValidator();
   }

   /**
    * Constructs a new instance of {@code TestReferenceValidator} with the specified {@link ValidatorBuilderFactory}.
    *
    * @param validatorBuilderFactory the factory used to create validator builders for
    *                                {@code TestReference} and its related validation components
    */
   public TestReferenceValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
      this.propertyValidator = new PropertyValidator(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends TestReference> instantiateValidatorBuilder(
         ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(TestReference.class);
   }

   @Override
   @SuppressWarnings("unchecked")
   protected void addRulesAndMembers(ValidatorBuilder<? extends TestReference> validatorBuilder) {
      validatorBuilder
            .addRule(TestReference::isTestIdDefined, "Test ID must be set and not empty")
            .addMember("properties", TestReference::getProperties, propertyValidator::getAssembledValidatorBuilder);
   }
}
