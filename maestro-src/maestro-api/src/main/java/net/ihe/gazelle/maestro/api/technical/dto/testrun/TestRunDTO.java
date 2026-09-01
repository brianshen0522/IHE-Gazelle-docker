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

package net.ihe.gazelle.maestro.api.technical.dto.testrun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.maestro.api.technical.dto.property.PropertyDTO;
import net.ihe.gazelle.maestro.api.technical.dto.report.SystemUnderTestDTO;
import net.ihe.gazelle.maestro.api.technical.dto.test.TestDTO;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import net.ihe.gazelle.modelmarshaller.technical.dto.SubTypingDTO;
import net.ihe.gazelle.security.technical.acl.AccessControlListDTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(
      name = "TestRun",
      description = "A test run to be executed by the test engine."
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
      "test",
      "inputs",
      "systemsUnderTest",
      "accessControlList"
})
public class TestRunDTO implements DTO<TestRun> {

   @JsonIgnore
   private final TestRun testRun;

   public TestRunDTO() {
      this(new TestRun());
   }

   public TestRunDTO(TestRun testRun) {
      this.testRun = testRun;
   }

   @Override
   @JsonIgnore
   public TestRun getBusinessObject() {
      return testRun;
   }

   @Schema(
         name = "test",
         description = "The test definition that will be used for execution.",
         required = true
   )
   @JsonProperty("test")
   public TestDTO getTest() {
      return new TestDTO(testRun.getTest());
   }

   public void setTest(TestDTO test) {
      testRun.setTest(test.getBusinessObject());
   }

   @Schema(
         name = "inputs",
         description = "A list of input provided for test execution that shall match supported inputs of test definition."
   )
    @SuppressWarnings("rawtypes")
   @JsonProperty("inputs")
   public List<PropertyDTO> getInputs() {
      return testRun.getInputs().stream()
            .map(SubTypingDTO::fromBusinessObject)
            .map(PropertyDTO.class::cast)
            .toList();
   }

   public void setInputs(List<PropertyDTO<?>> inputs) {
      testRun.setInputs(
              inputs.stream()
                      .map(DTO::getBusinessObject)
                      .map(Property.class::cast)
                      .toList()
      );
   }

   @Schema(
         name = "accessControlList",
         description = "An access control list for read, write authorization.",
         required = true
   )
   @JsonProperty("accessControlList")
   public AccessControlListDTO getAccessControlList() {
      return new AccessControlListDTO(testRun.getAccessControlList());
   }

   public void setAccessControlList(AccessControlListDTO accessControlList) {
      testRun.setAccessControlList(accessControlList.getBusinessObject());
   }

   @Schema(
         name = "systemsUnderTest",
         description = "The list of system under test involved in the test."
   )
   @JsonProperty("systemsUnderTest")
   public List<SystemUnderTestDTO> getSystemsUnderTest() {
      return testRun.getSystemsUnderTest() != null ? testRun.getSystemsUnderTest().stream().map(SystemUnderTestDTO::new).toList() : null;
   }

   public void setSystemsUnderTest(List<SystemUnderTestDTO> sutDTOs) {
      testRun.setSystemsUnderTest(
              sutDTOs.stream()
                      .map(SystemUnderTestDTO::getBusinessObject)
                      .toList()
      );
   }
}
