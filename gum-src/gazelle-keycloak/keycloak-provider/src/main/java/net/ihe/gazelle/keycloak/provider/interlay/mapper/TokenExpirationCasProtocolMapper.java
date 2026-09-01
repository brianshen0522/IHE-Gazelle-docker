package net.ihe.gazelle.keycloak.provider.interlay.mapper;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.cas.mappers.AbstractCASProtocolMapper;
import org.keycloak.provider.ProviderConfigProperty;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TokenExpirationCasProtocolMapper extends AbstractCASProtocolMapper {

    public static final String PROVIDER_ID = "cas-token-expiration";

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return new ArrayList<>();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "CAS Token expiration";
    }

    @Override
    public String getHelpText() {
        return "Provide token expiration information.";
    }

    @Override
    public void setAttribute(Map<String, Object> attributes, ProtocolMapperModel mappingModel, UserSessionModel userSession,
                             KeycloakSession session, ClientSessionContext clientSessionCt) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        // Retrieve the client session idle timeout from the realm settings
        int clientIdleTimeoutSeconds = session.getContext().getRealm().getClientSessionIdleTimeout();

        // Add two attributes to the ticket (ClientIdleTimeout becomes ticket_session_max_duration)
        attributes.put("authentication_timestamp", timestamp);
        attributes.put("ticket_session_max_duration_seconds", clientIdleTimeoutSeconds);
    }
}