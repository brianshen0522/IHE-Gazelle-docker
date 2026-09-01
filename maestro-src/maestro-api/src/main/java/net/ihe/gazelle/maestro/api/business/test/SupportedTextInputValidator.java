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

/**
 * Validator implementation for {@link SupportedTextInput}, responsible for defining
 */
public class SupportedTextInputValidator extends SupportedInputValidator<SupportedTextInput> {

   /**
    * Default constructor.
    */
   public SupportedTextInputValidator() {
      super();
   }

   /**
    * Constructs a new instance of SupportedTextInputValidator using the provided ValidatorBuilderFactory.
    *
    * @param validatorBuilderFactory The factory used to construct a ValidatorBuilder
    *                                for the validation process.
    */
   public SupportedTextInputValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends SupportedTextInput> instantiateValidatorBuilder(
         ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(SupportedTextInput.class);
   }

   @Override
   protected void addRulesAndMembers(ValidatorBuilder<? extends SupportedTextInput> validatorBuilder) {
      super.addRulesAndMembers(validatorBuilder);
      validatorBuilder
            .addRule(SupportedTextInput::areAllPossibleValuesDefined,
                  "possibleValues must not contain null or empty values");
   }
}
