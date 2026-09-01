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
import net.ihe.gazelle.maestro.api.business.test.SupportedTextInput;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(
      name = "SupportedTextInput",
      description = "A text input that can be used as a step or test input."
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonTypeName(SupportedTextInputDTO.TYPE)
@JsonPropertyOrder({
      "id",
      "type",
      "label",
      "required",
      "possibleValues",
      "defaultValue"
})
public class SupportedTextInputDTO extends SupportedInputDTO<SupportedTextInput> {

   @JsonIgnore
   public static final String TYPE = "TEXT";

   public SupportedTextInputDTO() {
      super(new SupportedTextInput());
   }

   public SupportedTextInputDTO(SupportedTextInput supportedInput) {
      super(supportedInput);
   }

   @Schema(
         name = "possibleValues",
         description = "A predefined list of possible values for this input."
   )
   @JsonProperty("possibleValues")
   public List<String> getPossibleValues() {
      return supportedInput.getPossibleValues();
   }

   public void setPossibleValues(List<String> possibleValues) {
      supportedInput.setPossibleValues(possibleValues);
   }

   @Schema(
         name = "defaultValue",
         description = "A default value provided by the step definition."
   )
   @JsonProperty("defaultValue")
   public String getDefaultValue() {
      return supportedInput.getDefaultValue();
   }

   public void setDefaultValue(String defaultValue) {
      supportedInput.setDefaultValue(defaultValue);
   }

   @Override
   @JsonIgnore
   public Class<SupportedTextInput> getBusinessObjectClass() {
      return SupportedTextInput.class;
   }

}
