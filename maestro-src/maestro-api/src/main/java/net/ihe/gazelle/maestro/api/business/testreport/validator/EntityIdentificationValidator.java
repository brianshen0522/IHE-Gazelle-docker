/*
 * Copyright 2025-2026 IHE International.
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
import net.ihe.gazelle.maestro.api.business.testreport.EntityIdentification;

/**
 * Validator implementation for {@link EntityIdentification}, responsible for defining
 */
public class EntityIdentificationValidator extends AbstractValidator<EntityIdentification> {

   /**
    * Default constructor
    */
   public EntityIdentificationValidator() {
      super();
   }

   /**
    * Constructs an EntityIdentificationValidator using the provided ValidatorBuilderFactory.
    *
    * @param builderFactory The factory to create ValidatorBuilder instances for validating EntityIdentification objects.
    */
   public EntityIdentificationValidator(ValidatorBuilderFactory builderFactory) {
      super(builderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends EntityIdentification> instantiateValidatorBuilder(ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(EntityIdentification.class);
   }

   @Override
   protected void addRulesAndMembers(ValidatorBuilder<? extends EntityIdentification> validatorBuilder) {
      validatorBuilder
            .addRule(EntityIdentification::isVersionDefinedIfPresent, "Version must be set if present")
            .addRule(EntityIdentification::isNameDefined, "Name must be set and not empty");
   }
}
