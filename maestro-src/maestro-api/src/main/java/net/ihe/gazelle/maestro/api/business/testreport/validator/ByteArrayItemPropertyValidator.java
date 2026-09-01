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

package net.ihe.gazelle.maestro.api.business.testreport.validator;

import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilder;
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayItemProperty;
import net.ihe.gazelle.maestro.api.business.property.Property;

/**
 * Validator implementation for {@link ByteArrayItemProperty}, responsible for defining
 */
public class ByteArrayItemPropertyValidator extends PropertyInReportValidator {

   /**
    * Default constructor
    */
   public ByteArrayItemPropertyValidator() {
      super();
   }

   /**
    * Constructs a new ByteArrayItemPropertyValidator with the specified ValidatorBuilderFactory.
    *
    * @param validatorBuilderFactory the factory used to create ValidatorBuilder instances for validation logic
    */
   public ByteArrayItemPropertyValidator(
         ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends Property> instantiateValidatorBuilder(
         ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(ByteArrayItemProperty.class);
   }

   @Override
   @SuppressWarnings("unchecked")
   protected void addRulesAndMembers(ValidatorBuilder<? extends Property> validatorBuilder) {
      super.addRulesAndMembers(validatorBuilder);
      ((ValidatorBuilder<ByteArrayItemProperty>) validatorBuilder)
            .addRule(ByteArrayItemProperty::isReferenceDefinedIfValueNull,
                  "Byte array property must either have a value or be a reference to an item");
   }
}
