package net.ihe.gazelle.user.management.api.interlay.organization;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class OrganizationCreationRequestTest {

    @Test
    void testSerializeDeserialize() {
        String json = "{\"shortname\":\"JD\",\"name\":\"John Doe\"}";
        TextSerDes serDes = new JacksonSerDes(new ObjectMapper());
        OrganizationCreationRequest organizationCreationRequest = serDes.deserialize(json.getBytes(), OrganizationCreationRequest.class);
        assertEquals("John Doe", organizationCreationRequest.getName());
        String serialized = serDes.serializeAsString(organizationCreationRequest);
        assertEquals(json, serialized);
    }

    @Test
    void testEqualsAndHashcode() {
        EqualsVerifier.simple().forClass(OrganizationCreationRequest.class).verify();
    }
}
