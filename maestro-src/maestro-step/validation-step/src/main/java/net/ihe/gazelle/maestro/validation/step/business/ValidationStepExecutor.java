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

package net.ihe.gazelle.maestro.validation.step.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.property.PropertyBindingPayload;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.testreport.StepResult;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReportBuilder;
import net.ihe.gazelle.maestro.spi.business.StepExecutor;
import net.ihe.gazelle.maestro.spi.business.StepRun;
import net.ihe.gazelle.validation.v2.api.business.UnknownValidationProfileException;
import net.ihe.gazelle.validation.v2.api.business.profile.SupportedInput;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationTestResult;
import net.ihe.gazelle.validation.v2.api.business.request.InputInRequestBuilder;
import net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest;
import net.ihe.gazelle.validation.v2.api.business.request.ValidationRequestBuilder;

import java.util.List;
import java.util.ArrayList;

/**
 * A StepExecutor is an object used to run ValidationStep {@link ValidationStepDefinition}
 * <br>
 * Translate ValidationStep into ValidationRequest
 * <br>
 * Use ValidationHandler to validate content which return a ValidationReport {@link ValidationHandler}
 * <br>
 * Assert ValidationReport to extract result and build a ValidationReportBuilder
 * {@link net.ihe.gazelle.validation.v2.api.business.report.ValidationReportBuilder}
 * <br>
 */
public class ValidationStepExecutor implements StepExecutor {

   private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
   private static final String VALIDATION_INPUT = "validationInput";
   private final ReportSerializer serializer;
   private final ValidationHandler handler;

   /**
    * Constructor for ValidationStepExecutor.
    * @param validationHandler the validation handler to use for validation operations
    * @param serializer the report serializer to use for converting reports to byte arrays
    */
   public ValidationStepExecutor(ValidationHandler validationHandler, ReportSerializer serializer) {
      this.handler = validationHandler;
      this.serializer = serializer;
   }

   @Override
   public StepRunReport execute(StepRun stepRun) {
      String validationService = stepRun.getPropertyValue(ValidationStepDefinition.VALIDATION_SERVICE);
      String validationProfile = stepRun.getPropertyValue(ValidationStepDefinition.VALIDATION_PROFILE);
      List<PropertyBindingPayload.Binding> propertyBindings = new ArrayList<>();

      ValidationRequest validationRequest = buildValidationRequest(
            stepRun,
            validationProfile,
            getSupportedInputs(validationService, validationProfile),
            propertyBindings
      );
      ValidationReport validationReport = handler.validate(validationRequest);


      ByteArrayProperty reportOutput =
            new ByteArrayProperty(ValidationStepDefinition.REPORT, serializer.toByteArray(validationReport))
            .setFileName("validation-report.json")
            .setMimeType("application/json");

      StringProperty propertyBindingOutput = new StringProperty(
            PropertyBindingPayload.PROPERTY_NAME,
            serializeBindings(new PropertyBindingPayload()
                  .setStepType(ValidationStepDefinition.TYPE)
                  .setBindings(propertyBindings))
      );

      StepRunReportBuilder stepRunReportBuilder = new StepRunReportBuilder()
            .setStepName(stepRun.getName())
            .setType(ValidationStepDefinition.TYPE)
            .setResult(mapResult(validationReport.getOverallResult()))
            .setOutputs(List.of(reportOutput, propertyBindingOutput));
      return stepRunReportBuilder.build();
   }

   private ValidationRequest buildValidationRequest(StepRun stepRun, String validationProfile,
                                                    final List<SupportedInput> supportedInputs,
                                                    List<PropertyBindingPayload.Binding> propertyBindings) {
      ValidationRequestBuilder requestBuilder = new ValidationRequestBuilder()
            .setValidationProfileId(validationProfile);

      if(supportedInputs.isEmpty()) {
         // If no supported inputs defined for this profile, then use the fallback CONTENT_TO_VALIDATE input
         requestBuilder.addInput(
               getInputInRequest(ValidationStepDefinition.CONTENT_TO_VALIDATE, stepRun)
         );
         propertyBindings.add(toPropertyBinding(stepRun, stepRun.getProperty(ValidationStepDefinition.CONTENT_TO_VALIDATE),
               ValidationStepDefinition.CONTENT_TO_VALIDATE, VALIDATION_INPUT));
      } else {
         for (SupportedInput supportedInput : supportedInputs) {
            if (supportedInput.isRequired()) {
               requestBuilder.addInput(
                     getInputInRequest(supportedInput.getId(), stepRun)
               );
               propertyBindings.add(toPropertyBinding(stepRun, stepRun.getProperty(supportedInput.getId()),
                     supportedInput.getId(), VALIDATION_INPUT));
            } else {
               if (stepRun.hasProperty(supportedInput.getId())) {
                  requestBuilder.addInput(
                        getInputInRequest(supportedInput.getId(), stepRun)
                  );
                  propertyBindings.add(toPropertyBinding(stepRun, stepRun.getProperty(supportedInput.getId()),
                        supportedInput.getId(), VALIDATION_INPUT));
               }
            }
         }
      }
      return requestBuilder.build();
   }

   private static InputInRequestBuilder getInputInRequest(String contentToValidate, StepRun stepRun) {
      return new InputInRequestBuilder()
            .setId(contentToValidate)
            .setContent((byte[]) stepRun.getPropertyValue(contentToValidate));
   }

   private static PropertyBindingPayload.Binding toPropertyBinding(StepRun stepRun,
                                                                   Property property,
                                                                   String targetId,
                                                                   String targetKind) {
      PropertyBindingPayload.Binding binding = new PropertyBindingPayload.Binding()
            .setTargetKind(targetKind)
            .setTargetId(targetId)
            .setReferenceName(resolveReferenceName(stepRun, property))
            .setPropertyName(property.getName());
      if (property instanceof ByteArrayProperty byteArrayProperty) {
         binding.setFileName(byteArrayProperty.getFileName());
         binding.setMimeType(byteArrayProperty.getMimeType());
      }
      return binding;
   }

   private static String resolveReferenceName(StepRun stepRun, Property property) {
      String referenceName = stepRun.getPropertyReferenceName(property.getName());
      return referenceName != null ? referenceName : property.getReferenceName();
   }

   private static String serializeBindings(PropertyBindingPayload payload) {
      try {
         return OBJECT_MAPPER.writeValueAsString(payload);
      } catch (JsonProcessingException e) {
         throw new IllegalStateException("Failed to serialize property bindings", e);
      }
   }

   private List<SupportedInput> getSupportedInputs(String validationService, String validationProfile) {
      List<ValidationProfile> profiles = handler.getValidationProfiles();
      return profiles.stream()
            .filter(p -> validationProfile.equals(p.getProfileID()))
            .findFirst()
            .orElseThrow(() -> new UnknownValidationProfileException(
                  "Validation Profile " + validationProfile + " not found on Validation Service " + validationService
            ))
            .getSupportedInputs();
   }

   /**
    * Compute the validation step result from the received validation report from external validation tool
    *
    * @param validationResult     The received validation report
    */
   private StepResult mapResult(ValidationTestResult validationResult) {
      return switch (validationResult) {
         case PASSED -> StepResult.PASSED;
         case FAILED -> StepResult.FAILED;
         default -> StepResult.UNDEFINED;
      };
   }

}
