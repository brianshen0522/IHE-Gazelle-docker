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

import net.ihe.gazelle.maestro.api.business.property.IntegerProperty;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.TestReference;
import net.ihe.gazelle.maestro.api.technical.dto.property.IntegerPropertyDTO;
import net.ihe.gazelle.maestro.api.technical.dto.property.PropertyDTO;
import net.ihe.gazelle.maestro.api.technical.dto.property.StringPropertyDTO;
import net.ihe.gazelle.maestro.api.technical.dto.test.TestReferenceDTO;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestReferenceDTOTest {

   private static final TextSerDes SER_DES = new JacksonSerDes();
   private static final Path TEST_REFERENCE_JSON = Path.of("src/test/resources/test/test-reference.json");

   @Test
   void shouldExposeBusinessProperties() {
      TestReference testReference = new TestReference()
            .setTestId("test-123")
            .setProperties(List.of(
                  new StringProperty("endpoint", "http://example.com"),
                  new IntegerProperty("retries", 3)
            ));

      TestReferenceDTO dto = new TestReferenceDTO(testReference);

      assertEquals("test-123", dto.getTestId());

      List<PropertyDTO> properties = dto.getProperties();
      assertEquals(2, properties.size());

      StringPropertyDTO stringPropertyDTO = properties.stream()
            .filter(StringPropertyDTO.class::isInstance)
            .map(StringPropertyDTO.class::cast)
            .findFirst()
            .orElseThrow();
      assertEquals("endpoint", stringPropertyDTO.getName());
      assertEquals("http://example.com", stringPropertyDTO.getValue());

      IntegerPropertyDTO integerPropertyDTO = properties.stream()
            .filter(IntegerPropertyDTO.class::isInstance)
            .map(IntegerPropertyDTO.class::cast)
            .findFirst()
            .orElseThrow();
      assertEquals("retries", integerPropertyDTO.getName());
      assertEquals(3, integerPropertyDTO.getValue());
   }

   @Test
   void shouldUpdateBusinessObjectFromPropertyDtos() {
      TestReferenceDTO dto = new TestReferenceDTO();

      dto.setTestId("test-456");

      StringPropertyDTO stringPropertyDTO = new StringPropertyDTO(new StringProperty("logLevel", "INFO"));
      IntegerPropertyDTO integerPropertyDTO = new IntegerPropertyDTO(new IntegerProperty("attempts", 5));
      dto.setProperties(List.of(stringPropertyDTO, integerPropertyDTO));

      TestReference businessObject = dto.getBusinessObject();
      assertEquals("test-456", businessObject.getTestId());
      assertEquals(2, businessObject.getProperties().size());

      assertTrue(businessObject.getProperties().stream().anyMatch(property ->
            "logLevel".equals(property.getName()) && "INFO".equals(property.getValue())));
      assertTrue(businessObject.getProperties().stream().anyMatch(property ->
            "attempts".equals(property.getName()) && (Integer) property.getValue() == 5));

      dto.setProperties(null);
      assertNotNull(businessObject.getProperties());
      assertTrue(businessObject.getProperties().isEmpty());
   }

   @Test
   @SuppressWarnings("unchecked")
   void shouldSerializeToJson() {
      TestReference source = new TestReference()
            .setTestId("serialize-test")
            .setProperties(List.of(
                  new StringProperty("url", "https://example.org"),
                  new IntegerProperty("timeout", 15)
            ));

      String json = assertDoesNotThrow(() -> SER_DES.serializeAsString(new TestReferenceDTO(source)));
      Map<String, Object> payload = assertDoesNotThrow(() -> SER_DES.deserialize(json, Map.class));

      assertEquals("serialize-test", payload.get("testId"));
      List<Map<String, Object>> properties = (List<Map<String, Object>>) payload.get("properties");
      assertEquals(2, properties.size());

      Map<String, Object> urlProperty = properties.getFirst();
      assertEquals("url", urlProperty.get("name"));
      assertEquals("STRING", urlProperty.get("type"));
      assertEquals("https://example.org", urlProperty.get("value"));
   }

   @Test
   void shouldDeserializeFromFixtureJson() {
      String json = assertDoesNotThrow(() -> Files.readString(TEST_REFERENCE_JSON));
      TestReferenceDTO dto = assertDoesNotThrow(() -> SER_DES.deserialize(json, TestReferenceDTO.class));

      TestReference restored = dto.getBusinessObject();
      assertEquals("fixture-reference", restored.getTestId());
      assertEquals(2, restored.getProperties().size());
      assertTrue(restored.getProperties().stream().anyMatch(property ->
            "endpoint".equals(property.getName()) && "https://fixture.example".equals(property.getValue())));
      assertTrue(restored.getProperties().stream().anyMatch(property ->
            "retries".equals(property.getName()) && (Integer) property.getValue() == 2));
   }
}
