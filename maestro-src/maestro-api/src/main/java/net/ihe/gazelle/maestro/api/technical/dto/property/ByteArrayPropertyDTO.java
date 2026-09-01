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

import com.fasterxml.jackson.annotation.*;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;

@Schema(
        name = "ByteArrayProperty",
        description = "A property containing a byte array value."
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "type",
        "value",
        "fileName",
        "mimeType"
})
@JsonTypeName(ByteArrayPropertyDTO.TYPE)
public class ByteArrayPropertyDTO extends PropertyDTO<ByteArrayProperty> {

    @JsonIgnore
   public static final String TYPE = "BYTE_ARRAY";

   public ByteArrayPropertyDTO() {
      super(new ByteArrayProperty());
   }

   public ByteArrayPropertyDTO(ByteArrayProperty property) {
      super(property);
   }

    @Schema(
            name = "value",
            description = "The byte array typed value of the property, or a reference expression like ${inputName}."
    )
   @JsonProperty(VALUE)
   public Serializable getValue() {
      return property.getValue();
   }

   public void setValue(String value) {
      if (value == null) {
         property.setValue(null);
         return;
      }
      if (isReference(value)) {
         property.setValue(value);
      } else {
          property.setValue(decodeBase64(value));
      }
   }

    @Schema(
            name = "fileName",
            description = "The name of the file that is contained in the value.",
            examples = "file.json"
    )
   @JsonProperty("fileName")
   public String getFileName() {
      return property.getFileName();
   }

   public void setFileName(String originalFileName) {
      property.setFileName(originalFileName);
   }

    @Schema(
            name = "mimeType",
            description = "The mimetype of the byte data in the value.",
            examples = "application/json"
    )
   @JsonProperty("mimeType")
   public String getMimeType() {
      return property.getMimeType();
   }

   public void setMimeType(String mimeType) {
      property.setMimeType(mimeType);
   }

   @Override
   @JsonIgnore
   public Class<ByteArrayProperty> getBusinessObjectClass() {
      return ByteArrayProperty.class;
   }

   //This parses a base64 string, removing the data:...;base64, part if present
   private byte[] decodeBase64(String value) {
      String trimmed = value.strip();
      if (trimmed.startsWith("data:")) {
         int commaIndex = trimmed.indexOf(',');
         if (commaIndex >= 0 && commaIndex + 1 < trimmed.length()) {
            trimmed = trimmed.substring(commaIndex + 1);
         }
      }
      return java.util.Base64.getDecoder().decode(trimmed.replaceAll("\\s", ""));
   }

}
