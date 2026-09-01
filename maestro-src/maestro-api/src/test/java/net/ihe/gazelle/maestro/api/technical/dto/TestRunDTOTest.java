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

import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.property.PropertyResolver;
import net.ihe.gazelle.maestro.api.business.property.UnsupportedTestProperty;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.maestro.api.technical.dto.property.PropertyDTO;
import net.ihe.gazelle.maestro.api.technical.dto.testrun.TestRunDTO;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import net.ihe.gazelle.modelmarshaller.technical.dto.SubTypingDTO;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.DeserializationException;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

class TestRunDTOTest {

    private static final TextSerDes SER_DES = new JacksonSerDes();

    @Test
    void shouldDeserializeAndSerialize() {
        List<TestRun> testRuns = assertDoesNotThrow(() -> Arrays.stream(SER_DES.deserialize(getTestRun("valid"), TestRunDTO[].class))
                .map(TestRunDTO::getBusinessObject)
                .toList());

        assertEquals(1, testRuns.size());
        assertEquals(2, testRuns.getFirst().getTest().getSteps().size());

        testRuns.forEach(this::resolveReferencedProperties);

        List<TestRunDTO> testRunDTOS = testRuns.stream().map(TestRunDTO::new).toList();
        assertDoesNotThrow(() -> SER_DES.serializeAsString(testRunDTOS));
    }

    @Test
    void shouldSerializeUnresolvedByteArrayReferences() {
        TestRun testRun = new TestRun()
              .setTest(new net.ihe.gazelle.maestro.api.business.test.Test()
                    .setId("byte-array-reference-test")
                    .setName("byte-array-reference-test")
                    .setSteps(List.of(
                          new Step()
                                .setName("step")
                                .setType("MOCK")
                                .setProperties(List.of(
                                      new ByteArrayProperty("contentToValidate", "${inputFile1}")
                                ))
                    )))
              .setAccessControlList(new AccessControlList())
              .setInputs(List.of(new ByteArrayProperty("inputFile1", "payload".getBytes(StandardCharsets.UTF_8))));
        List<TestRunDTO> testRunDTOS = List.of(new TestRunDTO(testRun));

        assertDoesNotThrow(() -> SER_DES.serializeAsString(testRunDTOS));
    }

    @Test
    void shouldFailWhenPropertyTypeUnknownInPayload() throws IOException {
        String json = getTestRun("wrong_property_type");
        DeserializationException exception = assertThrows(DeserializationException.class,
                () -> SER_DES.deserialize(json, TestRunDTO[].class));

        assertInstanceOf(InvalidTypeIdException.class, exception.getCause());
    }

    @Test
    void shouldFailWhenPropertyTypeUnsupportedForDtoConversion() {
        Property property = new UnsupportedTestProperty("testProperty", "value");

        assertThrows(NoSuchElementException.class, () -> SubTypingDTO.fromBusinessObject(property));
    }

    @Test
    void shouldFailWhenReferencePropertyTypeUnsupported() {
        Property property = new UnsupportedTestProperty("testProperty", "${reference}");

        assertThrows(NoSuchElementException.class, () -> SubTypingDTO.fromBusinessObject(property));
    }

    @Test
    void shouldFailOnInvalidDateFormat() {
        String json = """
                {
                  "name": "dateProperty",
                  "value": "20/05/25",
                  "type": "DATE"
                }
                """;
        DeserializationException de = assertThrows(DeserializationException.class,
                () -> SER_DES.deserialize(json, PropertyDTO.class));
        assertEquals(DateTimeParseException.class, de.getCause().getCause().getClass());
        DateTimeParseException iae = (DateTimeParseException) de.getCause().getCause();
        assertThat(iae.getMessage(), containsString("Text '20/05/25' could not be parsed"));
    }

    private static String getTestRun(String name) throws IOException {
        return Files.readString(Path.of("src/test/resources/testRun/" + name + ".json"));
    }

    private void resolveReferencedProperties(TestRun testRun) {
        List<Property> aggregatedProperties = new ArrayList<>();
        if (testRun.getInputs() != null) {
            aggregatedProperties.addAll(testRun.getInputs());
        }
        testRun.getTest().getSteps().forEach(step -> {
            List<Property> properties = step.getProperties();
            if (properties != null) {
                aggregatedProperties.addAll(properties);
            }
        });
        PropertyResolver.resolveProperties(aggregatedProperties);
    }
}
