/*
Copyright 2010-2025 IHE International

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package net.ihe.gazelle.xmlvalidation.ws;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.framework.modelvalidator.technical.adapter.bvalid.BValidatorBuilderFactory;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.jackson.ObjectMapperBuilder;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import net.ihe.gazelle.validation.v2.api.business.request.InputInRequestBuilder;
import net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest;
import net.ihe.gazelle.validation.v2.api.business.request.ValidationRequestBuilder;
import net.ihe.gazelle.validation.v2.api.technical.dto.report.ValidationReportDTO;
import net.ihe.gazelle.xmlvalidation.ws.config.IntegrationConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(value = IntegrationConfig.class, restrictToAnnotatedClass = true)
class ValidationApiWSIT {

   private static final JacksonSerDes structureMapper = new JacksonSerDes(
         new ObjectMapperBuilder().build()
   );

   protected static ValidatorBuilderFactory validatorBuilderFactory = new BValidatorBuilderFactory();

   @Test
   void testValidate() throws IOException {
      ValidationReportDTO actualReport =
            given()
                  .when()
                  .contentType("application/json")
                  .body(constructValidationRequest())
                  .post("/rest/validation/v2/validate")
                  .then()
                  .statusCode(200)
                  .extract()
                  .as(ValidationReportDTO.class);
      assertEqualReports(getExpectedReport(), actualReport);
   }

   private ValidationReportDTO getExpectedReport() throws IOException {
      try(InputStream inputStream = this.getClass().getResourceAsStream("/expectedReport.json")) {
         return structureMapper.deserialize(
               new String(inputStream.readAllBytes(), StandardCharsets.UTF_8),
               ValidationReportDTO.class
         );
      }
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testGetValidationProfiles() {
      List<ValidationProfile> actualProfiles =
            given()
                  .when()
                  .contentType("application/json")
                  .get("/rest/validation/v2/profiles")
                  .then()
                  .statusCode(200)
                  .extract()
                  .as(new TypeRef<ArrayList<ValidationProfile>>() {
                  });

      assertEquals(1, actualProfiles.size());
      assertEquals("test_profile", actualProfiles.get(0).getProfileID());
      assertEquals("test_profile", actualProfiles.get(0).getProfileName());
      assertEquals("test_domain", actualProfiles.get(0).getDomain());
      assertEquals(0, actualProfiles.get(0).getCoveredItems().size());
   }

   private void assertEqualReports(ValidationReportDTO expectedReport, ValidationReportDTO actualReport)
         throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      mapper.addMixIn(ValidationReportDTO.class, ValidationReportMixin.class);
      // using pretty print to make the output more readable for debugging
      String expectedReportJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectedReport);
      String actualReportJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualReport);
      assertEquals(expectedReportJson, actualReportJson);
   }


   private ValidationRequest constructValidationRequest() {
      byte[] content;
      try {
         content = getClass().getClassLoader()
               .getResourceAsStream("xml/valid01ForSch.xml")
               .readAllBytes();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
      return new ValidationRequestBuilder(validatorBuilderFactory)
            .addInput(new InputInRequestBuilder(validatorBuilderFactory)
                  .setContent(content)
                  .setItemId("test_item")
            )
            .setValidationProfileId("test_profile")
            .build();
   }


   private static class ValidationReportMixin extends ValidationReportDTO {
      @Serial
      private static final long serialVersionUID = -1015511751090298492L;

      @Override
      @JsonIgnore
      public String getUuid() {
         return super.getUuid();
      }

      @Override
      @JsonIgnore
      public Date getDateTime() {
         return super.getDateTime();
      }
   }

}
