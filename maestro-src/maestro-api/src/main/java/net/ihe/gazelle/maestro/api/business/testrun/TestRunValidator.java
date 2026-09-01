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

package net.ihe.gazelle.maestro.api.business.testrun;

import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilder;
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.framework.modelvalidator.business.utils.AbstractValidator;
import net.ihe.gazelle.maestro.api.business.property.PropertyValidator;
import net.ihe.gazelle.maestro.api.business.test.TestValidator;
import net.ihe.gazelle.maestro.api.business.testreport.validator.SystemUnderTestValidator;
import net.ihe.gazelle.security.business.acl.AccessControlListValidator;

/**
 * Validator responsible for validating {@link TestRun} instances.
 */
public class TestRunValidator extends AbstractValidator<TestRun> {

   private final TestValidator testValidator;
   private final PropertyValidator propertyValidator;
   private final SystemUnderTestValidator systemUnderTestValidator;
   private final AccessControlListValidator accessControlListValidator;

   /**
    * Default constructor
    */
   public TestRunValidator() {
      super();
      this.testValidator = new TestValidator();
      this.propertyValidator = new PropertyValidator();
      this.systemUnderTestValidator = new SystemUnderTestValidator();
      this.accessControlListValidator = new AccessControlListValidator();
   }

   /**
    * Creates a {@code TestRunValidator} using the provided {@link ValidatorBuilderFactory}.
    *
    * @param validatorBuilderFactory the factory used to build validation rules
    *                                and constraints. Must not be {@code null}.
    */
   public TestRunValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
      this.testValidator = new TestValidator(validatorBuilderFactory);
      this.propertyValidator = new PropertyValidator(validatorBuilderFactory);
      this.systemUnderTestValidator = new SystemUnderTestValidator(validatorBuilderFactory);
      this.accessControlListValidator = new AccessControlListValidator(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends TestRun> instantiateValidatorBuilder(
         ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(TestRun.class);
   }

   @Override
   @SuppressWarnings("unchecked")
   protected void addRulesAndMembers(ValidatorBuilder<? extends TestRun> validatorBuilder) {
      validatorBuilder
            .addRule(TestRun::isTestDefined, "Test must be defined")
            .addMember("test", TestRun::getTest, testValidator::getAssembledValidatorBuilder)
            .addMember("inputs", TestRun::getInputs, propertyValidator::getAssembledValidatorBuilder)
            .addMember("systemsUnderTest", TestRun::getSystemsUnderTest,
                  systemUnderTestValidator::getAssembledValidatorBuilder)
            .addMember("accessControlList", TestRun::getAccessControlList,
                  accessControlListValidator::getAssembledValidatorBuilder)
      ;
   }
}
