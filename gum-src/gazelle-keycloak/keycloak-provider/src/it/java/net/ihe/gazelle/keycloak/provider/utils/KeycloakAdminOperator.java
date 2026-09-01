package net.ihe.gazelle.keycloak.provider.utils;

import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.user.management.api.domain.user.User;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
public class KeycloakAdminOperator {

    private final HttpClient httpClient;
    public Keycloak keycloak;

    private final RealmResource realmGazelle;

    public KeycloakAdminOperator() {
        String keycloakUrl = System.getProperty("it.base.uri","http://localhost") +":28080/";
        keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .grantType(OAuth2Constants.PASSWORD)
                .realm("master")
                .clientId("admin-cli")
                .username("admin")
                .password("admin")
                .resteasyClient(
                        new ResteasyClientBuilderImpl().connectionPoolSize(10).build()
                ).build();
        // Get access token
        keycloak.tokenManager().getAccessToken();
        realmGazelle = keycloak.realm("gazelle");

        httpClient = HttpClient.newBuilder().build();
    }


    public RealmResource getRealmGazelle() {
        return realmGazelle;
    }

    public Keycloak getKeycloakInstance() {
        return keycloak;
    }

    /**
     * Log in a user in keycloak using openId connect protocol
     * @param username the username
     * @param password the password
     * curl --location --request POST '${FQDN}/realms/gazelle/protocol/openid-connect/token'
     *     --header 'Content-Type: application/x-www-form-urlencoded'
     *     --data-urlencode 'username=username' --data-urlencode 'password=password'
     *     --data-urlencode 'grant_type=grant_type' --data-urlencode 'client_id=client_id'
     */
    public HttpResponse<String> logInUser(String username, String password) {
        String clientId = "account";
        try {
            String keycloakUrl = System.getProperty("it.base.uri","http://localhost") +":28080/";
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(new URI(keycloakUrl + "realms/gazelle/protocol/openid-connect/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "username=" + username
                                    + "&password=" + password
                                    + "&grant_type=" + OAuth2Constants.PASSWORD
                                    + "&client_id=" + clientId))
                    .build();

            return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add a new user in keycloak
     * @param user the user to create
     */
    public Response addNewUser(User user) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(user.getId());
        userRepresentation.setEmail(user.getEmail());
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());

        return keycloak.realm("gazelle").users().create(userRepresentation);
    }
}

