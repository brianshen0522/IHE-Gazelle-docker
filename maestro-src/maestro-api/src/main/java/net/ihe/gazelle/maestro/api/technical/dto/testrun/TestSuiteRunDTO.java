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
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRun;
import net.ihe.gazelle.maestro.api.technical.dto.property.PropertyDTO;
import net.ihe.gazelle.maestro.api.technical.dto.report.SystemUnderTestDTO;
import net.ihe.gazelle.maestro.api.technical.dto.test.TestDTO;
import net.ihe.gazelle.maestro.api.technical.dto.test.TestSuiteDTO;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import net.ihe.gazelle.modelmarshaller.technical.dto.SubTypingDTO;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import net.ihe.gazelle.security.technical.acl.AccessControlListDTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(
      name = "TestSuiteRun",
      description = "A test suite run is a test suite execution."
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
      "testSuite",
      "tests",
      "inputs",
      "systemsUnderTest",
      "accessControlList"
})
public class TestSuiteRunDTO implements DTO<TestSuiteRun> {

   @JsonIgnore
   private final TestSuiteRun testSuiteRun;

   public TestSuiteRunDTO() {
      this(new TestSuiteRun());
   }

   public TestSuiteRunDTO(TestSuiteRun testSuiteRun) {
      this.testSuiteRun = testSuiteRun;
   }

   @Override
   @JsonIgnore
   public TestSuiteRun getBusinessObject() {
      return testSuiteRun;
   }

   @Schema(
         name = "testSuite",
         description = "The test suite definition that will be used for execution.",
         required = true
   )
   @JsonProperty("testSuite")
   public TestSuiteDTO getTestSuite() {
      return new TestSuiteDTO(testSuiteRun.getTestSuite());
   }

   public void setTestSuite(TestSuiteDTO testSuiteDTO) {
      testSuiteRun.setTestSuite(testSuiteDTO.getBusinessObject());
   }

   @Schema(
         name = "tests",
         description = "The list of test to execute."
   )
   @JsonProperty("tests")
   public List<TestDTO> getTests() {
      return testSuiteRun.getTests().stream().map(TestDTO::new).toList();
   }

   public void setTests(List<TestDTO> testDTOs) {
      testSuiteRun.setTests(
            testDTOs != null ?
                  testDTOs.stream()
                        .map(TestDTO::getBusinessObject)
                        .toList() :
                  null
      );
   }

   @Schema(
         name = "inputs",
         description = "A list of input provided for test suite execution that shall match supported inputs of test suite definition."
   )
   @SuppressWarnings("rawtypes")
   @JsonProperty("inputs")
   public List<PropertyDTO> getInputs() {
      return testSuiteRun.getInputs().stream()
            .map(SubTypingDTO::fromBusinessObject)
            .map(PropertyDTO.class::cast)
            .toList();
   }

   @SuppressWarnings("rawtypes")
   public void setInputs(List<PropertyDTO> inputs) {
      testSuiteRun.setInputs(
            inputs != null ? inputs.stream()
                  .map(DTO::getBusinessObject)
                  .map(Property.class::cast).toList() :
                  null
      );
   }

   @Schema(
         name = "systemsUnderTest",
         description = "The list of system under test involved in the test suite."
   )
   @JsonProperty("systemsUnderTest")
   public List<SystemUnderTestDTO> getSystemsUnderTest() {
      return testSuiteRun.getSystemsUnderTest().stream().map(SystemUnderTestDTO::new).toList();
   }

   public void setSystemsUnderTest(List<SystemUnderTestDTO> sutDTOs) {
      testSuiteRun.setSystemsUnderTest(
            sutDTOs != null ?
                  sutDTOs.stream()
                        .map(SystemUnderTestDTO::getBusinessObject)
                        .toList() :
                  null
      );
   }

   @Schema(
         name = "accessControlList",
         description = "An access control list for read, write authorization.",
         required = true
   )
   @JsonProperty("accessControlList")
   public AccessControlListDTO getAccessControlList() {
      AccessControlList acl = testSuiteRun.getAccessControlList();
      return acl != null
            ? new AccessControlListDTO(testSuiteRun.getAccessControlList())
            : null;
   }

   public void setAccessControlList(AccessControlListDTO accessControlList) {
      if (accessControlList != null) {
         testSuiteRun.setAccessControlList(accessControlList.getBusinessObject());
      }
   }

}
