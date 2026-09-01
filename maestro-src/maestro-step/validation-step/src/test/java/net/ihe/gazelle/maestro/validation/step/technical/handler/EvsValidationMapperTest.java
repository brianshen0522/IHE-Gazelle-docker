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

import net.ihe.gazelle.evsapi.client.business.ValidationServiceProfile;
import net.ihe.gazelle.evsapi.client.business.Validator;
import net.ihe.gazelle.evsapi.client.business.response.report.*;
import net.ihe.gazelle.maestro.validation.step.business.ValidationStepDefinition;
import net.ihe.gazelle.validation.v2.api.business.Input;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import net.ihe.gazelle.validation.v2.api.business.report.RequirementPriority;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationTestResult;
import net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EvsValidationMapperTest {

   private final EvsValidationMapper mapper = new EvsValidationMapper();

   @Test
   void shouldMapV2RequestToEvsRequest() {
      ValidationRequest request = new ValidationRequest()
            .setValidationProfileId("PROFILE-ID")
            .addInput(new Input()
                  .setId(ValidationStepDefinition.CONTENT_TO_VALIDATE)
                  .setContent("payload".getBytes())
                  .setItemId("item-1"));

      net.ihe.gazelle.evsapi.client.business.request.ValidationRequest evsRequest =
            mapper.toEvsRequest("MATCHBOX", request);

      assertEquals("MATCHBOX", evsRequest.getValidationService().getName());
      assertEquals("PROFILE-ID", evsRequest.getValidationService().getValidator());
   }

   @Test
   void shouldMapServiceProfilesToValidationProfiles() {
      ValidationServiceProfile profile = new ValidationServiceProfile()
            .setValidator(
                  new Validator().setKeyword("PROFILE-ID")
            );

      List<ValidationProfile> result =
            mapper.toValidationProfiles(List.of(profile));

      assertEquals(1, result.size());
      assertEquals("PROFILE-ID", result.getFirst().getProfileID());
      assertNotNull(result.getFirst().getSupportedInputs());
   }

   @Test
   void shouldTransformEvsReportIntoValidationReport() {
      ValidationRequest request = new ValidationRequest()
            .setValidationProfileId("PROFILE")
            .addInput(new Input()
                  .setId(ValidationStepDefinition.CONTENT_TO_VALIDATE)
                  .setContent("payload".getBytes())
                  .setItemId("item-1"));

      ValidationOverview overview = new ValidationOverview()
            .setDisclaimer("use at your own risk")
            .setValidationServiceName("Matchbox")
            .setValidationServiceVersion("1.0")
            .setValidatorID("PROFILE")
            .setValidatorVersion("2025")
            .setValidationOverallResult(net.ihe.gazelle.evsapi.client.business.response.report.ValidationTestResult.PASSED)
            .setAdditionalMetadata(List.of(new Metadata().setName("meta-key").setValue("meta-value")));

      ConstraintValidation recommendedConstraint = new ConstraintValidation()
            .setConstraintID("CARD-001")
            .setConstraintType("Cardinality")
            .setConstraintDescription("desc")
            .setLocationInValidatedObject("line 5, column 3")
            .setValueInValidatedObject("actual")
            .setAssertionIDs(List.of("REQ-1"))
            .setPriority(ConstraintPriority.RECOMMENDED)
            .setTestResult(net.ihe.gazelle.evsapi.client.business.response.report.ValidationTestResult.PASSED);
      recommendedConstraint.addUnexpectedError(new UnexpectedError().setName("unexpected").setMessage("boom"));

      ConstraintValidation xmlConstraint = new ConstraintValidation()
            .setConstraintID("XML-002")
            .setConstraintDescription("XML constraint")
            .setLocationInValidatedObject("/ClinicalDocument/component[1]")
            .setPriority(ConstraintPriority.MANDATORY)
            .setTestResult(net.ihe.gazelle.evsapi.client.business.response.report.ValidationTestResult.FAILED);

      ConstraintValidation jsonConstraint = new ConstraintValidation()
            .setConstraintID("JSON-003")
            .setConstraintDescription("JSON constraint")
            .setLocationInValidatedObject("$.entry[0]")
            .setPriority(ConstraintPriority.PERMITTED)
            .setTestResult(net.ihe.gazelle.evsapi.client.business.response.report.ValidationTestResult.PASSED);

      net.ihe.gazelle.evsapi.client.business.response.report.ValidationSubReport evsSubReport =
            new net.ihe.gazelle.evsapi.client.business.response.report.ValidationSubReport()
                  .setName("structure")
                  .setSubReportResult(net.ihe.gazelle.evsapi.client.business.response.report.ValidationTestResult.PASSED)
                  .setConstraints(List.of(recommendedConstraint, xmlConstraint, jsonConstraint));

      net.ihe.gazelle.evsapi.client.business.response.report.ValidationReport evsReport =
            new net.ihe.gazelle.evsapi.client.business.response.report.ValidationReport()
                  .setValidationOverview(overview)
                  .setSubReport(List.of(evsSubReport));

      ValidationReport mapped = mapper.toValidationReport(evsReport, request);

      assertEquals("use at your own risk", mapped.getDisclaimer());
      assertEquals("PROFILE", mapped.getValidationMethod().getValidationProfileID());
      assertEquals("Matchbox", mapped.getValidationMethod().getValidationServiceName());
      assertEquals(1, mapped.getInputs().size());
      assertEquals("item-1", mapped.getInputs().getFirst().getItemId());

      List<net.ihe.gazelle.validation.v2.api.business.report.ValidationSubReport> reports = mapped.getReports();
      assertEquals(1, reports.size());
      List<net.ihe.gazelle.validation.v2.api.business.report.AssertionReport> assertions =
            reports.getFirst().getAssertionReports();
      assertEquals(3, assertions.size());

      net.ihe.gazelle.validation.v2.api.business.report.AssertionReport first = assertions.getFirst();
      assertEquals("CARD-001", first.getAssertionID());
      assertEquals(RequirementPriority.RECOMMENDED, first.getPriority());
      assertEquals(ValidationTestResult.PASSED, first.getResult());
      assertEquals(EvsValidationMapper.LINE_COLUMN_LOCATION_TYPE, first.getSubjectLocations().getFirst().getType());
      assertEquals(1, first.getUnexpectedErrors().size());

      assertEquals(EvsValidationMapper.XML_PATH_LOCATION_TYPE,
            assertions.get(1).getSubjectLocations().getFirst().getType());
      assertEquals(ValidationTestResult.FAILED, assertions.get(1).getResult());

      assertEquals(EvsValidationMapper.JSON_PATH_LOCATION_TYPE,
            assertions.get(2).getSubjectLocations().getFirst().getType());
      assertEquals(ValidationTestResult.PASSED, assertions.get(2).getResult());
   }

   @Test
   void shouldFailWhenEvsReportIsNull() {
      ValidationRequest requestV2 = new ValidationRequest();
      assertThrows(IllegalArgumentException.class,
            () -> mapper.toValidationReport(null, requestV2));
   }

   @Test
   void shouldPropagateValidationErrorsWhenReportInvalid() {
      ValidationRequest request = new ValidationRequest()
            .setValidationProfileId("PROFILE")
            .addInput(new Input().setId("doc").setContent("content".getBytes()));

      ConstraintValidation invalidConstraint = new ConstraintValidation()
            .setConstraintID("INVALID")
            .setConstraintDescription("desc")
            .setLocationInValidatedObject("line 1, column 1")
            .setPriority(ConstraintPriority.MANDATORY)
            .setTestResult(null);

      net.ihe.gazelle.evsapi.client.business.response.report.ValidationReport evsReport =
            new net.ihe.gazelle.evsapi.client.business.response.report.ValidationReport()
                  .setValidationOverview(new ValidationOverview()
                        .setDisclaimer("disc")
                        .setValidationServiceName("service")
                        .setValidationServiceVersion("1.0")
                        .setValidatorID("PROFILE")
                        .setValidatorVersion("1.0"))
                  .setSubReport(List.of(
                        new net.ihe.gazelle.evsapi.client.business.response.report.ValidationSubReport()
                              .setName("invalid")
                              .setConstraints(List.of(invalidConstraint))
                  ));

      assertThrows(IllegalArgumentException.class,
            () -> mapper.toValidationReport(evsReport, request));
   }
}
