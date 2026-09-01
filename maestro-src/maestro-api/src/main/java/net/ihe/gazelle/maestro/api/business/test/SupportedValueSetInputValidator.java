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
 * Validator implementation for {@link SupportedValueSetInput}, responsible for defining
 */
public class SupportedValueSetInputValidator extends SupportedInputValidator<SupportedValueSetInput> {

   /**
    * Default constructor.
    */
   public SupportedValueSetInputValidator() {
      super();
   }

   /**
    * Constructs a new SupportedValueSetInputValidator with the specified ValidatorBuilderFactory.
    *
    * @param validatorBuilderFactory The factory used to construct a ValidatorBuilder for the current
    *                                validation process.
    */
   public SupportedValueSetInputValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends SupportedValueSetInput> instantiateValidatorBuilder(
         ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(SupportedValueSetInput.class);
   }

   @Override
   protected void addRulesAndMembers(ValidatorBuilder<? extends SupportedValueSetInput> validatorBuilder) {
      super.addRulesAndMembers(validatorBuilder);
      validatorBuilder
            .addRule(SupportedValueSetInput::isValueSetIdDefined,
                  "valueSetId must be set and not empty");
   }
}
