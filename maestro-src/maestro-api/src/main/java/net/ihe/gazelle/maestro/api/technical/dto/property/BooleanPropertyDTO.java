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

package net.ihe.gazelle.maestro.api.technical.dto.property;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import net.ihe.gazelle.maestro.api.business.property.BooleanProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;

@Schema(
      name = "BooleanProperty",
      description = "A property that supports boolean value."
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName(BooleanPropertyDTO.TYPE)
public class BooleanPropertyDTO extends PropertyDTO<BooleanProperty> {

   public static final String TYPE = "BOOLEAN";

   public BooleanPropertyDTO() {
      super(new BooleanProperty());
   }

   public BooleanPropertyDTO(BooleanProperty property) {
      super(property);
   }

   @Schema(
         name = "value",
         description = "The boolean typed value of the property."
   )
   @JsonProperty(VALUE)
   public Boolean getValue() {
      return property.getValue();
   }

   public void setValue(Serializable value) {
      super.setResolvedValue(Boolean.class, Boolean::valueOf, value);
   }

   @Override
   public Class<BooleanProperty> getBusinessObjectClass() {
      return BooleanProperty.class;
   }
}
