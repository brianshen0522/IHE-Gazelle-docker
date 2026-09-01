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
import net.ihe.gazelle.maestro.api.business.test.SupportedInput;
import net.ihe.gazelle.maestro.api.business.test.TestReference;
import net.ihe.gazelle.maestro.api.business.test.TestSuite;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import net.ihe.gazelle.modelmarshaller.technical.dto.SubTypingDTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(
      name = "TestSuite",
      description = "The test suite is a test scenario where one or more test can be executed sequentially."
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
      "id",
      "name",
      "testReferences",
      "supportedInputs",
})
public class TestSuiteDTO implements DTO<TestSuite> {

   @JsonIgnore
   private final TestSuite testSuite;

   public TestSuiteDTO() {
      this(new TestSuite());
   }

   public TestSuiteDTO(TestSuite testSuite) {
      this.testSuite = testSuite;
   }

   @Schema(
         name = "id",
         description = "The unique identifier of the test suite.",
         required = true
   )
   @JsonProperty("id")
   public String getId() {
      return testSuite.getId();
   }

   public void setId(String id) {
      testSuite.setId(id);
   }

   @Schema(
         name = "name",
         description = "The name of the test suite.",
         required = true
   )
   @JsonProperty("name")
   public String getName() {
      return testSuite.getName();
   }

   public void setName(String name) {
      testSuite.setName(name);
   }

   @Schema(
         name = "testReferences",
         description = "The list of test referenced by the test suite.",
         required = true,
         minLength = 1
   )
   @JsonProperty("testReferences")
   public List<TestReferenceDTO> getTestReferences() {
      return testSuite.getTestReferences().stream()
            .map(TestReferenceDTO::new)
            .toList();
   }

   public void setTestReferences(List<TestReferenceDTO> testReferenceDTOs) {
      testSuite.setTestReferences(
            testReferenceDTOs != null ?
                  testReferenceDTOs.stream()
                        .map(DTO::getBusinessObject)
                        .map(TestReference.class::cast)
                        .toList() :
                  null
      );
   }

   @Schema(
         name = "supportedInputs",
         description = "A list of input that can be accessible across all test in the test suite."
   )
   @SuppressWarnings("rawtypes")
   @JsonProperty("supportedInputs")
   public List<SupportedInputDTO> getSupportedInputs() {
      return testSuite.getSupportedInputs().stream()
            .map(SubTypingDTO::fromBusinessObject)
            .map(SupportedInputDTO.class::cast)
            .toList();
   }

   @SuppressWarnings("rawtypes")
   public void setSupportedInputs(List<SupportedInputDTO> supportedInputDTOs) {
      testSuite.setSupportedInputs(
            supportedInputDTOs != null ?
                  supportedInputDTOs.stream()
                        .map(DTO::getBusinessObject)
                        .map(SupportedInput.class::cast)
                        .toList() :
                  null
      );
   }

   @Override
   @JsonIgnore
   public TestSuite getBusinessObject() {
      return testSuite;
   }

}
