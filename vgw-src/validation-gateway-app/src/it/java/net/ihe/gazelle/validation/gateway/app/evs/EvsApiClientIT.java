package net.ihe.gazelle.validation.gateway.app.evs;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import net.ihe.gazelle.evsapi.client.business.ValidationClient;
import net.ihe.gazelle.evsapi.client.business.ValidationService;
import net.ihe.gazelle.evsapi.client.business.ValidationServiceProfile;
import net.ihe.gazelle.evsapi.client.business.request.HandledObject;
import net.ihe.gazelle.evsapi.client.business.request.ValidationRequest;
import net.ihe.gazelle.evsapi.client.business.response.ValidationResult;
import net.ihe.gazelle.evsapi.client.business.response.ValidationStatus;
import net.ihe.gazelle.evsapi.client.technical.AuthorizedValidationClient;
import net.ihe.gazelle.evsapi.client.technical.ValidationClientHttpImpl;
import net.ihe.gazelle.validation.gateway.app.itmock.MockEvsServiceResource;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.security.mocks.OIDCJWTGenerator;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@TestProfile(EvsJwtTestProfile.class)
@QuarkusTestResource(KeycloakMockResource.class)
@QuarkusTestResource(value = MockEvsServiceResource.class, restrictToAnnotatedClass = true)
class EvsApiClientIT {

    @TestHTTPResource("/evs/rest")
    URI evsBaseUri;

    @Test
    void getProfilesWithEvsApiClient() {
        ValidationClient client = new AuthorizedValidationClient(evsBaseUri.toString(), validJwt());

        List<ValidationServiceProfile> profiles = client.getValidationProfiles();

        assertThat(profiles, hasSize(7));
        ValidationServiceProfile iti18 = profiles.stream()
              .filter(profile -> profile.getValidator() != null)
              .filter(profile -> "ITI-18_request".equals(profile.getValidator().getKeyword()))
              .findFirst()
              .orElseThrow();
        assertThat(iti18.getServiceName(), is("mock-validation-service"));
        assertThat(iti18.getValidator().getName(), is("ITI-18 Registry Stored Query Request"));
        assertThat(iti18.getValidator().getDomain(), is("ITI"));
    }

    @Test
    void validateWithEvsApiClient() {
        ValidationClient client = new ValidationClientHttpImpl(evsBaseUri.toString());
        ValidationRequest request = new ValidationRequest()
              .setValidationService(new ValidationService()
                    .setName("mock-validation-service")
                    .setValidator("ITI-18_request"))
              .addValidationItem(new HandledObject()
                    .setOriginalFileName("payload.xml")
                    .setContent("test".getBytes(StandardCharsets.UTF_8)));

        String location = client.validate(request);

        assertThat(location, notNullValue());
        assertThat(location, containsString("/evs/rest/validations/"));

        String oid = URI.create(location).getPath().replaceAll(".*/", "");
        ValidationResult result = client.getValidationByOid(oid);

        assertThat(result, notNullValue());
        assertThat(result.getStatus(), anyOf(
              is(ValidationStatus.DONE_PASSED),
              is(ValidationStatus.DONE_UNDEFINED),
              is(ValidationStatus.DONE_FAILED)));
        assertThat(result.getValidationService(), notNullValue());
        assertThat(result.getValidationService().getName(), is("mock-validation-service"));
        assertThat(result.getValidationService().getValidator(), is("ITI-18_request"));
    }

    private String validJwt() {
        return OIDCJWTGenerator.getValidJwtWithGroups(List.of("role:sut_operator"));
    }
}
