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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import net.ihe.gazelle.maestro.api.business.property.IntegerProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;

@Schema(
      name = "IntegerProperty",
      description = "A property that supports integer value."
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName(IntegerPropertyDTO.TYPE)
public class IntegerPropertyDTO extends PropertyDTO<IntegerProperty> {

   @JsonIgnore
   public static final String TYPE = "INTEGER";

   public IntegerPropertyDTO() {
      super(new IntegerProperty());
   }

   public IntegerPropertyDTO(IntegerProperty property) {
      super(property);
   }

   @Schema(
         name = "value",
         description = "The integer typed value of the property.",
         examples = "3"
   )
   @JsonProperty(VALUE)
   public Integer getValue() {
      return property.getValue();
   }

   public void setValue(Serializable value) {
      super.setResolvedValue(Integer.class, Integer::valueOf, value);
   }

   @Override
   @JsonIgnore
   public Class<IntegerProperty> getBusinessObjectClass() {
      return IntegerProperty.class;
   }
}
