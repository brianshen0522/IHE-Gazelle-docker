package net.ihe.gazelle.keycloak.provider;

import net.ihe.gazelle.keycloak.provider.utils.KeycloakAdminOperator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.*;
import org.keycloak.representations.info.SpiInfoRepresentation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GazelleKeycloakConfigurationIT {

    private static RealmResource gazelleRealm;
    private static Keycloak keycloak;

    @BeforeAll
    public static void setUp() {
        KeycloakAdminOperator keycloakAuthentication = new KeycloakAdminOperator();
        gazelleRealm = keycloakAuthentication.getRealmGazelle();
        keycloak = keycloakAuthentication.getKeycloakInstance();
    }

    @Test
    void checkSMTPConfiguration() {
        Map<String, String> smtpConfiguration = gazelleRealm.toRepresentation().getSmtpServer();
        assertEquals("mailhog", smtpConfiguration.get("host"));
        assertEquals("1025", smtpConfiguration.get("port"));
        assertEquals("test", smtpConfiguration.get("user"));
        assertEquals("no-reply@localhost", smtpConfiguration.get("from"));
    }

    @Test
    void checkClientCasCommonsPresence() {
        List<ClientRepresentation> clientResources = gazelleRealm.clients().findByClientId("cas-commons");
        assertFalse(clientResources.isEmpty());
        assertEquals("cas-commons", clientResources.getFirst().getClientId());
        assertTrue(clientResources.getFirst().getRedirectUris().contains("https://localhost/gazelle-token-service/*"));
        assertTrue(clientResources.getFirst().getRedirectUris().contains("https://localhost/GazelleFhirValidator/*"));
    }

    @Test
    void checkEventListenerPresence() {
        RealmEventsConfigRepresentation realmEventsConfigRepresentation = gazelleRealm.getRealmEventsConfig();
        List<String> eventsListeners = realmEventsConfigRepresentation.getEventsListeners();

        assertTrue(eventsListeners.contains("jboss-logging"));
        assertTrue(eventsListeners.contains("gzl-last-login"));
        assertTrue(eventsListeners.contains("gzl-inactive-login"));
        assertTrue(eventsListeners.contains("gzl-block-login"));
        assertTrue(eventsListeners.contains("gzl-update-password"));
    }

    @Test
    void checkLoginActionsActivation() {
        List<RequiredActionProviderRepresentation> requiredActionProviderRepresentations = gazelleRealm.flows().getRequiredActions();
        List<String> requiredActionAliases = requiredActionProviderRepresentations.stream().filter(RequiredActionProviderRepresentation::isEnabled).map(RequiredActionProviderRepresentation::getProviderId).toList();

        assertTrue(requiredActionAliases.contains("UPDATE_PASSWORD"));
        assertTrue(requiredActionAliases.contains("GAZELLE_TERMS_OF_SERVICE"));
    }

    @Test
    void checkGazelleUserProviderPresence() {
        Map<String, SpiInfoRepresentation> clientResources = keycloak.serverInfo().getInfo().getProviders();
        assertFalse(clientResources.isEmpty());

        // Check if gazelle storage provider is present
        Set<String> storageProvider = clientResources.get("storage").getProviders().keySet();
        assertTrue(storageProvider.contains("gazelle-user-provider"));
    }

    @Test
    void checkCasClientScopePresence() {
        List<ClientScopeRepresentation> clientScopeRepresentations = gazelleRealm.clientScopes().findAll();
        List<String> clientScopes = clientScopeRepresentations.stream().map(ClientScopeRepresentation::getName).toList();
        assertTrue(clientScopes.contains("cas-client-scope"));
        ClientScopeRepresentation casClientScope = clientScopeRepresentations.stream().filter(clientScope -> clientScope.getName().equals("cas-client-scope")
        ).toList().getFirst();
        List<String> protocolMappers = casClientScope.getProtocolMappers().stream().map(ProtocolMapperRepresentation::getName).toList();
        assertTrue(protocolMappers.contains("last name"));
        assertTrue(protocolMappers.contains("gazelle_roles"));
        assertTrue(protocolMappers.contains("first name"));
        assertTrue(protocolMappers.contains("email"));
        assertTrue(protocolMappers.contains("institution_keyword"));
        assertTrue(protocolMappers.contains("ticket_session_expiration"));
        assertTrue(protocolMappers.contains("gazelle_roles"));
    }

    @Test
    void checkM2mClientScopePresence() {
        List<ClientScopeRepresentation> clientScopeRepresentations = gazelleRealm.clientScopes().findAll();
        List<String> clientScopes = clientScopeRepresentations.stream().map(ClientScopeRepresentation::getName).toList();
        assertTrue(clientScopes.contains("microprofile-jwt"));
        assertTrue(clientScopes.contains("m2m-client-scope"));
        ClientScopeRepresentation m2mClientScope = clientScopeRepresentations.stream().filter(clientScope -> clientScope.getName().equals("m2m-client-scope")
        ).toList().getFirst();
        List<String> protocolMappers = m2mClientScope.getProtocolMappers().stream().map(ProtocolMapperRepresentation::getName).toList();
        assertTrue(protocolMappers.contains("lastName"));
        assertTrue(protocolMappers.contains("firstName"));
        assertTrue(protocolMappers.contains("email"));
        assertTrue(protocolMappers.contains("authn_method"));
        assertTrue(protocolMappers.contains("roles"));

        // Check audience mapper
        Optional<ProtocolMapperRepresentation> audienceMapper = m2mClientScope.getProtocolMappers().stream().filter(mapper -> mapper.getName().equals("audience")).findFirst();
        assertTrue(audienceMapper.isPresent());
        assertEquals("audience", audienceMapper.get().getName());
        assertEquals("http://localhost:8080", audienceMapper.get().getConfig().get("included.custom.audience"));
    }

    @Test
    void checkOIDCClientScopePresence() {
        List<ClientScopeRepresentation> clientScopeRepresentations = gazelleRealm.clientScopes().findAll();
        List<String> clientScopes = clientScopeRepresentations.stream().map(ClientScopeRepresentation::getName).toList();
        assertTrue(clientScopes.contains("oidc-client-scope"));
        ClientScopeRepresentation m2mClientScope = clientScopeRepresentations.stream().filter(clientScope -> clientScope.getName().equals("oidc-client-scope")
        ).toList().getFirst();
        List<String> protocolMappers = m2mClientScope.getProtocolMappers().stream().map(ProtocolMapperRepresentation::getName).toList();
        assertTrue(protocolMappers.contains("lastName"));
        assertTrue(protocolMappers.contains("firstName"));
        assertTrue(protocolMappers.contains("authn_method"));
        assertTrue(protocolMappers.contains("email"));

        // Check audience mapper
        Optional<ProtocolMapperRepresentation> audienceMapper = m2mClientScope.getProtocolMappers().stream().filter(mapper -> mapper.getName().equals("audience")).findFirst();
        assertTrue(audienceMapper.isPresent());
        assertEquals("audience", audienceMapper.get().getName());
        assertEquals("http://localhost:8080", audienceMapper.get().getConfig().get("included.custom.audience"));
    }


    @Test
    void checkSSOSessionsTimeoutConfigurations() {
        // Check if gazelle session configuration are correct
        RealmRepresentation realmRepresentation = gazelleRealm.toRepresentation();
        assertEquals(2400, realmRepresentation.getSsoSessionIdleTimeout());
        assertEquals(43200, realmRepresentation.getSsoSessionMaxLifespan());
        assertEquals(1200, realmRepresentation.getClientSessionIdleTimeout());
        assertEquals(43200, realmRepresentation.getClientSessionMaxLifespan());
    }

    @Test
    void testGazelleBrowserAuthenticationFlow() {
        List<AuthenticationFlowRepresentation> authenticationFlows = gazelleRealm.flows().getFlows();
        Optional<AuthenticationFlowRepresentation> authenticationFlowRepresentation = authenticationFlows.stream().filter(authFlowRepresentation -> "gazelle-browser".equals(authFlowRepresentation.getAlias())).findFirst();
        assertTrue(authenticationFlowRepresentation.isPresent());
        AuthenticationFlowRepresentation flowRepresentation = authenticationFlowRepresentation.get();
        assertEquals("gazelle-browser", flowRepresentation.getAlias());
    }
}