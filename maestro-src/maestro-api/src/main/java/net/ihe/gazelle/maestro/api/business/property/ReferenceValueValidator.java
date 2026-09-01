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

package net.ihe.gazelle.maestro.api.business.property;

import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilder;
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.framework.modelvalidator.business.utils.AbstractValidator;

/**
 * Validator implementation for {@link ReferenceValue}, responsible for defining
 * validation rules and structure for instances of the {@code ReferenceValue} class.
 * This class ensures that a {@code ReferenceValue} object meets pre-defined validation
 * criteria.
 */
class ReferenceValueValidator extends AbstractValidator<ReferenceValue> {

   /**
    * Default constructor.
    */
   public ReferenceValueValidator() {
      super();
   }

   /**
    * Constructs a ReferenceValueValidator with the provided ValidatorBuilderFactory.
    *
    * @param validatorBuilderFactory the factory used to instantiate validator builders
    *                                for the {@code ReferenceValue} validation process
    */
   public ReferenceValueValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends ReferenceValue> instantiateValidatorBuilder(ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(ReferenceValue.class);
   }

   @Override
   protected void addRulesAndMembers(ValidatorBuilder<? extends ReferenceValue> validatorBuilder) {
      validatorBuilder
            .addRule(ReferenceValue::isReferenceDefined, "Reference must be defined");
   }
}
