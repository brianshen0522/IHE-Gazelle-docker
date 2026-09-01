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
 * Validator for the {@link Property} class that ensures specific validation rules are applied.
 */
public class PropertyValidator extends AbstractValidator<Property> {

   private final ReferenceValueValidator referenceValueValidator;
   private final DirectValueValidator directValueValidator;

   /**
    * Default constructor.
    */
   public PropertyValidator() {
      super();
      referenceValueValidator = new ReferenceValueValidator();
      directValueValidator = new DirectValueValidator();
   }

   /**
    * Constructs a new instance of {@code PropertyValidator} with the specified
    * {@link ValidatorBuilderFactory}. This constructor initializes the
    * {@code PropertyValidator} with validator builders for validating reference values
    * and direct values associated with properties.
    *
    * @param validatorBuilderFactory the factory used to create validator builders
    *                                for {@code Property}, {@code ReferenceValue},
    *                                and {@code DirectValue} validation processes
    */
   public PropertyValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
      referenceValueValidator = new ReferenceValueValidator(validatorBuilderFactory);
      directValueValidator = new DirectValueValidator(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<Property> instantiateValidatorBuilder(ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(Property.class);
   }


   @Override
   @SuppressWarnings("unchecked")
   protected void addRulesAndMembers(ValidatorBuilder<? extends Property> validatorBuilder) {
      validatorBuilder
            .addRule(Property::isNameDefined, "Name must be set and not empty")
            .addMember("valueHolder", Property::getValueHolder,
                  referenceValueValidator::getAssembledValidatorBuilder,
                  directValueValidator::getAssembledValidatorBuilder);
   }
}
