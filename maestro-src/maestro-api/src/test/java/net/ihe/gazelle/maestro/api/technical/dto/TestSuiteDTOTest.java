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

import net.ihe.gazelle.maestro.api.business.test.SupportedInput;
import net.ihe.gazelle.maestro.api.business.test.SupportedTextInput;
import net.ihe.gazelle.maestro.api.business.test.TestReference;
import net.ihe.gazelle.maestro.api.business.test.TestSuite;
import net.ihe.gazelle.maestro.api.technical.dto.test.SupportedInputDTO;
import net.ihe.gazelle.maestro.api.technical.dto.test.SupportedTextInputDTO;
import net.ihe.gazelle.maestro.api.technical.dto.test.TestReferenceDTO;
import net.ihe.gazelle.maestro.api.technical.dto.test.TestSuiteDTO;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestSuiteDTOTest {

   private static final TextSerDes SER_DES = new JacksonSerDes();
   private static final Path TEST_SUITE_JSON = Path.of("src/test/resources/test/test-suite.json");

   @Test
   void shouldExposeNestedStateFromBusinessObject() {
      TestReference testReference = new TestReference().setTestId("test-1");

      SupportedTextInput supportedTextInput = new SupportedTextInput()
            .setId("text-input")
            .setLabel("Text")
            .setRequired(true)
            .setPossibleValues(List.of("A", "B"))
            .setDefaultValue("A");

      TestSuite testSuite = new TestSuite()
            .setId("suite-1")
            .setName("Sample suite")
            .setTestReferences(List.of(testReference))
            .setSupportedInputs(List.of(supportedTextInput));

      TestSuiteDTO dto = new TestSuiteDTO(testSuite);

      assertEquals("suite-1", dto.getId());
      assertEquals("Sample suite", dto.getName());

      List<TestReferenceDTO> referenceDTOs = dto.getTestReferences();
      assertEquals(1, referenceDTOs.size());
      assertEquals("test-1", referenceDTOs.getFirst().getTestId());

      List<SupportedInputDTO> supportedInputs = dto.getSupportedInputs();
      assertEquals(1, supportedInputs.size());
      SupportedInputDTO<?> supportedInputDTO = supportedInputs.getFirst();
      assertInstanceOf(SupportedTextInputDTO.class, supportedInputDTO);
      SupportedTextInputDTO textInputDTO = (SupportedTextInputDTO) supportedInputDTO;
      assertEquals("text-input", textInputDTO.getId());
      assertEquals("Text", textInputDTO.getLabel());
      assertEquals(List.of("A", "B"), textInputDTO.getPossibleValues());
      assertEquals("A", textInputDTO.getDefaultValue());
      assertTrue(textInputDTO.isRequired());
   }

   @Test
   void shouldPopulateBusinessObjectFromNestedDtos() {
      TestSuiteDTO dto = new TestSuiteDTO();
      dto.setId("suite-2");
      dto.setName("Another suite");

      TestReferenceDTO referenceDTO = new TestReferenceDTO();
      referenceDTO.setTestId("test-99");
      dto.setTestReferences(List.of(referenceDTO));

      SupportedTextInputDTO supportedTextInputDTO = new SupportedTextInputDTO();
      supportedTextInputDTO.setId("text-input");
      supportedTextInputDTO.setLabel("Text");
      supportedTextInputDTO.setRequired(true);
      supportedTextInputDTO.setPossibleValues(List.of("X", "Y"));
      supportedTextInputDTO.setDefaultValue("X");
      dto.setSupportedInputs(List.of(supportedTextInputDTO));

      TestSuite businessObject = dto.getBusinessObject();

      assertEquals("suite-2", businessObject.getId());
      assertEquals("Another suite", businessObject.getName());
      assertEquals(1, businessObject.getTestReferences().size());
      assertEquals("test-99", businessObject.getTestReferences().getFirst().getTestId());

      assertEquals(1, businessObject.getSupportedInputs().size());
      SupportedInput supportedInput = businessObject.getSupportedInputs().getFirst();
      assertInstanceOf(SupportedTextInput.class, supportedInput);
      SupportedTextInput textInput = (SupportedTextInput) supportedInput;
      assertEquals(List.of("X", "Y"), textInput.getPossibleValues());
      assertEquals("X", textInput.getDefaultValue());

      dto.setTestReferences(null);
      assertTrue(businessObject.getTestReferences().isEmpty());

      dto.setSupportedInputs(List.of());
      assertNotNull(businessObject.getSupportedInputs());
      assertTrue(businessObject.getSupportedInputs().isEmpty());
   }

   @Test
   @SuppressWarnings("unchecked")
   void shouldSerializeToJson() {
      SupportedTextInput supportedInput = new SupportedTextInput()
            .setId("input-id")
            .setLabel("Input label")
            .setRequired(false)
            .setPossibleValues(List.of("one", "two"))
            .setDefaultValue("one");

      TestSuite source = new TestSuite()
            .setId("suite-serialize")
            .setName("Serialize Suite")
            .setTestReferences(List.of(new TestReference().setTestId("test-serialize")))
            .setSupportedInputs(List.of(supportedInput));

      String json = assertDoesNotThrow(() -> SER_DES.serializeAsString(new TestSuiteDTO(source)));
      Map<String, Object> payload = assertDoesNotThrow(() -> SER_DES.deserialize(json, Map.class));

      assertEquals("suite-serialize", payload.get("id"));
      assertEquals("Serialize Suite", payload.get("name"));

      List<Map<String, Object>> testReferences = (List<Map<String, Object>>) payload.get("testReferences");
      assertEquals(1, testReferences.size());
      assertEquals("test-serialize", testReferences.getFirst().get("testId"));

      List<Map<String, Object>> supportedInputs = (List<Map<String, Object>>) payload.get("supportedInputs");
      assertEquals(1, supportedInputs.size());
      Map<String, Object> inputPayload = supportedInputs.getFirst();
      assertEquals("TEXT", inputPayload.get("type"));
      assertEquals("Input label", inputPayload.get("label"));
   }

   @Test
   void shouldDeserializeFromFixtureJson() {
      String json = assertDoesNotThrow(() -> Files.readString(TEST_SUITE_JSON));
      TestSuiteDTO dto = assertDoesNotThrow(() -> SER_DES.deserialize(json, TestSuiteDTO.class));

      TestSuite restored = dto.getBusinessObject();
      assertEquals("suite-fixture", restored.getId());
      assertEquals("Fixture Suite", restored.getName());
      assertEquals(1, restored.getTestReferences().size());
      assertEquals("fixture-test", restored.getTestReferences().getFirst().getTestId());

      assertEquals(1, restored.getSupportedInputs().size());
      SupportedInput supportedInput = restored.getSupportedInputs().getFirst();
      assertInstanceOf(SupportedTextInput.class, supportedInput);
      SupportedTextInput textInput = (SupportedTextInput) supportedInput;
      assertEquals(List.of("foo", "bar"), textInput.getPossibleValues());
      assertEquals("foo", textInput.getDefaultValue());
      assertTrue(textInput.isRequired());
   }
}
