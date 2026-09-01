package net.ihe.gazelle.evsapi.client.technical;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ihe.gazelle.evsapi.client.business.ValidationClient;
import net.ihe.gazelle.evsapi.client.business.ValidationException;
import net.ihe.gazelle.evsapi.client.business.ValidationServiceProfile;
import net.ihe.gazelle.evsapi.client.business.request.ValidationRequest;
import net.ihe.gazelle.evsapi.client.business.response.ValidationResult;
import net.ihe.gazelle.evsapi.client.business.response.report.ValidationReport;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class AuthorizedValidationClient implements ValidationClient {

    private static final String ERROR_WHILE_SENDING_REQUEST = "Error while sending request to EVS ";

    private final ValidationClient delegate;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String validationsEndpoint;
    private final String bearerToken;

    public AuthorizedValidationClient(String evsUrl, String jwt) {
        this.delegate = new ValidationClientHttpImpl(evsUrl);
        this.httpClient = HttpClient.newBuilder()
              .connectTimeout(Duration.ofSeconds(10))
              .build();
        this.objectMapper = new ObjectMapper();
        this.validationsEndpoint = evsUrl + "/validations";
        this.bearerToken = "Bearer " + jwt;
    }

    @Override
    public String validate(ValidationRequest validationRequest) {
        return delegate.validate(validationRequest);
    }

    @Override
    public ValidationResult getValidationByOid(String oid) {
        return delegate.getValidationByOid(oid);
    }

    @Override
    public ValidationReport getValidationReportByOid(String oid) {
        return delegate.getValidationReportByOid(oid);
    }

    @Override
    public List<ValidationServiceProfile> getValidationProfiles() {
        return getProfiles("/profiles");
    }

    @Override
    public List<ValidationServiceProfile> getValidationProfilesByServiceName(String serviceName) {
        return getProfiles("/profiles?serviceName=" + serviceName.replace(" ", "%20"));
    }

    private List<ValidationServiceProfile> getProfiles(String path) {
        HttpRequest request = buildAuthorizedGetRequest(path);
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(
                  response.body(),
                  objectMapper.getTypeFactory().constructCollectionType(List.class, ValidationServiceProfile.class));
        } catch (IOException e) {
            throw new ValidationException("Error while deserializing response", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ValidationException("Request to EVS was interrupted", e);
        }
    }

    private HttpRequest buildAuthorizedGetRequest(String path) {
        try {
            return HttpRequest.newBuilder()
                  .uri(new URI(validationsEndpoint + path))
                  .GET()
                  .header("Accept", "application/json")
                  .header("Authorization", bearerToken)
                  .build();
        } catch (URISyntaxException e) {
            throw new ValidationException(ERROR_WHILE_SENDING_REQUEST, e);
        }
    }
}
