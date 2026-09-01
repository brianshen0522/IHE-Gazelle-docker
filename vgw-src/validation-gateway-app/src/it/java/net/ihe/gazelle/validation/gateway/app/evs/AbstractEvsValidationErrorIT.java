package net.ihe.gazelle.validation.gateway.app.evs;

import io.restassured.response.Response;
import net.ihe.gazelle.security.mocks.OIDCJWTGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

abstract class AbstractEvsValidationErrorIT {

    private static final String CREATE_VALIDATION_REQUEST_TEMPLATE =
          "/evs/error/requests/create-validation-request.json";

    protected String validJwt() {
        return OIDCJWTGenerator.getValidJwtWithGroups(List.of("role:sut_operator"));
    }

    protected String validationRequest(String serviceName) {
        return readResource(CREATE_VALIDATION_REQUEST_TEMPLATE)
              .replace("{{serviceName}}", serviceName);
    }

    protected String createValidationAndGetLocation(String jwt, String serviceName) {
        Response response = given()
              .header("Authorization", "Bearer " + jwt)
              .contentType("application/json")
              .body(validationRequest(serviceName))
              .when()
              .post("/evs/rest/validations")
              .then()
              .statusCode(201)
              .header("Content-Location", notNullValue())
              .extract()
              .response();
        return response.header("Content-Location");
    }

    private String readResource(String path) {
        try (InputStream inputStream = AbstractEvsValidationErrorIT.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing resource: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read resource: " + path, e);
        }
    }
}
