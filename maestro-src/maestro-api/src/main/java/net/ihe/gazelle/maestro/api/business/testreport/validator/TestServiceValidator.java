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
import net.ihe.gazelle.maestro.api.business.testreport.TestService;

/**
 * Validator implementation for {@link TestService}, responsible for defining
 */
public class TestServiceValidator extends AbstractValidator<TestService> {

   private final EntityIdentificationValidator entityIdentificationValidator;

   /**
    * Default constructor
    */
   public TestServiceValidator() {
      super();
      entityIdentificationValidator = new EntityIdentificationValidator();
   }

   /**
    * Constructs a TestServiceValidator using the provided ValidatorBuilderFactory.
    *
    * @param validatorBuilderFactory The factory to create ValidatorBuilder instances for validating TestService objects.
    */
   public TestServiceValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
      entityIdentificationValidator = new EntityIdentificationValidator(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends TestService> instantiateValidatorBuilder(ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(TestService.class);
   }

   @Override
   @SuppressWarnings("unchecked")
   protected void addRulesAndMembers(ValidatorBuilder<? extends TestService> validatorBuilder) {
      validatorBuilder
            .addRule(TestService::isServiceIdentificationDefined, "serviceIdentification must be set")
            .addRule(TestService::isServiceVersionDefined, "version in serviceIdentification must be set and not empty")
            .addRule(TestService::isDisclaimerDefined, "disclaimer must be set and not empty")
            .addMember("serviceIdentification", TestService::getServiceIdentification, entityIdentificationValidator::getAssembledValidatorBuilder);
   }
}
