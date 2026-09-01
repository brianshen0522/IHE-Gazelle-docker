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

package net.ihe.gazelle.maestro.api.technical.dto;

import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.test.SupportedTextInput;
import net.ihe.gazelle.maestro.api.business.test.TestReference;
import net.ihe.gazelle.maestro.api.business.test.TestSuite;
import net.ihe.gazelle.maestro.api.business.testreport.EntityIdentification;
import net.ihe.gazelle.maestro.api.business.testreport.SystemUnderTest;
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRun;
import net.ihe.gazelle.maestro.api.technical.dto.property.PropertyDTO;
import net.ihe.gazelle.maestro.api.technical.dto.property.StringPropertyDTO;
import net.ihe.gazelle.maestro.api.technical.dto.report.SystemUnderTestDTO;
import net.ihe.gazelle.maestro.api.technical.dto.test.SupportedTextInputDTO;
import net.ihe.gazelle.maestro.api.technical.dto.test.TestDTO;
import net.ihe.gazelle.maestro.api.technical.dto.test.TestReferenceDTO;
import net.ihe.gazelle.maestro.api.technical.dto.test.TestSuiteDTO;
import net.ihe.gazelle.maestro.api.technical.dto.testrun.TestSuiteRunDTO;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import net.ihe.gazelle.security.technical.acl.AccessControlListDTO;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TestSuiteRunDTOTest {

   private static final TextSerDes SER_DES = new JacksonSerDes();
   private static final Path TEST_SUITE_RUN_JSON = Path.of("src/test/resources/testRun/test-suite-run.json");

   @Test
   void shouldExposeNestedStateFromBusinessObject() {
      TestSuite testSuite = new TestSuite()
            .setId("suite-1")
            .setName("Full suite")
            .setTestReferences(List.of(new TestReference().setTestId("test-1")))
            .setSupportedInputs(List.of(new SupportedTextInput().setId("input-1").setLabel("Input").setRequired(true)));

      Step step = new Step()
            .setName("Validate")
            .setType("validation")
            .setProperties(List.of(new StringProperty("threshold", "strict")));

      net.ihe.gazelle.maestro.api.business.test.Test test = new net.ihe.gazelle.maestro.api.business.test.Test()
            .setId("test-1")
            .setName("Validation test")
            .addStep(step);

      SystemUnderTest systemUnderTest = new SystemUnderTest()
            .setSystemIdentification(new EntityIdentification("sut-1").setVersion("1.0"))
            .addHostName("sut.example.com")
            .addIpAddress("192.0.2.20");

      TestSuiteRun testSuiteRun = new TestSuiteRun()
            .setTestSuite(testSuite)
            .setTests(List.of(test))
            .setInputs(List.of(new StringProperty("key", "value")))
            .setSystemsUnderTest(List.of(systemUnderTest));

      TestSuiteRunDTO dto = new TestSuiteRunDTO(testSuiteRun);

      TestSuiteDTO suiteDTO = dto.getTestSuite();
      assertEquals("suite-1", suiteDTO.getId());
      assertEquals("Full suite", suiteDTO.getName());
      assertEquals(1, suiteDTO.getTestReferences().size());
      assertEquals("test-1", suiteDTO.getTestReferences().getFirst().getTestId());

      List<TestDTO> tests = dto.getTests();
      assertEquals(1, tests.size());
      TestDTO testDTO = tests.getFirst();
      assertEquals("test-1", testDTO.getId());
      assertEquals("Validation test", testDTO.getName());
      assertEquals(1, testDTO.getSteps().size());
      assertEquals("Validate", testDTO.getSteps().getFirst().getName());

      List<PropertyDTO> inputs = dto.getInputs();
      assertEquals(1, inputs.size());
      PropertyDTO<?> propertyDTO = inputs.getFirst();
      assertEquals("key", propertyDTO.getName());
      assertEquals("value", ((StringPropertyDTO) propertyDTO).getValue());

      List<SystemUnderTestDTO> systemsUnderTest = dto.getSystemsUnderTest();
      assertEquals(1, systemsUnderTest.size());
      SystemUnderTestDTO systemUnderTestDTO = systemsUnderTest.getFirst();
      assertEquals("sut-1", systemUnderTestDTO.getSystemIdentification().getName());
      assertTrue(systemUnderTestDTO.getHostNames().contains("sut.example.com"));

      AccessControlListDTO accessControlListDTO = dto.getAccessControlList();
      assertNull(accessControlListDTO);

      assertSame(testSuiteRun, dto.getBusinessObject());
   }

   @Test
   void shouldPopulateBusinessObjectFromNestedDtos() {
      TestSuiteRunDTO dto = new TestSuiteRunDTO();

      TestSuiteDTO suiteDTO = new TestSuiteDTO();
      suiteDTO.setId("suite-2");
      suiteDTO.setName("Suite two");
      TestReferenceDTO referenceDTO = new TestReferenceDTO();
      referenceDTO.setTestId("test-2");
      suiteDTO.setTestReferences(List.of(referenceDTO));
      suiteDTO.setSupportedInputs(List.of(new SupportedTextInputDTO(new SupportedTextInput().setId("input").setLabel("Input"))));
      dto.setTestSuite(suiteDTO);

      net.ihe.gazelle.maestro.api.business.test.Test test = new net.ihe.gazelle.maestro.api.business.test.Test()
            .setId("test-2")
            .setName("Simulation");
      test.addStep(new Step()
            .setName("Simulate")
            .setType("simulation")
            .setProperties(List.of(new StringProperty("mode", "AUTO"))));
      dto.setTests(List.of(new TestDTO(test)));

      StringPropertyDTO inputDTO = new StringPropertyDTO(new StringProperty("payload", "value"));
      dto.setInputs(List.of(inputDTO));

      SystemUnderTestDTO systemUnderTestDTO = new SystemUnderTestDTO(new SystemUnderTest()
            .setSystemIdentification(new EntityIdentification("sut-2"))
            .addHostName("sut-2.example.com"));
      dto.setSystemsUnderTest(List.of(systemUnderTestDTO));

      dto.setAccessControlList(new AccessControlListDTO());

      TestSuiteRun businessObject = dto.getBusinessObject();
      assertEquals("suite-2", businessObject.getTestSuite().getId());
      assertEquals("Suite two", businessObject.getTestSuite().getName());
      assertEquals(1, businessObject.getTestSuite().getTestReferences().size());
      assertEquals("test-2", businessObject.getTestSuite().getTestReferences().getFirst().getTestId());

      assertEquals(1, businessObject.getTests().size());
      assertEquals("test-2", businessObject.getTests().getFirst().getId());
      assertEquals(1, businessObject.getTests().getFirst().getSteps().size());

      assertEquals(1, businessObject.getInputs().size());
      assertEquals("payload", businessObject.getInputs().getFirst().getName());
      assertEquals("value", businessObject.getInputs().getFirst().getValue());

      assertEquals(1, businessObject.getSystemsUnderTest().size());
      assertTrue(businessObject.getSystemsUnderTest().getFirst().getHostNames().contains("sut-2.example.com"));
      assertNotNull(businessObject.getAccessControlList());

      dto.setTests(null);
      assertTrue(businessObject.getTests().isEmpty());

      dto.setInputs(null);
      assertTrue(businessObject.getInputs().isEmpty());

      dto.setSystemsUnderTest(null);
      assertTrue(businessObject.getSystemsUnderTest().isEmpty());
   }

   @Test
   @SuppressWarnings("unchecked")
   void shouldSerializeToJson() {
      SupportedTextInput supportedInput = new SupportedTextInput()
            .setId("input-serialize")
            .setLabel("Serialize input")
            .setRequired(true);

      TestSuite suite = new TestSuite()
            .setId("suite-serialize")
            .setName("Serialize suite")
            .setTestReferences(List.of(new TestReference().setTestId("test-serialize")))
            .setSupportedInputs(List.of(supportedInput));

      Step step = new Step()
            .setName("Execute")
            .setType("simulation")
            .setProperties(List.of(new StringProperty("mode", "STRICT")));

      net.ihe.gazelle.maestro.api.business.test.Test test = new net.ihe.gazelle.maestro.api.business.test.Test()
            .setId("test-serialize")
            .setName("Serialize test")
            .addStep(step);

      SystemUnderTest sut = new SystemUnderTest()
            .setSystemIdentification(new EntityIdentification("sut-serialize").setVersion("2.0"))
            .addHostName("sut.serialize.example");

      AccessControlList accessControlList = new AccessControlList()
            .setOwners(Set.of("owner"))
            .setReaders(Set.of("reader"))
            .setPublic(false)
            .setReadAccessKey("key");

      TestSuiteRun source = new TestSuiteRun()
            .setTestSuite(suite)
            .setTests(List.of(test))
            .setInputs(List.of(new StringProperty("payload", "data")))
            .setSystemsUnderTest(List.of(sut))
            .setAccessControlList(accessControlList);

      String json = assertDoesNotThrow(() -> SER_DES.serializeAsString(new TestSuiteRunDTO(source)));
      Map<String, Object> payload = assertDoesNotThrow(() -> SER_DES.deserialize(json, Map.class));

      Map<String, Object> suitePayload = (Map<String, Object>) payload.get("testSuite");
      assertEquals("suite-serialize", suitePayload.get("id"));

      List<Map<String, Object>> tests = (List<Map<String, Object>>) payload.get("tests");
      assertEquals(1, tests.size());
      assertEquals("test-serialize", tests.getFirst().get("id"));

      List<Map<String, Object>> inputs = (List<Map<String, Object>>) payload.get("inputs");
      assertEquals(1, inputs.size());
      assertEquals("payload", inputs.getFirst().get("name"));

      Map<String, Object> aclPayload = (Map<String, Object>) payload.get("accessControlList");
      assertEquals(Boolean.FALSE, aclPayload.get("isPublic"));
      assertEquals("key", aclPayload.get("readAccessKey"));
   }

   @Test
   void shouldDeserializeFromFixtureJson() {
      String json = assertDoesNotThrow(() -> Files.readString(TEST_SUITE_RUN_JSON));
      TestSuiteRunDTO dto = assertDoesNotThrow(() -> SER_DES.deserialize(json, TestSuiteRunDTO.class));

      TestSuiteRun restored = dto.getBusinessObject();
      assertEquals("suite-fixture", restored.getTestSuite().getId());
      assertEquals("Fixture Suite", restored.getTestSuite().getName());
      assertEquals(1, restored.getTestSuite().getTestReferences().size());
      assertEquals("fixture-test", restored.getTestSuite().getTestReferences().getFirst().getTestId());

      assertEquals(1, restored.getTests().size());
      assertEquals("fixture-test", restored.getTests().getFirst().getId());
      assertEquals(1, restored.getTests().getFirst().getSteps().size());
      assertEquals("Execute", restored.getTests().getFirst().getSteps().getFirst().getName());

      assertEquals(1, restored.getInputs().size());
      assertEquals("payload", restored.getInputs().getFirst().getName());
      assertEquals("fixture", restored.getInputs().getFirst().getValue());

      assertEquals(1, restored.getSystemsUnderTest().size());
      SystemUnderTest restoredSut = restored.getSystemsUnderTest().getFirst();
      assertEquals("sut-fixture", restoredSut.getSystemIdentification().getName());
      assertTrue(restoredSut.getHostNames().contains("sut.fixture.local"));

      AccessControlList accessControlList = restored.getAccessControlList();
      assertFalse(accessControlList.isPublic());
      assertTrue(accessControlList.getOwners().contains("owner-1"));
      assertEquals("access-key", accessControlList.getReadAccessKey());
   }
}
