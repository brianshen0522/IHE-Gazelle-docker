package net.ihe.gazelle.validation.gateway.evs.technical.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class ValidationDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializesLegacyObjectField() throws Exception {
        String json = """
              {
                "validationService": {
                  "name": "mock-validation-service",
                  "validator": "ITI-18_request"
                },
                "object": [
                  {
                    "originalFileName": "payload.xml",
                    "content": "dGVzdA=="
                  }
                ]
              }
              """;

        ValidationDTO dto = objectMapper.readValue(json, ValidationDTO.class);

        assertThat(dto.getValidationService().getName(), is("mock-validation-service"));
        assertThat(dto.getValidationService().getValidator(), is("ITI-18_request"));
        assertThat(dto.getObjects(), hasSize(1));
        assertThat(dto.getObjects().getFirst().getOriginalFileName(), is("payload.xml"));
        assertThat(new String(dto.getObjects().getFirst().getContent(), StandardCharsets.UTF_8), is("test"));
    }

    @Test
    void deserializesClientObjectsAlias() throws Exception {
        String json = """
              {
                "validationService": {
                  "name": "mock-validation-service",
                  "validator": "ITI-18_request"
                },
                "objects": [
                  {
                    "originalFileName": "payload.xml",
                    "content": "dGVzdA=="
                  }
                ]
              }
              """;

        ValidationDTO dto = objectMapper.readValue(json, ValidationDTO.class);

        assertThat(dto.getValidationService().getName(), is("mock-validation-service"));
        assertThat(dto.getValidationService().getValidator(), is("ITI-18_request"));
        assertThat(dto.getObjects(), hasSize(1));
        assertThat(dto.getObjects().getFirst().getOriginalFileName(), is("payload.xml"));
        assertThat(new String(dto.getObjects().getFirst().getContent(), StandardCharsets.UTF_8), is("test"));
    }
}
