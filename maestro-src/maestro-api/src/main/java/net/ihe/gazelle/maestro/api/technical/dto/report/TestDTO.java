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

package net.ihe.gazelle.maestro.api.technical.dto.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.maestro.api.business.testreport.Test;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
      name = "Test",
      description = "The test that has been executed."
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
      "id",
      "name",
      "version",
      "description"
})
public class TestDTO implements DTO<Test> {

   @JsonIgnore
   private final Test test;

   public TestDTO() {
      this(new Test());
   }

   public TestDTO(Test test) {
      this.test = test;
   }

   @Override
   @JsonIgnore
   public Test getBusinessObject() {
      return test;
   }

   @Schema(
         name = "id",
         description = "The unique identifier of the test.",
         required = true
   )
   @JsonProperty(value = "id")
   public String getId() {
      return test.getId();
   }

   public void setId(String id) {
      test.setId(id);
   }

   @Schema(
         name = "name",
         description = "The name of the test."
   )
   @JsonProperty(value = "name")
   public String getName() {
      return test.getName();
   }

   public void setName(String name) {
      test.setName(name);
   }

   @Schema(
         name = "version",
         description = "The version of the test."
   )
   @JsonProperty(value = "version")
   public String getVersion() {
      return test.getVersion();
   }

   public void setVersion(String version) {
      test.setVersion(version);
   }

   @Schema(
         name = "description",
         description = "The description of the test."
   )
   @JsonProperty(value = "description")
   public String getDescription() {
      return test.getDescription();
   }

   public void setDescription(String description) {
      test.setDescription(description);
   }
}
