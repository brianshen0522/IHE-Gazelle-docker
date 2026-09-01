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

package net.ihe.gazelle.maestro.validation.step.technical.handler;


import net.ihe.gazelle.evsapi.client.business.ValidationService;
import net.ihe.gazelle.evsapi.client.business.ValidationServiceProfile;
import net.ihe.gazelle.evsapi.client.business.Validator;
import net.ihe.gazelle.evsapi.client.business.request.HandledObject;
import net.ihe.gazelle.evsapi.client.business.response.report.ConstraintPriority;
import net.ihe.gazelle.evsapi.client.business.response.report.ConstraintValidation;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import net.ihe.gazelle.validation.v2.api.business.report.*;
import net.ihe.gazelle.validation.v2.api.business.report.validator.SubjectLocationValidator;

import java.util.List;

class EvsValidationMapper {

   public static final String UNKNOWN_LOCATION_TYPE = "unknown";
   public static final String LINE_COLUMN_LOCATION_TYPE = "line-column";
   public static final String XML_PATH_LOCATION_TYPE = "xml-path";
   public static final String JSON_PATH_LOCATION_TYPE = "json-path";

   net.ihe.gazelle.evsapi.client.business.request.ValidationRequest toEvsRequest(String serviceName,
                                                                                 net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest requestV2) {
      return new net.ihe.gazelle.evsapi.client.business.request.ValidationRequest()
            .setValidationService(
                  new ValidationService().setName(serviceName).setValidator(requestV2.getValidationProfileId())
            )
            .setValidationItems(
                  requestV2.getInputs().stream()
                        .map(input -> new HandledObject().setContent(input.getContent()).setRole(input.getId()))
                        .toList()
            );
   }

   ValidationReport toValidationReport(
         net.ihe.gazelle.evsapi.client.business.response.report.ValidationReport evsReport,
         net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest requestV2) {
      if (evsReport == null) {
         throw new IllegalArgumentException("EVS ValidationReport cannot be null");
      }

      ValidationReportBuilder reportBuilder = new ValidationReportBuilder();

      // Build the main components
      buildValidationOverview(evsReport, reportBuilder);
      addValidationItems(requestV2, reportBuilder);
      addSubReports(evsReport, reportBuilder);

      return reportBuilder.build();
   }

   List<ValidationProfile> toValidationProfiles(List<ValidationServiceProfile> validationServiceProfiles) {
      return validationServiceProfiles.stream()
            .map(this::toValidationProfile)
            .toList();
   }

   ValidationProfile toValidationProfile(ValidationServiceProfile validationServiceProfile) {
      Validator validator = validationServiceProfile.getValidator();
      return new ValidationProfile()
            .setProfileID(validator.getKeyword())
            .setProfileName(validator.getName())
            .setDomain(validator.getDomain());
   }

   /**
    * Builds validation overview section including disclaimer, validation method, and metadata
    */
   private void buildValidationOverview(
         net.ihe.gazelle.evsapi.client.business.response.report.ValidationReport evsReport,
         ValidationReportBuilder reportBuilder) {
      if (evsReport.getValidationOverview() == null) {
         return;
      }
      ValidationReportBuilder builder = reportBuilder
            .setDisclaimer(evsReport.getValidationOverview().getDisclaimer())
            .setValidationMethod(createValidationMethod(evsReport.getValidationOverview()));

      addMetadataIfPresent(evsReport.getValidationOverview(), builder);
   }

   /**
    * Creates validation method from EVS validation overview
    */
   private ValidationMethodBuilder createValidationMethod(
         net.ihe.gazelle.evsapi.client.business.response.report.ValidationOverview overview) {
      return new ValidationMethodBuilder()
            .setValidationProfileID(overview.getValidatorID())
            .setValidationProfileVersion(overview.getValidatorVersion() != null && !overview.getValidatorVersion()
                  .isEmpty() ? overview.getValidatorVersion() : "Unknown")
            .setValidationServiceName(overview.getValidationServiceName())
            .setValidationServiceVersion(
                  overview.getValidationServiceVersion() != null && !overview.getValidationServiceVersion()
                        .isEmpty() ? overview.getValidationServiceVersion() : "Unknown");
   }

