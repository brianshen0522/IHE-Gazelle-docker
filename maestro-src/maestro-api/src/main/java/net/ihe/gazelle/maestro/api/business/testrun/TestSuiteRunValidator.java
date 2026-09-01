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
import net.ihe.gazelle.maestro.api.business.test.TestSuiteValidator;
import net.ihe.gazelle.maestro.api.business.test.TestValidator;
import net.ihe.gazelle.maestro.api.business.testreport.validator.SystemUnderTestValidator;
import net.ihe.gazelle.security.business.acl.AccessControlListValidator;

/**
 * Validator responsible for validating {@link TestSuiteRun} instances.
 */
public class TestSuiteRunValidator extends AbstractValidator<TestSuiteRun> {

   private final TestValidator testValidator;
   private final TestSuiteValidator testSuiteValidator;
   private final PropertyValidator propertyValidator;
   private final AccessControlListValidator accessControlListValidator;
   private final SystemUnderTestValidator systemUnderTestValidator;

   /**
    * Default constructor
    */
   public TestSuiteRunValidator() {
      super();
      this.testValidator = new TestValidator();
      this.testSuiteValidator = new TestSuiteValidator();
      this.propertyValidator = new PropertyValidator();
      this.accessControlListValidator = new AccessControlListValidator();
      this.systemUnderTestValidator = new SystemUnderTestValidator();
   }

   /**
    * Creates a {@code TestSuiteRunValidator} using the provided {@link ValidatorBuilderFactory}.
    *
    * @param validatorBuilderFactory the factory used to build validation rules
    */
   public TestSuiteRunValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
      this.testValidator = new TestValidator(validatorBuilderFactory);
      this.testSuiteValidator = new TestSuiteValidator(validatorBuilderFactory);
      this.propertyValidator = new PropertyValidator(validatorBuilderFactory);
      this.accessControlListValidator = new AccessControlListValidator(validatorBuilderFactory);
      this.systemUnderTestValidator = new SystemUnderTestValidator(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends TestSuiteRun> instantiateValidatorBuilder(
         ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(TestSuiteRun.class);
   }

   @Override
   @SuppressWarnings("unchecked")
   protected void addRulesAndMembers(ValidatorBuilder<? extends TestSuiteRun> validatorBuilder) {
      validatorBuilder
            .addRule(TestSuiteRun::isTestSuiteDefined, "Test Suite must be set")
            .addRule(TestSuiteRun::areAllReferencedTestsAttached, "All Tests referenced in the Test Suite must be attached to the Test Suite Run")
            .addMember("testSuite", TestSuiteRun::getTestSuite, testSuiteValidator::getAssembledValidatorBuilder)
            .addMember("tests", TestSuiteRun::getTests, testValidator::getAssembledValidatorBuilder)
            .addMember("inputs", TestSuiteRun::getInputs,
                 propertyValidator::getAssembledValidatorBuilder)
            .addMember("accessControlList", TestSuiteRun::getAccessControlList,
                  accessControlListValidator::getAssembledValidatorBuilder)
            .addMember("systemUnderTest", TestSuiteRun::getSystemsUnderTest,
                  systemUnderTestValidator::getAssembledValidatorBuilder);
   }

}
