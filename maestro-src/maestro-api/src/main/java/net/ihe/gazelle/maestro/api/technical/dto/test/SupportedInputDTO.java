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

import com.fasterxml.jackson.annotation.*;
import net.ihe.gazelle.maestro.api.business.test.SupportedInput;
import net.ihe.gazelle.modelmarshaller.technical.dto.SubTypingDTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
      name = "SupportedInput",
      description = "An input that can be used by one or more step during execution."
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
      "id",
      "type",
      "label",
      "groupName",
      "description",
      "required"
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class SupportedInputDTO<T extends SupportedInput> implements SubTypingDTO<T> {

   @JsonIgnore
   protected T supportedInput;

   protected SupportedInputDTO(T supportedInput) {
      initWithBusinessObject(supportedInput);
   }

   @Override
   @JsonIgnore
   public T getBusinessObject() {
      return supportedInput;
   }

   @Override
   @JsonIgnore
   public SubTypingDTO<T> initWithBusinessObject(T supportedInput) {
      this.supportedInput = supportedInput;
      return this;
   }

   @Schema(
         name = "id",
         description = "The unique identifier of the input.",
         required = true
   )
   @JsonProperty("id")
   public String getId() {
      return supportedInput.getId();
   }

   public void setId(String id) {
      supportedInput.setId(id);
   }

   @Schema(
         name = "label",
         description = "A human readable text to label the input.",
         required = true
   )
   @JsonProperty("label")
   public String getLabel() {
      return supportedInput.getLabel();
   }

   public void setLabel(String label) {
      supportedInput.setLabel(label);
   }

   @Schema(
         name = "groupName",
         description = "A name to gather inputs of the same kind together."
   )
   @JsonProperty("groupName")
   public String getGroupName() {
      return supportedInput.getGroupName();
   }

   public void setGroupName(String groupName) {
      supportedInput.setGroupName(groupName);
   }

   @Schema(
         name = "description",
         description = "The description of what is supposed to be uploaded in the input."
   )
   @JsonProperty("description")
   public String getDescription() {
      return supportedInput.getDescription();
   }

   public void setDescription(String description) {
      supportedInput.setDescription(description);
   }

   @Schema(
         name = "required",
         description = "A boolean value to specify if the input in mandatory or optional."
   )
   @JsonProperty("required")
   public boolean isRequired() {
      return supportedInput.isRequired();
   }

   public void setRequired(boolean required) {
      supportedInput.setRequired(required);
   }

}
