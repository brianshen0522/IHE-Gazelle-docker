package net.ihe.gazelle.user.management.commons.interlay.cipher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ihe.gazelle.user.management.api.domain.user.Credentials;

public class CredentialsSerializer {

    private ObjectMapper mapper;

    public CredentialsSerializer() {
        mapper = new ObjectMapper();
    }

    public String mapCredentialsToString(Credentials credentials) {
        if(credentials == null) throw new IllegalArgumentException("Credentials cannot be null");
        try {
            return mapper.writeValueAsString(credentials);

        } catch (JsonProcessingException e) {
            throw new CredentialsSerializerException(e);
        }
    }

    public Credentials mapStringToCredentials(String credentials) {
        if(credentials == null) throw new IllegalArgumentException("Credentials cannot be null");
        try {
            return mapper.readValue(credentials, Credentials.class);
        } catch (JsonProcessingException e) {
            throw new CredentialsSerializerException(e);
        }
    }

}
