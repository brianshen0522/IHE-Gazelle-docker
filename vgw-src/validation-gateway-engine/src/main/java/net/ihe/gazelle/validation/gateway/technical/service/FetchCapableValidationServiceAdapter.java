package net.ihe.gazelle.validation.gateway.technical.service;

import net.ihe.gazelle.validation.gateway.technical.cache.ProfileFetchCapable;
import net.ihe.gazelle.validation.v2.api.business.ValidationService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest;
import net.ihe.gazelle.validation.v2.client.ValidationServiceHttpClient;

import java.util.List;
import java.util.Objects;

public final class FetchCapableValidationServiceAdapter implements ValidationService, ProfileFetchCapable {

   private final ValidationService delegate;
   private final ValidationServiceHttpClient httpClient;

   public FetchCapableValidationServiceAdapter(ValidationService delegate) {
      this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
      if (!(delegate instanceof ValidationServiceHttpClient client)) {
         throw new IllegalArgumentException("delegate must be a ValidationServiceHttpClient");
      }
      this.httpClient = client;
   }

   public static boolean supports(ValidationService delegate) {
      return delegate instanceof ValidationServiceHttpClient;
   }

   @Override
   public ValidationReport validate(ValidationRequest validationRequest) {
      return delegate.validate(validationRequest);
   }

   @Override
   public List<ValidationProfile> getValidationProfiles() {
      return delegate.getValidationProfiles();
   }

   @Override
   public ProfileFetchResponse fetchProfiles(String ifNoneMatch) {
      ValidationServiceHttpClient.ProfilesResponse response = httpClient.fetchProfiles(ifNoneMatch);
      return new ProfileFetchResponse(response.status(), response.etag(), response.profiles());
   }
}
