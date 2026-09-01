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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class SupportedInputValidatorsTest {

   @Test
   void shouldReturnExpectedSpecializedValidators() {
      List<SupportedInputValidator<?>> validators = SupportedInputValidators.getAllSupportedInputValidators();

      assertEquals(4, validators.size());
      assertInstanceOf(SupportedTextInputValidator.class, validators.get(0));
      assertInstanceOf(SupportedFileInputValidator.class, validators.get(1));
      assertInstanceOf(SupportedBooleanInputValidator.class, validators.get(2));
      assertInstanceOf(SupportedValueSetInputValidator.class, validators.get(3));
   }

   @Test
   void shouldCreateAssembledSuppliersForAllValidators() {
      List<SupportedInputValidator<?>> validators = SupportedInputValidators.getAllSupportedInputValidators();

      Supplier<ValidatorBuilder<?>>[] suppliers = SupportedInputValidators.getAssembledSuppliers(validators);

      assertEquals(validators.size(), suppliers.length);
      for (Supplier<ValidatorBuilder<?>> supplier : suppliers) {
         assertNotNull(supplier.get());
      }
   }
}