   /**
    * Adds metadata to the report builder if metadata exists and is not empty
    */
   private void addMetadataIfPresent(net.ihe.gazelle.evsapi.client.business.response.report.ValidationOverview overview,
                                     ValidationReportBuilder builder) {
      if (overview.getAdditionalMetadata() != null && !overview.getAdditionalMetadata().isEmpty()) {
         builder.addAdditionalMetadata(
               overview.getAdditionalMetadata().stream()
                     .map(metadata -> new MetadataBuilder()
                           .setName(metadata.getName())
                           .setValue(metadata.getValue()))
                     .toList());
      }
   }

   /**
    * Adds validation items from the validation request if present
    */
   private void addValidationItems(net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest requestV2,
                                   ValidationReportBuilder reportBuilder) {
      if (!requestV2.getInputs().isEmpty()) {
         reportBuilder.addInputs(
               requestV2.getInputs().stream().map(
                     input -> new InputInReportBuilder()
                           .setId(input.getId())
                           .setItemId(input.getItemId())
                           .setContent(input.getContent())
                           .setLocation(input.getLocation())
               ).toList()
         );
      }
   }


   /**
    * Adds sub-reports to the validation report if present
    */
   private void addSubReports(net.ihe.gazelle.evsapi.client.business.response.report.ValidationReport evsReport,
                              ValidationReportBuilder reportBuilder) {
      if (evsReport.getSubReport() == null || evsReport.getSubReport().isEmpty()) {
         return;
      }

      reportBuilder.addSubReports(
            evsReport.getSubReport().stream()
                  .map(this::createValidationSubReport)
                  .toList()
      );
   }

   /**
    * Creates a single validation sub-report from EVS sub-report
    */
   private ValidationSubReportBuilder createValidationSubReport(
         net.ihe.gazelle.evsapi.client.business.response.report.ValidationSubReport subReport) {
      ValidationSubReportBuilder subReportBuilder = new ValidationSubReportBuilder()
            .setName(subReport != null ? subReport.getName() : null);

      addAssertionReports(subReport, subReportBuilder);
      return subReportBuilder;
   }

   /**
    * Adds assertion reports to the sub-report if constraints exist
    */
   private void addAssertionReports(
         net.ihe.gazelle.evsapi.client.business.response.report.ValidationSubReport subReport,
         ValidationSubReportBuilder subReportBuilder) {
      if (!hasConstraints(subReport)) {
         return;
      }

      subReport.getConstraints().forEach(constraint -> {
         if (constraint != null) {
            AssertionReportBuilder assertionBuilder = createAssertionReport(constraint);
            addUnexpectedErrors(constraint, assertionBuilder);
            subReportBuilder.addAssertionReport(assertionBuilder);
         }
      });
   }

   /**
    * Checks if sub-report has valid constraints
    */
   private boolean hasConstraints(
         net.ihe.gazelle.evsapi.client.business.response.report.ValidationSubReport subReport) {
      return subReport != null &&
             subReport.getConstraints() != null &&
             !subReport.getConstraints().isEmpty();
   }

