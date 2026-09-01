package net.ihe.gazelle.validation.gateway.business;

import net.ihe.gazelle.search.api.ReadException;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.UnauthorizedException;
import net.ihe.gazelle.validation.v2.api.business.ValidationService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReadProfileServiceTest {

   private static final GazelleIdentity IDENTITY = new GazelleIdentity() {
      @Override
      public String getId() {
         return "user-1";
      }

      @Override
      public String getName() {
         return "user-1";
      }

      @Override
      public Set<String> getGroups() {
         return Set.of();
      }

      @Override
      public String getOrganizationGroup() {
         return null;
      }

      @Override
      public String getOrganizationId() {
         return null;
      }

      @Override
      public Principal getPrincipal() {
         return null;
      }

      @Override
      public boolean isAuthenticated() {
         return true;
      }

      @Override
      public boolean hasGroup(String group) {
         return false;
      }
   };

   private static final Authz ALLOW_ALL_AUTHZ = new Authz() {
      @Override
      public boolean isAuthorized(GazelleIdentity identity, String action, Object... context) {
         return true;
      }

      @Override
      public <C extends java.util.Collection<? extends Object>> C filterOutUnauthorized(
            GazelleIdentity identity, String action, C collection) {
         return collection;
      }
   };

   @Test
   void readObjectRejectsMissingIdentifiers() {
      ReadProfileService service = new ReadProfileService(new StubResolver(), ALLOW_ALL_AUTHZ);
      ProfileReadId missingProfileId = new ProfileReadId("", "service");
      ProfileReadId missingServiceName = new ProfileReadId("profile", "");

      assertThrows(IllegalArgumentException.class, () -> service.readObject(null, IDENTITY));
      assertThrows(IllegalArgumentException.class, () -> service.readObject(missingProfileId, IDENTITY));
      assertThrows(IllegalArgumentException.class, () -> service.readObject(missingServiceName, IDENTITY));
   }

   @Test
   void readObjectRequiresAuthorization() {
      Authz denyingAuthz = new Authz() {
         @Override
         public boolean isAuthorized(GazelleIdentity identity, String action, Object... context) {
            return false;
         }

         @Override
         public <C extends java.util.Collection<? extends Object>> C filterOutUnauthorized(
               GazelleIdentity identity, String action, C collection) {
            return collection;
         }
      };

      ReadProfileService service = new ReadProfileService(new StubResolver(), denyingAuthz);
      ProfileReadId readId = new ProfileReadId("profile", "service");

      assertThrows(UnauthorizedException.class, () -> service.readObject(readId, IDENTITY));
   }

   @Test
   void readObjectReturnsExactMatchIgnoringCase() {
      ValidationProfile exact = profile("ITI-18", "Profile A");
      ValidationProfile extra = profile("ITI-18_request", "Profile B");
      ResolvedValidationService resolved = new ResolvedValidationService("Service-A",
            new FixedValidationService(List.of(extra, exact)));
      StubResolver resolver = new StubResolver().returning(List.of(resolved));

      ReadProfileService service = new ReadProfileService(resolver, ALLOW_ALL_AUTHZ);
      ValidationProfile result = service.readObject(new ProfileReadId("iti-18", "service-a"), IDENTITY);

      assertThat(result, sameInstance(exact));
      assertThat(resolver.lastCriteria.get().getProfileId().getValues().getFirst(), is("iti-18"));
   }

   @Test
   void readObjectFallsBackToShortestContainingMatch() {
      ValidationProfile first = profile("ITI-18_request", "Profile A");
      ValidationProfile second = profile("ITI-18_request_v2", "Profile B");
      ResolvedValidationService resolved = new ResolvedValidationService("svc",
            new FixedValidationService(List.of(second, first)));
      StubResolver resolver = new StubResolver().returning(List.of(resolved));

      ReadProfileService service = new ReadProfileService(resolver, ALLOW_ALL_AUTHZ);
      ValidationProfile result = service.readObject(new ProfileReadId("ITI-18", "svc"), IDENTITY);

      assertThat(result, sameInstance(first));
   }

   @Test
   void readObjectWrapsServiceFailuresInReadException() {
      ResolvedValidationService resolved = new ResolvedValidationService("svc",
            new FailingValidationService());
      ReadProfileService service = new ReadProfileService(new StubResolver().returning(List.of(resolved)),
            ALLOW_ALL_AUTHZ);
      ProfileReadId readId = new ProfileReadId("profile", "svc");

      ReadException error = assertThrows(ReadException.class, () -> service.readObject(readId, IDENTITY));

      assertThat(error.getMessage(), is("Failed to read validation profiles for service svc"));
   }

   @Test
   void readObjectThrowsWhenProfileNotFound() {
      ResolvedValidationService resolved = new ResolvedValidationService("svc",
            new FixedValidationService(List.of(profile("OTHER", "Profile"))));
      ReadProfileService service = new ReadProfileService(new StubResolver().returning(List.of(resolved)),
            ALLOW_ALL_AUTHZ);
      ProfileReadId readId = new ProfileReadId("missing", "svc");

      assertThrows(NoSuchElementException.class, () -> service.readObject(readId, IDENTITY));
   }

   private static ValidationProfile profile(String id, String name) {
      return new ValidationProfile().setProfileID(id).setProfileName(name);
   }

   private static final class FixedValidationService implements ValidationService {
      private final List<ValidationProfile> profiles;

      private FixedValidationService(List<ValidationProfile> profiles) {
         this.profiles = profiles;
      }

      @Override
      public net.ihe.gazelle.validation.v2.api.business.report.ValidationReport validate(
            net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest validationRequest) {
         throw new UnsupportedOperationException("Not needed for these tests.");
      }

      @Override
      public List<ValidationProfile> getValidationProfiles() {
         return profiles;
      }
   }

   private static final class FailingValidationService implements ValidationService {
      @Override
      public net.ihe.gazelle.validation.v2.api.business.report.ValidationReport validate(
            net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest validationRequest) {
         throw new UnsupportedOperationException("Not needed for these tests.");
      }

      @Override
      public List<ValidationProfile> getValidationProfiles() {
         throw new RuntimeException("Service failed");
      }
   }

   private static final class StubResolver implements ValidationServiceResolver {
      private List<ResolvedValidationService> toReturn = List.of();
      private final AtomicReference<ProfileSearchCriteria> lastCriteria = new AtomicReference<>();

      private StubResolver returning(List<ResolvedValidationService> resolved) {
         this.toReturn = resolved;
         return this;
      }

      @Override
      public List<ResolvedValidationService> resolve(ProfileSearchCriteria criteria, GazelleIdentity identity) {
         lastCriteria.set(criteria);
         return toReturn;
      }
   }
}
