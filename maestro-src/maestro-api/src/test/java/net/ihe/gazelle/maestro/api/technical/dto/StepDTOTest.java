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

package net.ihe.gazelle.maestro.api.technical.dto;

import net.ihe.gazelle.maestro.api.business.property.IntegerProperty;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.technical.dto.property.IntegerPropertyDTO;
import net.ihe.gazelle.maestro.api.technical.dto.property.PropertyDTO;
import net.ihe.gazelle.maestro.api.technical.dto.property.StringPropertyDTO;
import net.ihe.gazelle.maestro.api.technical.dto.test.StepDTO;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StepDTOTest {

    private static final TextSerDes SER_DES = new JacksonSerDes();
    private static final Path STEP_JSON = Path.of("src/test/resources/test/step.json");

    @Test
    void shouldExposeBusinessProperties() {
        Step step = new Step()
                .setName("test-step")
                .setType("extraction-step")
                .setProperties(List.of(
                        new StringProperty("xpath", "//root/element"),
                        new IntegerProperty("timeout", 30)
                ));
        step.addOutputMapping("result", "extractedValue");

        StepDTO dto = new StepDTO(step);

        assertEquals("test-step", dto.getName());
        assertEquals("extraction-step", dto.getType());

        List<PropertyDTO> properties = dto.getProperties();
        assertEquals(2, properties.size());

        StringPropertyDTO stringPropertyDTO = properties.stream()
                .filter(StringPropertyDTO.class::isInstance)
                .map(StringPropertyDTO.class::cast)
                .findFirst()
                .orElseThrow();
        assertEquals("xpath", stringPropertyDTO.getName());
        assertEquals("//root/element", stringPropertyDTO.getValue());

        IntegerPropertyDTO integerPropertyDTO = properties.stream()
                .filter(IntegerPropertyDTO.class::isInstance)
                .map(IntegerPropertyDTO.class::cast)
                .findFirst()
                .orElseThrow();
        assertEquals("timeout", integerPropertyDTO.getName());
        assertEquals(30, integerPropertyDTO.getValue());

        List<String> outputMappings = dto.getOutputMappings();
        assertEquals(1, outputMappings.size());
        assertEquals("result:extractedValue", outputMappings.getFirst());
    }

    @Test
    void shouldUpdateBusinessObjectFromPropertyDtos() {
        StepDTO dto = new StepDTO();

        dto.setName("conversion-step");
        dto.setType("base64-encoder");

        StringPropertyDTO stringPropertyDTO = new StringPropertyDTO(new StringProperty("encoding", "UTF-8"));
        IntegerPropertyDTO integerPropertyDTO = new IntegerPropertyDTO(new IntegerProperty("bufferSize", 1024));
        dto.setProperties(List.of(stringPropertyDTO, integerPropertyDTO));

        dto.setOutputMappings(List.of("encoded:outputValue", "size:outputSize"));

        Step businessObject = dto.getBusinessObject();
        assertEquals("conversion-step", businessObject.getName());
        assertEquals("base64-encoder", businessObject.getType());
        assertEquals(2, businessObject.getProperties().size());

        assertTrue(businessObject.getProperties().stream().anyMatch(property ->
                "encoding".equals(property.getName()) && "UTF-8".equals(property.getValue())));
        assertTrue(businessObject.getProperties().stream().anyMatch(property ->
                "bufferSize".equals(property.getName()) && (Integer) property.getValue() == 1024));

        Map<String, String> outputMappings = businessObject.getOutputMappings();
        assertEquals(2, outputMappings.size());
        assertEquals("outputValue", outputMappings.get("encoded"));
        assertEquals("outputSize", outputMappings.get("size"));
    }

    @Test
    void shouldHandleNullProperties() {
        StepDTO dto = new StepDTO();
        dto.setName("minimal-step");
        dto.setType("simple-step");
        dto.setProperties(null);

        Step businessObject = dto.getBusinessObject();
        assertNotNull(businessObject.getProperties());
        assertTrue(businessObject.getProperties().isEmpty());
    }

    @Test
    void shouldHandleNullOutputMappings() {
        StepDTO dto = new StepDTO();
        dto.setName("no-mapping-step");
        dto.setType("simple-step");
        dto.setOutputMappings(null);

        Step businessObject = dto.getBusinessObject();
        assertNotNull(businessObject.getOutputMappings());
        assertTrue(businessObject.getOutputMappings().isEmpty());
    }

    @Test
    void shouldHandleEmptyOutputMappings() {
        StepDTO dto = new StepDTO();
        dto.setName("empty-mapping-step");
        dto.setType("simple-step");
        dto.setOutputMappings(List.of());

        Step businessObject = dto.getBusinessObject();
        assertNotNull(businessObject.getOutputMappings());
        assertTrue(businessObject.getOutputMappings().isEmpty());
    }

    @Test
    void shouldThrowExceptionForInvalidOutputMapping() {
        StepDTO dto = new StepDTO();
        List<String> invalidMapping = List.of("invalidMapping");
        List<String> tooManyDelimiters = List.of("key:value:extra");

        // Missing delimiter
        assertThrows(IllegalArgumentException.class, () ->
                dto.setOutputMappings(invalidMapping));

        // Too many delimiters
        assertThrows(IllegalArgumentException.class, () ->
                dto.setOutputMappings(tooManyDelimiters));
    }

    @Test
    void shouldHandleMultipleOutputMappings() {
        StepDTO dto = new StepDTO();
        dto.setName("multi-mapping-step");
        dto.setType("complex-step");
        dto.setOutputMappings(List.of(
                "output1:variable1",
                "output2:variable2",
                "output3:variable3"
        ));

        Step businessObject = dto.getBusinessObject();
        Map<String, String> mappings = businessObject.getOutputMappings();

        assertEquals(3, mappings.size());
        assertEquals("variable1", mappings.get("output1"));
        assertEquals("variable2", mappings.get("output2"));
        assertEquals("variable3", mappings.get("output3"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSerializeToJson() {
        Step source = new Step()
                .setName("serialize-step")
                .setType("assertion-step")
                .setProperties(List.of(
                        new StringProperty("expression", "response.status == 200"),
                        new IntegerProperty("retries", 3)
                ));
        source.addOutputMapping("assertionResult", "testResult");

        String json = assertDoesNotThrow(() -> SER_DES.serializeAsString(new StepDTO(source)));
        Map<String, Object> payload = assertDoesNotThrow(() -> SER_DES.deserialize(json, Map.class));

        assertEquals("serialize-step", payload.get("name"));
        assertEquals("assertion-step", payload.get("type"));

        List<Map<String, Object>> properties = (List<Map<String, Object>>) payload.get("properties");
        assertEquals(2, properties.size());

        Map<String, Object> expressionProperty = properties.stream()
                .filter(p -> "expression".equals(p.get("name")))
                .findFirst()
                .orElseThrow();
        assertEquals("STRING", expressionProperty.get("type"));
        assertEquals("response.status == 200", expressionProperty.get("value"));

        Map<String, Object> retriesProperty = properties.stream()
                .filter(p -> "retries".equals(p.get("name")))
                .findFirst()
                .orElseThrow();
        assertEquals("INTEGER", retriesProperty.get("type"));
        assertEquals(3, retriesProperty.get("value"));

        List<String> outputMappings = (List<String>) payload.get("outputMappings");
        assertEquals(1, outputMappings.size());
        assertEquals("assertionResult:testResult", outputMappings.getFirst());
    }

    @Test
    void shouldDeserializeFromFixtureJson() {
        String json = assertDoesNotThrow(() -> Files.readString(STEP_JSON));
        StepDTO dto = assertDoesNotThrow(() -> SER_DES.deserialize(json, StepDTO.class));

        Step restored = dto.getBusinessObject();
        assertEquals("validate-response", restored.getName());
        assertEquals("validation-step", restored.getType());
        assertEquals(2, restored.getProperties().size());

        assertTrue(restored.getProperties().stream().anyMatch(property ->
                "expectedStatus".equals(property.getName()) && (Integer) property.getValue() == 200));
        assertTrue(restored.getProperties().stream().anyMatch(property ->
                "endpoint".equals(property.getName()) && "https://test.example.com/api".equals(property.getValue())));

        Map<String, String> mappings = restored.getOutputMappings();
        assertEquals(2, mappings.size());
        assertEquals("actualResponse", mappings.get("responseBody"));
        assertEquals("actualStatus", mappings.get("statusCode"));
    }

    @Test
    void shouldCreateEmptyStep() {
        StepDTO dto = new StepDTO();

        Step businessObject = dto.getBusinessObject();
        assertNotNull(businessObject);
        assertNull(businessObject.getName());
        assertNull(businessObject.getType());
        assertNotNull(businessObject.getProperties());
        assertTrue(businessObject.getProperties().isEmpty());
        assertNotNull(businessObject.getOutputMappings());
        assertTrue(businessObject.getOutputMappings().isEmpty());
    }

    @Test
    void shouldCreateStepWithBusinessObject() {
        Step step = new Step()
                .setName("existing-step")
                .setType("existing-type");

        StepDTO dto = new StepDTO(step);

        assertEquals("existing-step", dto.getName());
        assertEquals("existing-type", dto.getType());
        assertSame(step, dto.getBusinessObject());
    }

    @Test
    void shouldRoundTripSerializationDeserialization() {
        Step original = new Step()
                .setName("roundtrip-step")
                .setType("roundtrip-type")
                .setProperties(List.of(
                        new StringProperty("param1", "value1"),
                        new IntegerProperty("param2", 42)
                ));
        original.addOutputMapping("out1", "var1");
        original.addOutputMapping("out2", "var2");

        StepDTO originalDto = new StepDTO(original);
        String json = assertDoesNotThrow(() -> SER_DES.serializeAsString(originalDto));
        StepDTO deserializedDto = assertDoesNotThrow(() -> SER_DES.deserialize(json, StepDTO.class));

        Step restored = deserializedDto.getBusinessObject();
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getType(), restored.getType());
        assertEquals(original.getProperties().size(), restored.getProperties().size());
        assertEquals(original.getOutputMappings().size(), restored.getOutputMappings().size());

        original.getProperties().forEach(prop ->
            assertTrue(restored.getProperties().stream()
                    .anyMatch(p -> p.getName().equals(prop.getName()) && p.getValue().equals(prop.getValue()))));

        original.getOutputMappings().forEach((key, value) ->
            assertEquals(value, restored.getOutputMappings().get(key)));
    }
}
