package net.ihe.gazelle.validation.gateway.business;

import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.PresentationService;
import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.Sort;
import net.ihe.gazelle.search.api.UnknownSortParameterException;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.validation.v2.api.business.ValidationService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SearchProfileServiceTest {

   private static final Authz ALLOW_ALL_AUTHZ = new Authz() {
      @Override
      public boolean isAuthorized(GazelleIdentity identity, String action, Object... resources) {
         return true;
      }

      @Override
      public <C extends java.util.Collection<? extends Object>> C filterOutUnauthorized(
            GazelleIdentity identity, String action, C resources) {
         return resources;
      }
   };

   @Test
   void searchUsesDefaultRangeWhenQueryIsNull() {
      CapturingValidationServiceResolver resolver = new CapturingValidationServiceResolver()
            .returning(new ResolvedValidationService("svc-1", new FakeValidationService(List.of(
                  profile("ID-1", "Alpha", "1.0", "LAB"),
                  profile("ID-2", "Beta", "1.0", "RAD")
            ))));

      SearchProfileService service = new SearchProfileService(resolver, ALLOW_ALL_AUTHZ);

      SearchResult<ValidationProfileWithService> result = service.search(null, null);

      assertThat(result.offset(), is(0));
      assertThat(result.limit(), is(25));
      assertThat(result.totalObjects(), is(2));
      assertThat(result.objects(), hasSize(2));
      assertThat(resolver.lastCriteria.get(), notNullValue());
      assertThat(resolver.lastCriteria.get().getSearchParameters(), hasSize(0));
      assertThat(resolver.lastIdentity.get(), is((GazelleIdentity) null));
   }

   @Test
   void searchFiltersCaseInsensitiveAndByListFields() {
      CapturingValidationServiceResolver resolver = new CapturingValidationServiceResolver()
            .returning(new ResolvedValidationService("svc-1", new FakeValidationService(List.of(
                  profile("ID-1", "XDS Metadata", "1.0", "LAB", List.of("XDS", "Metadata"), List.of("HL7v2"),
                        List.of("tag-one")),
                  profile("ID-2", "FHIR Resources", "2.0", "RAD", List.of("FHIR"), List.of("IHE"), List.of("other"))
            ))));

      SearchProfileService service = new SearchProfileService(resolver, ALLOW_ALL_AUTHZ);

      ProfileSearchCriteria criteria = new ProfileSearchCriteria()
            .setProfileName("xds")
            .setStandards("hl7")
            .setTags("TAG");
      SearchQuery<ProfileSearchCriteria> query = new SearchQuery<>(criteria, new Range(0, 25), List.of());

      SearchResult<ValidationProfileWithService> result = service.search(query, null);

      assertThat(result.totalObjects(), is(1));
      assertThat(result.objects(), hasSize(1));
      assertThat(result.objects().getFirst().getProfile().getProfileID(), is("ID-1"));
   }

   @Test
   void searchAppliesSortingAndPagination() {
      CapturingValidationServiceResolver resolver = new CapturingValidationServiceResolver()
            .returning(new ResolvedValidationService("svc-1", new FakeValidationService(List.of(
                  profile("ID-1", "b", "1.0", "LAB"),
                  profile("ID-2", "A", "1.0", "LAB"),
                  profile("ID-3", null, "1.0", "LAB")
            ))));

      SearchProfileService service = new SearchProfileService(resolver, ALLOW_ALL_AUTHZ);

      SearchQuery<ProfileSearchCriteria> query = new SearchQuery<>(
            new ProfileSearchCriteria(),
            new Range(1, 1),
            List.of(Sort.ascending(ProfileSearchCriteria.PROFILE_NAME))
      );

      SearchResult<ValidationProfileWithService> result = service.search(query, null);

      assertThat(result.totalObjects(), is(3));
      assertThat(result.objects(), hasSize(1));
      assertThat(result.objects().getFirst().getProfile().getProfileID(), is("ID-1"));
   }

   @Test
   void searchThrowsOnUnknownSortParameter() {
      CapturingValidationServiceResolver resolver = new CapturingValidationServiceResolver()
            .returning(new ResolvedValidationService("svc-1",
                  new FakeValidationService(List.of(profile("ID-1", "A", "1.0", "LAB")))));

      SearchProfileService service = new SearchProfileService(resolver, ALLOW_ALL_AUTHZ);

      SearchQuery<ProfileSearchCriteria> query = new SearchQuery<>(
            new ProfileSearchCriteria(),
            new Range(0, 25),
            List.of(Sort.ascending("unknown"))
      );

      assertThrows(UnknownSortParameterException.class, () -> service.search(query, null));
   }

   @Test
   void searchDelegatesToResolver() {
      GazelleIdentity identity = identity("user-1");
      ProfileSearchCriteria criteria = new ProfileSearchCriteria().setProfileId("X");
      SearchQuery<ProfileSearchCriteria> query = new SearchQuery<>(criteria, new Range(0, 25), List.of());

      ValidationService validationService = new FakeValidationService(List.of(profile("ID-1", "A", "1.0", "LAB")));
      CapturingValidationServiceResolver resolver = new CapturingValidationServiceResolver()
            .returning(new ResolvedValidationService("svc-1", validationService));

      SearchProfileService service = new SearchProfileService(resolver, ALLOW_ALL_AUTHZ);
      service.search(query, identity);

      assertThat(resolver.lastCriteria.get(), sameInstance(criteria));
      assertThat(resolver.lastIdentity.get(), sameInstance(identity));
   }

   @Test
   void listValidationServiceNamesIncludesServicesWithoutProfiles() {
      GazelleIdentity identity = identity("user-1");
      CapturingValidationServiceResolver resolver = new CapturingValidationServiceResolver()
            .returningAll(List.of(
                  new ResolvedValidationService("svc-empty", new FakeValidationService(List.of())),
                  new ResolvedValidationService("svc-filled",
                        new FakeValidationService(List.of(profile("ID-1", "A", "1.0", "LAB"))))
            ));

      SearchProfileService service = new SearchProfileService(resolver, ALLOW_ALL_AUTHZ);
      List<String> names = service.getServiceNamesFromRegistry(new ProfileSearchCriteria(), identity);

      assertThat(names, is(List.of("svc-empty", "svc-filled")));
      assertThat(resolver.lastIdentity.get(), sameInstance(identity));
   }

   @Test
   void searchAggregatesAcrossServicesAndSkipsFailures() {
      ResolvedValidationService serviceOne = new ResolvedValidationService("svc-1",
            new FakeValidationService(List.of(profile("ID-1", "Alpha", "1.0", "LAB"))));
      ResolvedValidationService serviceTwo = new ResolvedValidationService("svc-2", new FailingValidationService());
      ResolvedValidationService serviceThree = new ResolvedValidationService("svc-3",
            new FakeValidationService(List.of(profile("ID-2", "Beta", "1.0", "RAD"))));

      CapturingValidationServiceResolver resolver = new CapturingValidationServiceResolver()
            .returningAll(List.of(serviceOne, serviceTwo, serviceThree));

      SearchProfileService service = new SearchProfileService(resolver, ALLOW_ALL_AUTHZ);
      SearchResult<ValidationProfileWithService> result = service.search(
            new SearchQuery<>(new ProfileSearchCriteria(), new Range(0, 25), List.of()),
            null);

      assertThat(result.totalObjects(), is(2));
      assertThat(result.objects(), hasSize(2));
      assertThat(result.objects().get(0).getValidationService(), is("svc-1"));
      assertThat(result.objects().get(1).getValidationService(), is("svc-3"));
   }

   @Test
   void searchWithPresentationNormalizesRequestedPaths() {
      CapturingValidationServiceResolver resolver = new CapturingValidationServiceResolver()
            .returning(new ResolvedValidationService("svc-1",
                  new FakeValidationService(List.of(profile("ID-1", "Alpha", "1.0", "LAB")))));
      AtomicReference<List<String>> capturedPaths = new AtomicReference<>();
      PresentationService<ValidationProfile> presentationService = new PresentationService<>() {
         @Override
         public ValidationProfile getPresentedObject(ValidationProfile object, List<String> attributePaths) {
            capturedPaths.set(attributePaths);
            return object;
         }

         @Override
         public List<ValidationProfile> getPresentedObjects(List<ValidationProfile> objects, List<String> attributePaths) {
            capturedPaths.set(attributePaths);
            return objects;
         }
      };
      SearchProfileService service = new SearchProfileService(resolver, presentationService, ALLOW_ALL_AUTHZ);

      SearchResult<ValidationProfileWithService> result = service.search(
            new SearchQuery<>(new ProfileSearchCriteria(), new Range(0, 25), List.of()),
            List.of(" profile.profileName ", "validationService", "profile", "", "profile.domain"),
            null
      );

      assertThat(result.totalObjects(), is(1));
      assertThat(capturedPaths.get(), is(List.of("profileName", "domain")));
   }

   private static GazelleIdentity identity(String id) {
      return new GazelleIdentity() {
         @Override
         public String getId() {
            return id;
         }

         @Override
         public String getName() {
            return id;
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
            return false;
         }

         @Override
         public boolean hasGroup(String group) {
            return false;
         }
      };
   }

   private static ValidationProfile profile(String id, String name, String version, String domain) {
      return new ValidationProfile()
            .setProfileID(id)
            .setProfileName(name)
            .setVersion(version)
            .setDomain(domain);
   }

   private static ValidationProfile profile(String id, String name, String version, String domain,
                                            List<String> coveredItems, List<String> standards, List<String> tags) {
      return profile(id, name, version, domain)
            .setCoveredItems(coveredItems)
            .setStandards(standards)
            .setTags(tags);
   }

   private static final class CapturingValidationServiceResolver implements ValidationServiceResolver {
      private final AtomicReference<ProfileSearchCriteria> lastCriteria = new AtomicReference<>();
      private final AtomicReference<GazelleIdentity> lastIdentity = new AtomicReference<>();
      private List<ResolvedValidationService> toReturn;

      private CapturingValidationServiceResolver returning(ResolvedValidationService toReturn) {
         this.toReturn = List.of(toReturn);
         return this;
      }

      private CapturingValidationServiceResolver returningAll(List<ResolvedValidationService> toReturn) {
         this.toReturn = toReturn;
         return this;
      }

      @Override
      public List<ResolvedValidationService> resolve(ProfileSearchCriteria criteria, GazelleIdentity identity) {
         lastCriteria.set(criteria);
         lastIdentity.set(identity);
         return toReturn;
      }
   }

   private static final class FakeValidationService implements ValidationService {
      private final List<ValidationProfile> validationProfiles;

      private FakeValidationService(List<ValidationProfile> validationProfiles) {
         this.validationProfiles = validationProfiles;
      }

      @Override
      public net.ihe.gazelle.validation.v2.api.business.report.ValidationReport validate(
            net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest validationRequest) {
         throw new UnsupportedOperationException("Not needed for these tests.");
      }

      @Override
      public List<ValidationProfile> getValidationProfiles() {
         return validationProfiles;
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
         throw new RuntimeException("Service unavailable");
      }
   }
}
