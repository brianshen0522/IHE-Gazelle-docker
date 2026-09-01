package net.ihe.gazelle.evsapi.client.technical;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import net.ihe.gazelle.evsapi.client.business.ValidationService;
import net.ihe.gazelle.evsapi.client.business.ValidationException;
import net.ihe.gazelle.evsapi.client.business.request.HandledObject;
import net.ihe.gazelle.evsapi.client.business.request.ValidationRequest;
import net.ihe.gazelle.evsapi.client.business.response.ValidationResult;
import net.ihe.gazelle.evsapi.client.business.response.ValidationStatus;
import net.ihe.gazelle.validation.gateway.app.evs.EvsJwtTestProfile;
import net.ihe.gazelle.validation.gateway.app.itmock.MockEvsServiceResource;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@TestProfile(EvsJwtTestProfile.class)
@QuarkusTestResource(KeycloakMockResource.class)
@QuarkusTestResource(value = MockEvsServiceResource.class, restrictToAnnotatedClass = true)
class EvsApiClientXmlIT {

    @TestHTTPResource("/evs/rest")
    URI evsBaseUri;

    @Test
    void getValidationByOidAsXmlWithEvsApiClient() {
        XmlOnlyValidationClient client = new XmlOnlyValidationClient(evsBaseUri.toString());
        ValidationRequest request = new ValidationRequest()
              .setValidationService(new ValidationService()
                    .setName("mock-validation-service")
                    .setValidator("ITI-18_request"))
              .addValidationItem(new HandledObject()
                    .setOriginalFileName("payload.xml")
                    .setContent("test".getBytes(StandardCharsets.UTF_8)));

        String location = client.validate(request);
        String oid = URI.create(location).getPath().replaceAll(".*/", "");
        String xmlResponse = client.fetchValidationAsXml(location);

        ValidationResult result = client.getValidationByOid(oid);

        assertThat(result, notNullValue());
        assertThat(result.getStatus(), anyOf(
              is(ValidationStatus.DONE_PASSED),
              is(ValidationStatus.DONE_UNDEFINED),
              is(ValidationStatus.DONE_FAILED)));
        assertThat(xmlResponse, containsString("<validation"));
        assertThat(xmlResponse, containsString("validationService"));
        assertThat(xmlResponse, not(containsString("net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationDTO@")));
    }

    static class XmlOnlyValidationClient extends ValidationClientHttpImpl {

        private final HttpClient xmlHttpClient;

        XmlOnlyValidationClient(String evsUrl) {
            super(evsUrl);
            this.xmlHttpClient = HttpClient.newHttpClient();
        }

        String fetchValidationAsXml(String validationLocation) {
            HttpRequest xmlRequest = HttpRequest.newBuilder()
                  .uri(URI.create(validationLocation))
                  .header("Accept", "application/xml")
                  .GET()
                  .build();
            HttpResponse<String> response = sendHttpRequest(xmlRequest);
            return response.body();
        }

        @Override
        HttpResponse<String> sendHttpRequest(HttpRequest httpRequest) {
            HttpResponse<String> response;
            if ("GET".equals(httpRequest.method())) {
                HttpRequest xmlRequest = HttpRequest.newBuilder()
                      .uri(httpRequest.uri())
                      .header("Accept", "application/xml")
                      .GET()
                      .build();
                try {
                    response = xmlHttpClient.send(xmlRequest, HttpResponse.BodyHandlers.ofString());
                } catch (IOException e) {
                    throw new ValidationException("Error while sending request to EVS ", e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ValidationException("Request to EVS was interrupted", e);
                }
            } else {
                response = super.sendHttpRequest(httpRequest);
            }
            return response;
        }
    }
}
