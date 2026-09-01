package net.ihe.gazelle.validation.gateway.technical.cache;

import net.ihe.gazelle.validation.v2.api.business.ValidationService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest;

import java.util.List;
import java.util.Objects;

public class CachingValidationService implements ValidationService {

   private final String cacheKey;
   private final ValidationService delegate;
   private final ValidationProfileCache profileCache;

   public CachingValidationService(String cacheKey, ValidationService delegate, ValidationProfileCache profileCache) {
      this.cacheKey = Objects.requireNonNull(cacheKey, "cacheKey must not be null");
      this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
      this.profileCache = Objects.requireNonNull(profileCache, "profileCache must not be null");
   }

   @Override
   public ValidationReport validate(ValidationRequest validationRequest) {
      return delegate.validate(validationRequest);
   }

   @Override
   public List<ValidationProfile> getValidationProfiles() {
      return profileCache.getProfiles(cacheKey, delegate);
   }
}
