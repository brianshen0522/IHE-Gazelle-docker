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

import java.util.List;

/**
 * Validator for the {@link TestSuite} class that ensures specific validation rules are applied.
 */
public class TestSuiteValidator extends AbstractValidator<TestSuite> {

   private final TestReferenceValidator testReferenceValidator;
   private final List<SupportedInputValidator<?>> supportedInputValidators;

   /**
    * Default constructor.
    */
   public TestSuiteValidator() {
      super();
      this.testReferenceValidator = new TestReferenceValidator();
      this.supportedInputValidators = SupportedInputValidators.getAllSupportedInputValidators();
   }

   /**
    * Constructs a new {@code TestSuiteValidator} with the given {@code validatorBuilderFactory}.
    *
    * @param validatorBuilderFactory the factory used to create validator builders for the
    *                                {@link TestSuite} and its related validation components
    */
   public TestSuiteValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
      this.testReferenceValidator = new TestReferenceValidator(validatorBuilderFactory);
      this.supportedInputValidators = SupportedInputValidators.getAllSupportedInputValidators(validatorBuilderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends TestSuite> instantiateValidatorBuilder(ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(TestSuite.class);
   }

   @Override
   @SuppressWarnings("unchecked")
   protected void addRulesAndMembers(ValidatorBuilder<? extends TestSuite> validatorBuilder) {
      validatorBuilder
            .addRule(TestSuite::isIdDefined, "Test Suite ID must be set and not empty")
            .addRule(TestSuite::isNameDefined, "Test Suite name must be set and not empty")
            .addRule(TestSuite::hasAtLeastOneTest, "There must be at least one Test in the Test Suite")
            .addMember("testReferences", TestSuite::getTestReferences,
                  testReferenceValidator::getAssembledValidatorBuilder)
            .addMember("supportedInputs", TestSuite::getSupportedInputs,
                  SupportedInputValidators.getAssembledSuppliers(supportedInputValidators));
   }
}
