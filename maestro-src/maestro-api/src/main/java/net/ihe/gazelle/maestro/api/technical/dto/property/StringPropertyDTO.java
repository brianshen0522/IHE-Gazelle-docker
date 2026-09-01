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
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
        name = "StringProperty",
        description = "A property that supports string value."
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName(StringPropertyDTO.TYPE)
public class StringPropertyDTO extends PropertyDTO<StringProperty> {

    @JsonIgnore
   public static final String TYPE = "STRING";

   public StringPropertyDTO() {
      super(new StringProperty());
   }

   public StringPropertyDTO(StringProperty property) {
      super(property);
   }

    @Schema(
            name = "value",
            description = "The string typed value of the property.",
            examples = "any text"
    )
   @JsonProperty(VALUE)
   public String getValue() {
      return property.getValue();
   }

   public void setValue(String value) {
      property.setValue(value);
   }

   @Override
   @JsonIgnore
   public Class<StringProperty> getBusinessObjectClass() {
      return StringProperty.class;
   }
}
