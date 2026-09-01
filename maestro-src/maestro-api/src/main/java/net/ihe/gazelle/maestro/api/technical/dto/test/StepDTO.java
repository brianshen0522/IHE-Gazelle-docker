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
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.technical.dto.property.PropertyDTO;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import net.ihe.gazelle.modelmarshaller.technical.dto.SubTypingDTO;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

@Schema(
      name = "Step",
      description = "A test step to execute."
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
      "name",
      "type",
      "timeout",
      "properties",
      "outputMappings"
})
public class StepDTO implements DTO<Step> {

   @JsonIgnore
   public static final String MAPPING_DELIMITER = ":";

   @JsonIgnore
   private final Step step;

   public StepDTO() {
      this(new Step());
   }

   public StepDTO(Step step) {
      this.step = step;
   }

   @Schema(
         name = "name",
         description = "The name of the test step",
         required = true
   )
   @JsonProperty("name")
   public String getName() {
      return step.getName();
   }

   public void setName(String name) {
      step.setName(name);
   }

   @Schema(
         name = "type",
         description = "The type of the test step.",
         enumeration = {"SIMULATION", "VALIDATION", "USER_INTERACTION", "ITB", "ASSERT_CONTAINS", "ASSERT_EQUALS"},
         required = true
   )
   @JsonProperty("type")
   public String getType() {
      return step.getType();
   }

   public void setType(String type) {
      step.setType(type);
   }

   @Schema(
         name = "timeout",
         type = SchemaType.NUMBER,
         description = "The maximum time in milliseconds to let the execution running.",
         examples = "60000"
   )
   @JsonProperty("timeout")
   public Long getTimeout() {
      return step.getTimeout();
   }

   public void setTimeout(Long timeout) {
      step.setTimeout(timeout);
   }

   @Schema(
         name = "properties",
         description = "A list of properties used by the step during execution."
   )
   @JsonProperty("properties")
   @SuppressWarnings({"java:S3740", "rawtypes"})
   public List<PropertyDTO> getProperties() {
      return step.getProperties().stream()
            .map(SubTypingDTO::fromBusinessObject)
            .map(PropertyDTO.class::cast).toList();
   }

   @SuppressWarnings({"java:S3740", "rawtypes"})
   public void setProperties(List<PropertyDTO> propertyDTOs) {
      step.setProperties(
            propertyDTOs != null ?
                  propertyDTOs.stream()
                        .map(DTO::getBusinessObject)
                        .map(Property.class::cast).toList() :
                  null
      );
   }

   @Schema(
         name = "outputMappings",
         description = "A list of mapping for the outputs of the test step."
   )
   @JsonProperty("outputMappings")
   public List<String> getOutputMappings() {
      return step.getOutputMappings().entrySet().stream()
            .map(StepDTO::formatMapping)
            .toList();
   }

   public void setOutputMappings(List<String> mappings) {
      if (mappings != null) {
         mappings.stream()
               .map(StepDTO::parseMapping)
               .forEach(e -> step.addOutputMapping(e.getKey(), e.getValue()));
      }
   }

   @Override
   @JsonIgnore
   public Step getBusinessObject() {
      return step;
   }

   private static Map.Entry<String, String> parseMapping(String outputMapping) {
      String[] mapping = outputMapping.split(MAPPING_DELIMITER);
      if (mapping.length == 2) {
         return new AbstractMap.SimpleEntry<>(mapping[0], mapping[1]);
      } else {
         throw new IllegalArgumentException("Invalid output mapping: " + outputMapping);
      }
   }

   private static String formatMapping(Map.Entry<String, String> outputMapping) {
      return outputMapping.getKey() + MAPPING_DELIMITER + outputMapping.getValue();
   }

}

