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
import net.ihe.gazelle.maestro.api.business.test.SupportedValueSetInput;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
      name = "SupportedValueSetInput",
      description = "A value set input that can be used as a step or test input."
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName(SupportedValueSetInputDTO.TYPE)
@JsonPropertyOrder({
      "id",
      "type",
      "label",
      "required",
      "valueSetId",
      "defaultValue"
})
public class SupportedValueSetInputDTO extends SupportedInputDTO<SupportedValueSetInput> {

   @JsonIgnore
   public static final String TYPE = "VALUE_SET";

   public SupportedValueSetInputDTO() {
      super(new SupportedValueSetInput());
   }

   public SupportedValueSetInputDTO(SupportedValueSetInput supportedInput) {
      super(supportedInput);
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

   @Schema(
         name = "valueSetId",
         description = "The unique identifier of the value set from which to retrieve the possible values for the input."
   )
   @JsonProperty("valueSetId")
   public String getValueSetId() {
      return supportedInput.getValueSetId();
   }

   public void setValueSetId(String valueSetId) {
      supportedInput.setValueSetId(valueSetId);
   }

   @Override
   @JsonIgnore
   public Class<SupportedValueSetInput> getBusinessObjectClass() {
      return SupportedValueSetInput.class;
   }

}