   /**
    * Creates assertion report from EVS constraint
    */
   private AssertionReportBuilder createAssertionReport(
         net.ihe.gazelle.evsapi.client.business.response.report.ConstraintValidation constraint) {

      AssertionReportBuilder assertionReportBuilder = new AssertionReportBuilder()
            .setAssertionID(constraint.getConstraintID())
            .setAssertionType(constraint.getConstraintType())
            .setDescription(constraint.getConstraintDescription())
            .setSubjectValue(constraint.getValueInValidatedObject())
            .setRequirementIDs(getRequirementIds(constraint))
            .setPriority(mapPriority(constraint.getPriority()))
            .setResult(mapTestResult(constraint.getTestResult()));

      SubjectLocationBuilder subjectLocationBuilder = getValidatedSubjectLocation(
            constraint.getLocationInValidatedObject());
      if (subjectLocationBuilder != null) {
         assertionReportBuilder.addSubjectLocation(subjectLocationBuilder);
      }
      return assertionReportBuilder;
   }

   /**
    * Gets validated location, fixing invalid formats if necessary
    */
   private SubjectLocationBuilder getValidatedSubjectLocation(String locationValue) {
      if (locationValue == null || locationValue.isEmpty()) {
         return null;
      }
      SubjectLocationBuilder subjectLocationBuilder = new SubjectLocationBuilder().setValue(locationValue)
            .setInputId(null) //TODO find the associated input ID if possible
            .setType(estimateTypeFromLocationValue(locationValue));
      if (isSubjectLocationValid(subjectLocationBuilder)) {
         return subjectLocationBuilder;
      } else {
         return null;
      }
   }

   /**
    * Estimates location type from the location value using simple heuristics
    *
    * @param location the location value
    *
    * @return the estimated location type
    */
   private String estimateTypeFromLocationValue(String location) {
      if (location.startsWith("line")) {
         return LINE_COLUMN_LOCATION_TYPE;
      } else if (location.contains("/")) {
         return XML_PATH_LOCATION_TYPE;
      } else if (location.contains("$")) {
         return JSON_PATH_LOCATION_TYPE;
      } else {
         return UNKNOWN_LOCATION_TYPE;
      }
   }

   /**
    * Gets requirement IDs if they exist and are not empty
    */
   private List<String> getRequirementIds(ConstraintValidation constraint) {
      return constraint.getAssertionIDs() != null && !constraint.getAssertionIDs().isEmpty()
            ? constraint.getAssertionIDs()
            : null;
   }

   /**
    * Adds unexpected errors to assertion report if they exist
    */
   private void addUnexpectedErrors(ConstraintValidation constraint, AssertionReportBuilder assertionBuilder) {
      if (constraint.getUnexpectedErrors() == null || constraint.getUnexpectedErrors().isEmpty()) {
         return;
      }

      constraint.getUnexpectedErrors().forEach(error -> {
         if (error != null) {
            assertionBuilder.addUnexpectedError(
                  new UnexpectedErrorBuilder()
                        .setName(error.getName())
                        .setMessage(error.getMessage())
            );
         }
      });
   }

   private RequirementPriority mapPriority(ConstraintPriority constraintPriority) {
      if (constraintPriority == null) {
         return RequirementPriority.MANDATORY;
      }

      return switch (constraintPriority) {
         case MANDATORY -> RequirementPriority.MANDATORY;
         case RECOMMENDED -> RequirementPriority.RECOMMENDED;
         case PERMITTED -> RequirementPriority.PERMITTED;
      };
   }

   private ValidationTestResult mapTestResult(
         net.ihe.gazelle.evsapi.client.business.response.report.ValidationTestResult testResult) {
      if (testResult == null) {
         return ValidationTestResult.UNDEFINED;
      }

      return switch (testResult) {
         case PASSED -> ValidationTestResult.PASSED;
         case FAILED -> ValidationTestResult.FAILED;
         case UNDEFINED -> ValidationTestResult.UNDEFINED;
      };
   }

   private boolean isSubjectLocationValid(SubjectLocationBuilder subjectLocation) {
      if (subjectLocation == null) {
         return true;
      }
      SubjectLocation subjectLocation1 = subjectLocation.build();
      SubjectLocationValidator subjectLocationValidator = new SubjectLocationValidator();
      return subjectLocationValidator.validate(subjectLocation1).isValid();
   }
}
