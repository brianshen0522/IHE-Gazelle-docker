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

package net.ihe.gazelle.itb.gateway.technical.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbReporting;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbSystem;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbTestSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * JAX-RS reader/writer mapping ITB callback payloads to {@link ItbReporting}.
 */
@Provider
@Consumes("application/json")
@Produces("application/json")
public class ItbReportingMessageBodyReader implements MessageBodyReader<ItbReporting>, MessageBodyWriter<ItbReporting> {

    /**
     * JSON field carrying raw value.
     */
    public static final String VALUE = "value";
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates the message body reader/writer.
     */
    public ItbReportingMessageBodyReader() {
      // Default constructor
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == ItbReporting.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItbReporting readFrom(Class<ItbReporting> type, Type genericType, Annotation[] annotations,
                                 MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                                 InputStream entityStream) throws IOException, WebApplicationException {

        JsonNode rootNode = mapper.readTree(entityStream);

        ItbSystem systemDTO = null;
        ItbTestSession testSessionDTO = null;
        String testReport = null;

        for (JsonNode inputNode : rootNode.path("inputs")) {
            String name = inputNode.path("name").asText();

            switch (name) {
                case "system":
                    systemDTO = parseSystemDTO(inputNode.path("item"));
                    break;
                case "testSession":
                    testSessionDTO = parseTestSessionDTO(inputNode.path("item"));
                    break;
                case "testReport":
                    testReport = inputNode.path(VALUE).asText();
                    break;
                default: // ignore unknown fields
                    break;
            }
        }

        return new ItbReporting(systemDTO, testSessionDTO, testReport);
    }

    private ItbSystem parseSystemDTO(JsonNode itemNode) {
        Long id = null;
        String shortName = null;
        String fullName = null;

        for (JsonNode node : itemNode) {
            String name = node.path("name").asText();

            switch (name) {
                case "id":
                    id = Long.parseLong(node.path(VALUE).asText());
                    break;
                case "shortName":
                    shortName = node.path(VALUE).asText();
                    break;
                case "fullName":
                    fullName = node.path(VALUE).asText();
                    break;
                default: // ignore unknown fields
                    break;
            }
        }

        return new ItbSystem(id, shortName, fullName);
    }

    private ItbTestSession parseTestSessionDTO(JsonNode itemNode) {
        String testSuiteId = null;
        String testCaseId = null;
        String testSessionId = null;

        for (JsonNode node : itemNode) {
            String name = node.path("name").asText();

            switch (name) {
                case "testSuiteIdentifier":
                    testSuiteId = node.path(VALUE).asText();
                    break;
                case "testCaseIdentifier":
                    testCaseId = node.path(VALUE).asText();
                    break;
                case "testSessionIdentifier":
                    testSessionId = node.path(VALUE).asText();
                    break;
                default: // ignore unknown fields
                    break;
            }
        }

        return new ItbTestSession(testSuiteId, testCaseId, testSessionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == ItbReporting.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeTo(ItbReporting itbReporting, Class<?> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        mapper.writeValue(entityStream, itbReporting);
    }
}
