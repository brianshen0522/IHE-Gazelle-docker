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
import net.ihe.gazelle.maestro.api.business.testreport.SystemUnderTest;

/**
 * Validator implementation for {@link SystemUnderTest}, responsible for defining
 */
public class SystemUnderTestValidator extends AbstractValidator<SystemUnderTest> {

   private final EntityIdentificationValidator entityIdentificationValidator;

   /**
    * Default constructor
    */
   public SystemUnderTestValidator() {
      super();
      entityIdentificationValidator = new EntityIdentificationValidator();
   }

   /**
    * Constructs a SystemUnderTestValidator using the provided ValidatorBuilderFactory.
    *
    * @param validatorBuilderFactory The factory to create ValidatorBuilder instances for validating SystemUnderTest objects.
    */
   public SystemUnderTestValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
      entityIdentificationValidator = new EntityIdentificationValidator(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends SystemUnderTest> instantiateValidatorBuilder(ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(SystemUnderTest.class);
   }

   @Override
   @SuppressWarnings("unchecked")
   protected void addRulesAndMembers(ValidatorBuilder<? extends SystemUnderTest> validatorBuilder) {
      validatorBuilder
            .addRule(SystemUnderTest::isSystemIdentificationDefined, "systemIdentification must be set")
            .addMember("systemIdentification", SystemUnderTest::getSystemIdentification, entityIdentificationValidator::getAssembledValidatorBuilder);
   }
}
