package net.ihe.gazelle.validation.gateway.quarkus.ws;

import net.ihe.gazelle.search.api.UnknownFieldException;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.validation.gateway.business.ProfileSearchCriteria;
import net.ihe.gazelle.validation.gateway.business.ResolvedValidationService;
import net.ihe.gazelle.validation.gateway.business.SearchProfileService;
import net.ihe.gazelle.validation.gateway.business.ValidationServiceResolver;
import net.ihe.gazelle.validation.gateway.quarkus.service.ValidationProfileIndexService;
import net.ihe.gazelle.validation.v2.api.business.ValidationService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationProfileSuggestionServiceTest {

   private static final Authz ALLOW_ALL = new Authz() {
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
   void suggestsValuesForNormalizedFieldName() {
      ValidationServiceResolver resolver = new StubResolver(List.of(
            new ResolvedValidationService("svc",
                  new FixedValidationService(List.of(
                        profile("ID-1", "Alpha", "LAB"),
                        profile("ID-2", "Beta", "LAB")
                  )))
      ));
      SearchProfileService searchProfileService = new SearchProfileService(resolver, ALLOW_ALL);
      ValidationProfileSuggestionService suggestionService = new ValidationProfileSuggestionService(
            searchProfileService, new ValidationProfileIndexService());

      List<String> suggestions = suggestionService.getSuggestions("profileId",
            new ProfileSearchCriteria().setDomain("LAB"), null);

      assertThat(suggestions, contains("ID-1", "ID-2"));
   }

   @Test
   void removesCriteriaForRequestedFieldOnly() {
      ValidationServiceResolver resolver = new StubResolver(List.of(
            new ResolvedValidationService("svc",
                  new FixedValidationService(List.of(
                        profile("ID-1", "Alpha", "LAB"),
                        profile("ID-2", "Beta", "RAD")
                  )))
      ));
      SearchProfileService searchProfileService = new SearchProfileService(resolver, ALLOW_ALL);
      ValidationProfileSuggestionService suggestionService = new ValidationProfileSuggestionService(
            searchProfileService, new ValidationProfileIndexService());

      ProfileSearchCriteria criteria = new ProfileSearchCriteria()
            .setDomain("LAB")
            .setProfileName("Alpha");

      List<String> suggestions = suggestionService.getSuggestions("profileName", criteria, null);

      assertThat(suggestions, contains("Alpha"));
   }

   @Test
   void throwsOnUnknownField() {
      ValidationProfileSuggestionService suggestionService = new ValidationProfileSuggestionService(
            new SearchProfileService(new StubResolver(List.of()), ALLOW_ALL),
            new ValidationProfileIndexService());

      assertThrows(UnknownFieldException.class,
            () -> suggestionService.getSuggestions("unknown", new ProfileSearchCriteria(), null));
   }

   @Test
   void suggestsCoveredItemsWithTrimAndCaseInsensitiveDeduplication() {
      ValidationServiceResolver resolver = new StubResolver(List.of(
            new ResolvedValidationService("svc",
                  new FixedValidationService(List.of(
                        profile("ID-1", "Alpha", "LAB")
                              .setCoveredItems(java.util.Arrays.asList("  zeta ", null, "Alpha", "alpha", " ")),
                        profile("ID-2", "Beta", "LAB")
                              .setCoveredItems(List.of("beta", "alpha"))
                  )))
      ));
      SearchProfileService searchProfileService = new SearchProfileService(resolver, ALLOW_ALL);
      ValidationProfileSuggestionService suggestionService = new ValidationProfileSuggestionService(
            searchProfileService, new ValidationProfileIndexService());

      List<String> suggestions = suggestionService.getSuggestions("coveredItem", new ProfileSearchCriteria(), null);

      assertThat(suggestions, contains("Alpha", "beta", "zeta"));
   }

   @Test
   void suggestsValidationServiceNamesUsingAlias() {
      ValidationServiceResolver resolver = new StubResolver(List.of(
            new ResolvedValidationService("svc-b", new FixedValidationService(List.of(profile("ID-1", "A", "LAB")))),
            new ResolvedValidationService("svc-a", new FixedValidationService(List.of(profile("ID-2", "B", "LAB"))))
      ));
      SearchProfileService searchProfileService = new SearchProfileService(resolver, ALLOW_ALL);
      ValidationProfileSuggestionService suggestionService = new ValidationProfileSuggestionService(
            searchProfileService, new ValidationProfileIndexService());

      List<String> suggestions = suggestionService.getSuggestions("serviceName", new ProfileSearchCriteria(), null);

      assertThat(suggestions, contains("svc-a", "svc-b"));
   }

   @Test
   void suggestsValidationServiceNamesEvenWhenServiceHasNoProfiles() {
      ValidationServiceResolver resolver = new StubResolver(List.of(
            new ResolvedValidationService("svc-b", new FixedValidationService(List.of())),
            new ResolvedValidationService("svc-a", new FixedValidationService(List.of(profile("ID-1", "A", "LAB"))))
      ));
      SearchProfileService searchProfileService = new SearchProfileService(resolver, ALLOW_ALL);
      ValidationProfileSuggestionService suggestionService = new ValidationProfileSuggestionService(
            searchProfileService, new ValidationProfileIndexService());

      List<String> suggestions = suggestionService.getSuggestions("validationService", new ProfileSearchCriteria(), null);

      assertThat(suggestions, contains("svc-a", "svc-b"));
   }

   @Test
   void suggestsDomainWithCaseInsensitiveDeduplication() {
      ValidationServiceResolver resolver = new StubResolver(List.of(
            new ResolvedValidationService("svc",
                  new FixedValidationService(List.of(
                        profile("ID-1", "Alpha", "DICOM"),
                        profile("ID-2", "Beta", "dicom"),
                        profile("ID-3", "Gamma", "IHE")
                  )))
      ));
      SearchProfileService searchProfileService = new SearchProfileService(resolver, ALLOW_ALL);
      ValidationProfileSuggestionService suggestionService = new ValidationProfileSuggestionService(
            searchProfileService, new ValidationProfileIndexService());

      List<String> suggestions = suggestionService.getSuggestions("domain", new ProfileSearchCriteria(), null);

      assertThat(suggestions, contains("DICOM", "IHE"));
   }

   @Test
   void suggestsProfileNamesWithCaseInsensitiveDeduplication() {
      ValidationServiceResolver resolver = new StubResolver(List.of(
            new ResolvedValidationService("svc",
                  new FixedValidationService(List.of(
                        profile("ID-1", "alpha", "LAB"),
                        profile("ID-2", "Alpha", "LAB"),
                        profile("ID-3", "Beta", "LAB")
                  )))
      ));
      SearchProfileService searchProfileService = new SearchProfileService(resolver, ALLOW_ALL);
      ValidationProfileSuggestionService suggestionService = new ValidationProfileSuggestionService(
            searchProfileService, new ValidationProfileIndexService());

      List<String> suggestions = suggestionService.getSuggestions("profileName", new ProfileSearchCriteria(), null);

      assertThat(suggestions, contains("Alpha", "Beta"));
   }

   private static ValidationProfile profile(String id, String name, String domain) {
      return new ValidationProfile()
            .setProfileID(id)
            .setProfileName(name)
            .setDomain(domain);
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

   private static final class StubResolver implements ValidationServiceResolver {
      private final List<ResolvedValidationService> services;

      private StubResolver(List<ResolvedValidationService> services) {
         this.services = services;
      }

      @Override
      public List<ResolvedValidationService> resolve(ProfileSearchCriteria criteria, GazelleIdentity identity) {
         return services;
      }
   }
}
