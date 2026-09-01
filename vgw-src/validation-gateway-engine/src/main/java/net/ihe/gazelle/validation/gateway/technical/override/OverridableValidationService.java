package net.ihe.gazelle.validation.gateway.technical.override;

import net.ihe.gazelle.validation.gateway.technical.cache.ProfileFetchCapable;
import net.ihe.gazelle.validation.v2.api.business.ValidationService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class OverridableValidationService implements ValidationService, ProfileFetchCapable {

   private static final Logger logger = LoggerFactory.getLogger(OverridableValidationService.class);

   private final String serviceName;
   private final ValidationService delegate;
   private final ValidationProfilesOverride override;

   public OverridableValidationService(String serviceName,
                                       ValidationService delegate,
                                       ValidationProfilesOverride override) {
      this.serviceName = requireServiceName(serviceName);
      this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
      this.override = Objects.requireNonNull(override, "override must not be null");
   }

   @Override
   public ValidationReport validate(ValidationRequest validationRequest) {
      return delegate.validate(validationRequest);
   }

   @Override
   public List<ValidationProfile> getValidationProfiles() {
      return resolveOverride().orElseGet(delegate::getValidationProfiles);
   }

   @Override
   public ProfileFetchResponse fetchProfiles(String ifNoneMatch) {
      Optional<List<ValidationProfile>> overridden = resolveOverride();
      if (overridden.isPresent()) {
         List<ValidationProfile> profiles = normalizeProfiles(overridden.get());
         return new ProfileFetchResponse(200, null, profiles);
      }
      if (delegate instanceof ProfileFetchCapable capable) {
         return capable.fetchProfiles(ifNoneMatch);
      }
      throw new UnsupportedOperationException("Delegate does not support profile fetch");
   }

   private Optional<List<ValidationProfile>> resolveOverride() {
      Optional<List<ValidationProfile>> overridden = override.resolve(serviceName);
      overridden.ifPresent(this::logOverride);
      return overridden.map(OverridableValidationService::normalizeProfiles);
   }

   private void logOverride(List<ValidationProfile> profiles) {
      logger.info("Validation profiles overridden for service '{}' (profiles: {})", serviceName, normalizeProfiles(profiles).size());
   }

   private static List<ValidationProfile> normalizeProfiles(List<ValidationProfile> profiles) {
      return profiles != null ? profiles : List.of();
   }

   private static String requireServiceName(String serviceName) {
      String trimmed = serviceName != null ? serviceName.trim() : null;
      if (trimmed == null || trimmed.isEmpty()) {
         throw new IllegalArgumentException("serviceName must not be blank");
      }
      return trimmed;
   }
}
