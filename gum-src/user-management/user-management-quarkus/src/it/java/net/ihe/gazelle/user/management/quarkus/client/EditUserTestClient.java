package net.ihe.gazelle.user.management.quarkus.client;

import io.restassured.response.Response;
import net.ihe.gazelle.user.management.api.interlay.user.ActivationResource;
import org.apache.http.HttpHeaders;

import java.util.Locale;

import static io.restassured.RestAssured.given;
import static net.ihe.gazelle.security.mocks.OIDCJWTGenerator.getValidJwt;

public class EditUserTestClient {

    public static final String BEARER = "Bearer ";
    public static final String REST_USERS = "rest/v2/users";

    public Response activateUserFromActivationCode(String activationCode) {
        return given()
                .when()
                .post(REST_USERS + "/activate/" + activationCode)
                .then()
                .extract().response();
    }

    public Response activateUser(String userId) {
        ActivationResource activationResource = new ActivationResource(userId);
        String jwt = getValidJwt();
        return given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .body(activationResource)
                .header("Content-Type", "application/json")
                .post(REST_USERS + "/activate")
                .then()
                .extract().response();
    }

    public Response deactivateUser(String userId) {
        ActivationResource activationResource = new ActivationResource(userId);
        String jwt = getValidJwt();
        return given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .body(activationResource)
                .header("Content-Type", "application/json")
                .post(REST_USERS + "/deactivate")
                .then()
                .extract().response();
    }

    public Response deleteUser(String userId) {
        String jwt = getValidJwt();
        return given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .header(HttpHeaders.ACCEPT_LANGUAGE, Locale.ENGLISH.getLanguage())
                .delete(REST_USERS + "/" + userId)
                .then()
                .extract().response();
    }
}
