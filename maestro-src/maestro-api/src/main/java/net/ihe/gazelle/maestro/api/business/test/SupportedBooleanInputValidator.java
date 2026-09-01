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
 * Validator implementation for {@link SupportedBooleanInput}, responsible for defining
 */
public class SupportedBooleanInputValidator extends SupportedInputValidator<SupportedBooleanInput> {

   /**
    * Default constructor
    */
   public SupportedBooleanInputValidator() {
      super();
   }

   /**
    * Constructs a {@code SupportedBooleanInputValidator} with the specified {@code ValidatorBuilderFactory}.
    *
    * @param validatorBuilderFactory the factory to create or extend {@code ValidatorBuilder} instances
    */
   public SupportedBooleanInputValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends SupportedBooleanInput> instantiateValidatorBuilder(
         ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(SupportedBooleanInput.class);
   }

}
