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
import net.ihe.gazelle.maestro.api.business.property.PropertyValidator;

/**
 * Validator for the {@link Step} class that ensures specific validation rules are applied.
 */
public class StepValidator extends AbstractValidator<Step> {

   private final PropertyValidator propertyValidator;

   /**
    * Default constructor.
    */
   public StepValidator() {
      super();
      propertyValidator = new PropertyValidator();
   }

   /**
    * Constructs a {@code StepValidator} using the specified {@link ValidatorBuilderFactory}.
    * This constructor initializes the {@code StepValidator} with a {@link PropertyValidator},
    * allowing validation of {@link Step} objects and their associated properties.
    *
    * @param validatorBuilderFactory the factory used to create validator builders
    *                                for {@link Step} validation processes and associated elements
    */
   public StepValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
      propertyValidator = new PropertyValidator(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends Step> instantiateValidatorBuilder(
         ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(Step.class);
   }

   @Override
   @SuppressWarnings("unchecked")
   protected void addRulesAndMembers(ValidatorBuilder<? extends Step> validatorBuilder) {
      validatorBuilder
            .addRule(Step::isNameDefined, "Name must be set and not empty")
            .addRule(Step::isTypeDefined, "Type must be set and not empty")
            .addRule(Step::areOutputMappingsValid,
                  "Both key and value of an OutputMapping must be defined if present")
            .addMember("properties", Step::getProperties, propertyValidator::getAssembledValidatorBuilder);
   }
}
