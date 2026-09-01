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

package net.ihe.gazelle.maestro.api.technical.dto.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.maestro.api.business.testreport.TestService;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
      name = "TestService",
      description = "The service used as the test engine to execute the test suite."
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
      "serviceIdentification",
      "disclaimer"
})
public class TestServiceDTO implements DTO<TestService> {

   @JsonIgnore
   private final TestService testService;

   public TestServiceDTO() {
      this(new TestService());
   }

   public TestServiceDTO(TestService testService) {
      this.testService = testService;
   }

   @Override
   @JsonIgnore
   public TestService getBusinessObject() {
      return testService;
   }

   @Schema(
         name = "serviceIdentification",
         description = "The identification of the service with name and version.",
         required = true
   )
   @JsonProperty(value = "serviceIdentification")
   public EntityIdentificationDTO getServiceIdentification() {
      return new EntityIdentificationDTO(testService.getServiceIdentification());
   }

   public void setServiceIdentification(EntityIdentificationDTO entityIdentificationDTO) {
      testService.setServiceIdentification(entityIdentificationDTO.getBusinessObject());
   }

   @Schema(
         name = "disclaimer",
         description = "A disclaimer about the test service.",
         required = true
   )
   @JsonProperty(value = "disclaimer")
   public String getDisclaimer() {
      return testService.getDisclaimer();
   }

   public void setDisclaimer(String disclaimer) {
      testService.setDisclaimer(disclaimer);
   }
}
