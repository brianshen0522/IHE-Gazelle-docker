package net.ihe.gazelle.validation.gateway.quarkus.ws;

import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.SearchParameter;
import net.ihe.gazelle.search.api.SuggestionService;
import net.ihe.gazelle.search.api.UnknownFieldException;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.validation.gateway.business.ProfileSearchCriteria;
import net.ihe.gazelle.validation.gateway.business.SearchProfileService;
import net.ihe.gazelle.validation.gateway.business.ValidationProfileWithService;
import net.ihe.gazelle.validation.gateway.quarkus.service.ValidationProfileIndexService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import static java.util.stream.Collectors.toCollection;

public class ValidationProfileSuggestionService implements SuggestionService<ProfileSearchCriteria> {
   private static final String FIELD_VALIDATION_SERVICE = "validationService";
   private static final String FIELD_PROFILE_ID = "profileID";
   private static final String FIELD_PROFILE_NAME = "profileName";
   private static final String FIELD_VERSION = "version";
   private static final String FIELD_DOMAIN = "domain";
   private static final String FIELD_COVERED_ITEMS = "coveredItems";
   private static final String FIELD_STANDARDS = "standards";
   private static final String FIELD_TAGS = "tags";

   private final SearchProfileService searchProfileService;
   private final ValidationProfileIndexService indexService;

   public ValidationProfileSuggestionService(SearchProfileService searchProfileService,
                                             ValidationProfileIndexService indexService) {
      this.searchProfileService = Objects.requireNonNull(searchProfileService, "searchProfileService must not be null");
      this.indexService = Objects.requireNonNull(indexService, "indexService must not be null");
   }

   @Override
   public List<String> getSuggestions(String field, ProfileSearchCriteria criteria, GazelleIdentity identity) {
      String normalizedField = normalizeIndexedFieldName(field);
      if (normalizedField == null || !indexService.isIndexedField(normalizedField)) {
         throw new UnknownFieldException("Unknown search parameter: " + field);
      }

      ProfileSearchCriteria effectiveCriteria = criteria != null ? criteria : new ProfileSearchCriteria();
      ProfileSearchCriteria filteredCriteria = removeCriteriaForField(normalizedField, effectiveCriteria);

      if (FIELD_VALIDATION_SERVICE.equals(normalizedField)) {
         return normalizeValues(searchProfileService.getServiceNamesFromRegistry(filteredCriteria, identity));
      }

      SearchQuery<ProfileSearchCriteria> query = new SearchQuery<>(
            filteredCriteria,
            new Range(0, Integer.MAX_VALUE),
            List.of()
      );
      SearchResult<ValidationProfileWithService> result = searchProfileService.search(query, identity);

      return extractValues(normalizedField, result.objects());
   }

   private ProfileSearchCriteria removeCriteriaForField(String field, ProfileSearchCriteria criteria) {
      ProfileSearchCriteria copy = new ProfileSearchCriteria();
      if (!FIELD_VALIDATION_SERVICE.equals(field)) {
         copy.setValidationService(toValues(criteria.getValidationService()));
      }
      if (!FIELD_PROFILE_ID.equals(field)) {
         copy.setProfileId(toValues(criteria.getProfileId()));
      }
      if (!FIELD_PROFILE_NAME.equals(field)) {
         copy.setProfileName(toValues(criteria.getProfileName()));
      }
      if (!FIELD_VERSION.equals(field)) {
         copy.setProfileVersion(toValues(criteria.getProfileVersion()));
      }
      if (!FIELD_DOMAIN.equals(field)) {
         copy.setDomain(toValues(criteria.getDomain()));
      }
      if (!FIELD_COVERED_ITEMS.equals(field)) {
         copy.setCoveredItems(toValues(criteria.getCoveredItems()));
      }
      if (!FIELD_STANDARDS.equals(field)) {
         copy.setStandards(toValues(criteria.getStandards()));
      }
      if (!FIELD_TAGS.equals(field)) {
         copy.setTags(toValues(criteria.getTags()));
      }
      return copy;
   }

   private List<String> extractValues(String field, List<ValidationProfileWithService> profiles) {
      Set<String> values = new LinkedHashSet<>();
      for (ValidationProfileWithService profileWithService : profiles) {
         if (profileWithService == null) {
            continue;
         }
         if (FIELD_VALIDATION_SERVICE.equals(field)) {
            addIfPresent(values, profileWithService.getValidationService());
            continue;
         }
         ValidationProfile profile = profileWithService.getProfile();
         if (profile == null) {
            continue;
         }
         switch (field) {
            case FIELD_PROFILE_ID -> addIfPresent(values, profile.getProfileID());
            case FIELD_PROFILE_NAME -> addIfPresent(values, profile.getProfileName());
            case FIELD_VERSION -> addIfPresent(values, profile.getVersion());
            case FIELD_DOMAIN -> addIfPresent(values, profile.getDomain());
            case FIELD_COVERED_ITEMS -> addAllIfPresent(values, profile.getCoveredItems());
            case FIELD_STANDARDS -> addAllIfPresent(values, profile.getStandards());
            case FIELD_TAGS -> addAllIfPresent(values, profile.getTags());
            default -> throw new UnknownFieldException("Unknown search parameter: " + field);
         }
      }

      return normalizeValues(values);
   }

   private String preferValue(String current, String candidate) {
      boolean currentLower = current.equals(current.toLowerCase(Locale.ROOT));
      boolean candidateLower = candidate.equals(candidate.toLowerCase(Locale.ROOT));
      if (currentLower && !candidateLower) {
         return candidate;
      }
      return current;
   }

   private List<String> normalizeValues(Iterable<String> values) {
      if (values == null) {
         return List.of();
      }
      TreeMap<String, String> normalized = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      for (String value : values) {
         String trimmed = trimToNull(value);
         if (trimmed == null) {
            continue;
         }
         normalized.merge(trimmed, trimmed, this::preferValue);
      }
      return List.copyOf(normalized.values());
   }

   private String trimToNull(String value) {
      if (value == null) {
         return null;
      }
      String trimmed = value.trim();
      return trimmed.isBlank() ? null : trimmed;
   }

   private String normalizeIndexedFieldName(String name) {
      if (name == null) {
         return null;
      }
      return switch (name) {
         case "profileId" -> FIELD_PROFILE_ID;
         case "profileVersion" -> FIELD_VERSION;
         case "coveredItem" -> FIELD_COVERED_ITEMS;
         case "standard" -> FIELD_STANDARDS;
         case "tag" -> FIELD_TAGS;
         case "serviceName" -> FIELD_VALIDATION_SERVICE;
         default -> name;
      };
   }

   private String[] toValues(SearchParameter parameter) {
      if (parameter == null || parameter.getValues() == null || parameter.getValues().isEmpty()) {
         return new String[0];
      }
      return parameter.getValues().stream()
            .filter(Objects::nonNull)
            .map(String::valueOf)
            .toArray(String[]::new);
   }

   private void addIfPresent(Set<String> target, String value) {
      if (value == null) {
         return;
      }
      String normalized = value.trim();
      if (normalized.isBlank()) {
         return;
      }
      target.add(normalized);
   }

   private void addAllIfPresent(Set<String> target, List<String> values) {
      if (values == null || values.isEmpty()) {
         return;
      }
      target.addAll(values.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(v -> !v.isBlank())
            .collect(toCollection(LinkedHashSet::new)));
   }
}
