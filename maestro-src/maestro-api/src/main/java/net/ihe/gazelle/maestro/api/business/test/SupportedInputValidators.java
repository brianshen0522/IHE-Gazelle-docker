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

import java.util.List;
import java.util.function.Supplier;

final class SupportedInputValidators {

   private SupportedInputValidators() {
      // utility class
   }

   @SuppressWarnings("java:S1452")
   static List<SupportedInputValidator<?>> getAllSupportedInputValidators() {
      return List.of(
            new SupportedTextInputValidator(),
            new SupportedFileInputValidator(),
            new SupportedBooleanInputValidator(),
            new SupportedValueSetInputValidator()
      );
   }

   @SuppressWarnings("java:S1452")
   static List<SupportedInputValidator<?>> getAllSupportedInputValidators(ValidatorBuilderFactory builderFactory) {
      return List.of(
            new SupportedTextInputValidator(builderFactory),
            new SupportedFileInputValidator(builderFactory),
            new SupportedBooleanInputValidator(builderFactory),
            new SupportedValueSetInputValidator(builderFactory)
      );
   }

   @SuppressWarnings({"java:S1452", "unchecked"})
   static Supplier<ValidatorBuilder<?>>[] getAssembledSuppliers(final List<SupportedInputValidator<?>> inputValidators) {
      return inputValidators.stream()
            .map(SupportedInputValidators::getAssemblerSupplier)
            .toArray(Supplier[]::new);
   }

   private static Supplier<ValidatorBuilder<?>> getAssemblerSupplier(final SupportedInputValidator<?> validator) {
      return validator::getAssembledValidatorBuilder;
   }
}
