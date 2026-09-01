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
import net.ihe.gazelle.framework.modelvalidator.business.utils.AbstractValidator;
import net.ihe.gazelle.maestro.api.business.property.Property;

/**
 * Validator implementation for {@link Property}, responsible for defining
 */
public class PropertyInReportValidator extends AbstractValidator<Property> {

   /**
    * Default constructor
    */
   public PropertyInReportValidator() {
      super();
   }

   /**
    * Constructs a new PropertyInReportValidator using the specified ValidatorBuilderFactory.
    *
    * @param validatorBuilderFactory the factory to create a validator builder for Property
    */
   public PropertyInReportValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends Property> instantiateValidatorBuilder(
         ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(Property.class);
   }

   @Override
   protected void addRulesAndMembers(ValidatorBuilder<? extends Property> validatorBuilder) {
      validatorBuilder
            .addRule(Property::isNameDefined, "Name must be set and not empty");
   }
}
