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

package net.ihe.gazelle.maestro.api.technical.dto.test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.test.TestReference;
import net.ihe.gazelle.maestro.api.technical.dto.property.PropertyDTO;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import net.ihe.gazelle.modelmarshaller.technical.dto.SubTypingDTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(
      name = "TestReference",
      description = "The test reference is the test id linked to a list of properties in the test suite definition."
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
      "testId",
      "properties",
})
public class TestReferenceDTO implements DTO<TestReference> {

   @JsonIgnore
   private final TestReference testReference;

   public TestReferenceDTO() {
      this(new TestReference());
   }

   public TestReferenceDTO(TestReference testReference) {
      this.testReference = testReference;
   }

   @Schema(
         name = "testId",
         description = "The unique identifier of the referenced test.",
         required = true
   )
   @JsonProperty("testId")
   public String getTestId() {
      return testReference.getTestId();
   }

   public void setTestId(String testId) {
      testReference.setTestId(testId);
   }

   @Schema(
         name = "properties",
         description = "The list of properties of the test."
   )
   @SuppressWarnings("rawtypes")
   @JsonProperty("properties")
   public List<PropertyDTO> getProperties() {
      return testReference.getProperties().stream()
            .map(SubTypingDTO::fromBusinessObject)
            .map(PropertyDTO.class::cast)
            .toList();
   }

   @SuppressWarnings("rawtypes")
   public void setProperties(List<PropertyDTO> propertyDTOs) {
      testReference.setProperties(
            propertyDTOs != null ?
                  propertyDTOs.stream()
                        .map(DTO::getBusinessObject)
                        .map(Property.class::cast)
                        .toList() :
                  null
      );
   }

   @Override
   @JsonIgnore
   public TestReference getBusinessObject() {
      return testReference;
   }
}
