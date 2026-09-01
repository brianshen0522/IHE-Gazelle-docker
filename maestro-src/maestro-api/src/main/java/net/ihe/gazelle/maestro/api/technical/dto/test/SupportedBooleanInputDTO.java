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
import com.fasterxml.jackson.annotation.JsonTypeName;
import net.ihe.gazelle.maestro.api.business.test.SupportedBooleanInput;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
      name = "SupportedBooleanInput",
      description = "A boolean input that can be used as a step or test input."
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName(SupportedBooleanInputDTO.TYPE)
public class SupportedBooleanInputDTO extends SupportedInputDTO<SupportedBooleanInput> {

   @JsonIgnore
   public static final String TYPE = "BOOLEAN";

   public SupportedBooleanInputDTO() {
      super(new SupportedBooleanInput());
   }

   public SupportedBooleanInputDTO(SupportedBooleanInput supportedInput) {
      super(supportedInput);
   }

   @Override
   @JsonIgnore
   public Class<SupportedBooleanInput> getBusinessObjectClass() {
      return SupportedBooleanInput.class;
   }

}
