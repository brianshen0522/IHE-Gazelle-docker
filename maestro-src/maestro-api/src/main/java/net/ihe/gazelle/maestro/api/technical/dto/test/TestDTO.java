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

package net.ihe.gazelle.maestro.api.technical.dto.test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.test.SupportedInput;
import net.ihe.gazelle.maestro.api.business.test.Test;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import net.ihe.gazelle.modelmarshaller.technical.dto.SubTypingDTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(
      name = "Test",
      description = "The test to execute."
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
      "id",
      "name",
      "supportedInputs",
      "steps",
})
public class TestDTO implements DTO<Test> {

   @JsonIgnore
   private final Test test;

   public TestDTO() {
      this(new Test());
   }

   public TestDTO(Test test) {
      this.test = test;
   }

   @Override
   @JsonIgnore
   public Test getBusinessObject() {
      return test;
   }

   @Schema(
         name = "id",
         description = "The unique identifier of the test.",
         required = true
   )
   @JsonProperty("id")
   public String getId() {
      return test.getId();
   }

   public void setId(String id) {
      test.setId(id);
   }

   @Schema(
         name = "name",
         description = "The name of the test.",
         required = true
   )
   @JsonProperty("name")
   public String getName() {
      return test.getName();
   }

   public void setName(String name) {
      test.setName(name);
   }

   @Schema(
         name = "supportedInputs",
         description = "The list of input that are supported by the test."
   )
   @SuppressWarnings("rawtypes")
   @JsonProperty("supportedInputs")
   public List<SupportedInputDTO> getSupportedInputs() {
      return test.getSupportedInputs().stream()
            .map(SubTypingDTO::fromBusinessObject)
            .map(SupportedInputDTO.class::cast)
            .toList();
   }

   @SuppressWarnings("rawtypes")
   public void setSupportedInputs(List<SupportedInputDTO> supportedInputs) {
      test.setSupportedInputs(
            supportedInputs != null ?
                  supportedInputs.stream()
                        .map(DTO::getBusinessObject)
                        .map(SupportedInput.class::cast)
                        .toList() :
                  null
      );
   }

   @Schema(
         name = "steps",
         description = "The list of step that will be executed by the test.",
         required = true,
         minLength = 1
   )
   @JsonProperty("steps")
   public List<StepDTO> getSteps() {
      return test.getSteps().stream()
            .map(StepDTO::new)
            .toList();
   }

   public void setSteps(List<StepDTO> steps) {
      test.setSteps(
            steps != null ?
                  steps.stream()
                        .map(DTO::getBusinessObject)
                        .map(Step.class::cast)
                        .toList() :
                  null
      );
   }
}
