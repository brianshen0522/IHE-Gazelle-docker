package net.ihe.gazelle.user.management.api.interlay.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class UserCreationRequestTest {


    @Test
    void testSerializeDeserialize() {
        String json = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\"}";
        TextSerDes serDes = new JacksonSerDes(new ObjectMapper());
        UserCreationRequest userCreationRequest = serDes.deserialize(json.getBytes(), UserCreationRequest.class);
        assertEquals("John", userCreationRequest.getFirstName());
        String serialized = serDes.serializeAsString(userCreationRequest);
        assertEquals(json, serialized);
    }

    @Test
    void testEqualsAndHashcode() {
        EqualsVerifier.simple().forClass(UserCreationRequest.class).verify();
    }
}
