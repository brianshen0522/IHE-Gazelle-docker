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
 * Validator implementation for {@link SupportedFileInput}, responsible for defining
 */
public class SupportedFileInputValidator extends SupportedInputValidator<SupportedFileInput> {

   /**
    * Default constructor
    */
   public SupportedFileInputValidator() {
      super();
   }

   /**
    * Constructs a {@code SupportedFileInputValidator} with a specified {@code ValidatorBuilderFactory}.
    *
    * @param validatorBuilderFactory The factory used to create validator builders for the {@code SupportedFileInput}.
    */
   public SupportedFileInputValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends SupportedFileInput> instantiateValidatorBuilder(
         ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(SupportedFileInput.class);
   }

}
