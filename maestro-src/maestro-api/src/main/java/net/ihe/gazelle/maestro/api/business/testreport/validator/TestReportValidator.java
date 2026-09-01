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

import net.ihe.gazelle.errorhandling.business.UnexpectedErrorValidator;
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilder;
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.framework.modelvalidator.business.utils.AbstractValidator;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;

/**
 * Validator implementation for {@link TestReport}, responsible for defining
 */
public class TestReportValidator extends AbstractValidator<TestReport> {

   private final TestServiceValidator testServiceValidator;
   private final SystemUnderTestValidator systemUnderTestValidator;
   private final TestCountersValidator testCountersValidator;
   private final TestReportValidator subReportValidator;
   private final TestRunReportValidator testRunReportValidator;
   private final UnexpectedErrorValidator unexpectedErrorValidator;

   /**
    * Default constructor
    */
   public TestReportValidator() {
      super();
      testServiceValidator = new TestServiceValidator();
      systemUnderTestValidator = new SystemUnderTestValidator();
      testCountersValidator = new TestCountersValidator();
      subReportValidator = new TestReportValidator(this);
      testRunReportValidator = new TestRunReportValidator();
      unexpectedErrorValidator = new UnexpectedErrorValidator();
   }

   /**
    * Constructs a new TestReportValidator using the provided TestReportValidator instance as the sub-validator.
    *
    * @param testReportValidator the instance of TestReportValidator to be used as a sub-validator
    */
   public TestReportValidator(TestReportValidator testReportValidator) {
      super();
      testServiceValidator = new TestServiceValidator();
      systemUnderTestValidator = new SystemUnderTestValidator();
      testCountersValidator = new TestCountersValidator();
      subReportValidator = testReportValidator; // NOSONAR - recursive validator linkage is intentional
      testRunReportValidator = new TestRunReportValidator();
      unexpectedErrorValidator = new UnexpectedErrorValidator();
   }

   /**
    * Constructs a new TestReportValidator and initializes the dependent validators.
    *
    * @param validatorBuilderFactory the factory used to create validator builders.
    */
   public TestReportValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
      testServiceValidator = new TestServiceValidator(validatorBuilderFactory);
      systemUnderTestValidator = new SystemUnderTestValidator(validatorBuilderFactory);
      testCountersValidator = new TestCountersValidator(validatorBuilderFactory);
      subReportValidator = new TestReportValidator(validatorBuilderFactory, this);
      testRunReportValidator = new TestRunReportValidator(validatorBuilderFactory);
      unexpectedErrorValidator = new UnexpectedErrorValidator(validatorBuilderFactory);
   }

   /**
    * Constructs a new TestReportValidator and initializes its dependent validators along with a sub-validator.
    *
    * @param builderFactory The factory used to create validator builders.
    * @param testReportValidator The instance of TestReportValidator to be used as a sub-validator.
    */
   public TestReportValidator(ValidatorBuilderFactory builderFactory, TestReportValidator testReportValidator) {
      super(builderFactory);
      testServiceValidator = new TestServiceValidator(builderFactory);
      systemUnderTestValidator = new SystemUnderTestValidator(builderFactory);
      testCountersValidator = new TestCountersValidator(builderFactory);
      subReportValidator = testReportValidator; // NOSONAR - recursive validator linkage is intentional
      testRunReportValidator = new TestRunReportValidator(builderFactory);
      unexpectedErrorValidator = new UnexpectedErrorValidator(builderFactory);
   }

   @Override
   protected ValidatorBuilder<? extends TestReport> instantiateValidatorBuilder(ValidatorBuilderFactory validatorBuilderFactory) {
      return validatorBuilderFactory.getBuilder(TestReport.class);
   }

   @Override
   @SuppressWarnings("unchecked")
   protected void addRulesAndMembers(ValidatorBuilder<? extends TestReport> validatorBuilder) {
      validatorBuilder
            .addRule(TestReport::isUuidDefined, "Uuid must be set and not empty")
            .addRule(TestReport::isDateTimeNotInTheFuture, "DateTime must be set and not in the future")
            .addRule(TestReport::resultMatchesComputedResult, "Result must be set and reflect the global result of the test testreport")
            .addRule(TestReport::isTestSuiteNameValid, "TestSuite name must be not empty if present")
            .addRule(TestReport::testCountersMatchesComputedCounters, "TestCounters must be set and reflect the number of contained testRuns")
            .addRule(TestReport::isNoteDefinedIfPresent, "Note must be not empty if present")
            .addRule(TestReport::isUrlToTestSuiteResultDefinedIfPresent, "UrlToTestResult must be not empty if present")
            .addRule(TestReport::hasAtLeastOneResult, "A testReport must have at least on result")
            .addMember("testService", TestReport::getTestService, testServiceValidator::getAssembledValidatorBuilder)
            .addMember("systemsUnderTest", TestReport::getSystemsUnderTest, systemUnderTestValidator::getAssembledValidatorBuilder)
            .addMember("testCounters", TestReport::getTestCounters, testCountersValidator::getAssembledValidatorBuilder)
            .addMember("subReports", TestReport::getSubReports, subReportValidator::getAssembledValidatorBuilder)
            .addMember("testRuns", TestReport::getTestRunReports, testRunReportValidator::getAssembledValidatorBuilder)
            .addMember("unexpectedErrors", TestReport::getUnexpectedErrors, unexpectedErrorValidator::getAssembledValidatorBuilder);

   }
}
