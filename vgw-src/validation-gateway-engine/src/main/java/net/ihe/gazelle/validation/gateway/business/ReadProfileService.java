package net.ihe.gazelle.validation.gateway.business;

import net.ihe.gazelle.search.api.ReadException;
import net.ihe.gazelle.search.api.ReadService;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;

public class ReadProfileService implements ReadService<ProfileReadId, ValidationProfile> {

   private final ValidationServiceResolver validationServiceResolver;
   private final Authz authz;

   public ReadProfileService(ValidationServiceResolver validationServiceResolver, Authz authz) {
      this.validationServiceResolver = Objects.requireNonNull(validationServiceResolver,
            "validationServiceResolver must not be null");
      this.authz = Objects.requireNonNull(authz, "authz must not be null");
   }

   @Override
   public ValidationProfile readObject(ProfileReadId id, GazelleIdentity identity) {
      return readObject(id, null, identity);
   }

   @Override
   public ValidationProfile readObject(ProfileReadId id, String presentationUrl, GazelleIdentity identity) {
      if (id == null || isBlank(id.getServiceName()) || isBlank(id.getProfileId())) {
         throw new IllegalArgumentException("serviceName and profileId must be provided.");
      }

       authz.assertAuthorized(identity, "profile:read");

       ProfileSearchCriteria criteria = new ProfileSearchCriteria()
            .setValidationService(id.getServiceName())
            .setProfileId(id.getProfileId());

      List<ResolvedValidationService> services = validationServiceResolver.resolve(criteria, identity);
      for (ResolvedValidationService service : services) {
         if (service != null
               && service.validationService() != null
               && serviceNameMatches(service.serviceName(), id.getServiceName())) {
            List<ValidationProfile> profiles;
            try {
               profiles = service.validationService().getValidationProfiles();
            } catch (RuntimeException e) {
               throw new ReadException("Failed to read validation profiles for service " + service.serviceName(), e);
            }
            ValidationProfile profile = findExactProfile(id.getProfileId(), profiles);
            if (profile != null) {
               return profile;
            }
         }
      }
      throw new NoSuchElementException("Profile not found.");
   }

   private boolean serviceNameMatches(String candidate, String expected) {
      if (candidate == null || expected == null) {
         return false;
      }
      return candidate.equalsIgnoreCase(expected);
   }

   private ValidationProfile findExactProfile(String profileId, List<ValidationProfile> profiles) {
      if (profiles == null || profiles.isEmpty()) {
         return null;
      }
      ValidationProfile exact = profiles.stream()
            .filter(Objects::nonNull)
            .filter(p -> p.getProfileID() != null)
            .filter(p -> p.getProfileID().equalsIgnoreCase(profileId))
            .findFirst()
            .orElse(null);
      if (exact != null) {
         return exact;
      }
      String expected = profileId.toLowerCase(Locale.ROOT);
      return profiles.stream()
            .filter(Objects::nonNull)
            .filter(p -> p.getProfileID() != null)
            .filter(p -> p.getProfileID().toLowerCase(Locale.ROOT).contains(expected))
            .min(java.util.Comparator.comparingInt(p -> p.getProfileID().length()))
            .orElse(null);
   }

   private boolean isBlank(String value) {
      return value == null || value.isBlank();
   }
}
