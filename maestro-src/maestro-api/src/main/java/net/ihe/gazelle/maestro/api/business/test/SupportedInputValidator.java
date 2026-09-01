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

/**
 * Validator implementation for {@link SupportedInput}, responsible for defining
 * @param <T> The concrete type of {@link SupportedInput} to validate
 */
public abstract class SupportedInputValidator<T extends SupportedInput> extends AbstractValidator<T> {

   /**
    * Default constructor.
    */
   protected SupportedInputValidator() {
      super();
   }

   /**
    * Constructor for SupportedInputValidator that takes a ValidatorBuilderFactory.
    * This constructor allows for the customization of the validation process by
    * utilizing a specific factory to create the validator builder.
    *
    * @param validatorBuilderFactory The factory used to construct a ValidatorBuilder
    *                                for the current validation process.
    */
   protected SupportedInputValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
   }

   /**
    * Adds validation rules and members to the provided {@code ValidatorBuilder}.
    * This method defines the validation logic for the SupportedInput validation process,
    * ensuring that certain conditions, such as the presence of a valid ID and label, are met.
    *
    * @param validatorBuilder the builder used to define validation rules and members
    *                         for the {@link SupportedInput} object
    */
   @Override
   protected void addRulesAndMembers(ValidatorBuilder<? extends T> validatorBuilder) {
      validatorBuilder
            .addRule(SupportedInput::isIdDefined, "ID must be set and not empty")
            .addRule(SupportedInput::isLabelDefined, "Label must be set and not empty");
   }

}
