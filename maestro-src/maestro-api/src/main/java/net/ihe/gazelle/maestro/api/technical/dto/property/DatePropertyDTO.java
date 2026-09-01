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
import net.ihe.gazelle.maestro.api.business.property.DateProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Schema(
        name = "DateProperty",
        description = "A property that supports date value."
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName(DatePropertyDTO.TYPE)
public class DatePropertyDTO extends PropertyDTO<DateProperty> {

    @JsonIgnore
   public static final String TYPE = "DATE";

   public DatePropertyDTO() {
      super(new DateProperty());
   }

   public DatePropertyDTO(DateProperty property) {
      super(property);
   }

    @Schema(
            name = "value",
            description = "The date typed value of the property.",
            examples = "2025-01-01T16:30:00.387+01:00"
    )
   @JsonProperty(VALUE)
   public String getValue() {
      return ZonedDateTime.ofInstant(property.getValue(), ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
   }

   public void setValue(String value) {
      if (isReference(value)) {
         property.setValue(value);
      } else {
         property.setValue(Instant.from(ZonedDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
      }
   }

   @Override
   @JsonIgnore
   public Class<DateProperty> getBusinessObjectClass() {
      return DateProperty.class;
   }
}
