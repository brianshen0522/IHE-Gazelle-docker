package net.ihe.gazelle.validation.gateway.business;

import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.PresentationService;
import net.ihe.gazelle.search.api.SearchParameter;
import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.Sort;
import net.ihe.gazelle.search.api.UnknownSortParameterException;
import net.ihe.gazelle.search.api.SearchService;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.validation.gateway.technical.ProfilePresenterService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Collections.emptyList;

public class SearchProfileService implements SearchService<ValidationProfileWithService, ProfileSearchCriteria> {

   private static final int DEFAULT_OFFSET = 0;
   private static final int DEFAULT_LIMIT = 25;

   private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SearchProfileService.class);

   private final ValidationServiceResolver validationServiceResolver;
   private final PresentationService<ValidationProfile> presentationService;
   private final Authz authz;

   public SearchProfileService(ValidationServiceResolver validationServiceResolver, Authz authz) {
      this(validationServiceResolver, new ProfilePresenterService(), authz);
   }

   public SearchProfileService(ValidationServiceResolver validationServiceResolver,
                               PresentationService<ValidationProfile> presentationService,
                               Authz authz) {
      this.validationServiceResolver = Objects.requireNonNull(validationServiceResolver,
            "validationServiceResolver must not be null");
      this.presentationService = Objects.requireNonNull(presentationService, "presentationService must not be null");
      this.authz = Objects.requireNonNull(authz, "authz must not be null");
   }

   @Override
   public SearchResult<ValidationProfileWithService> search(SearchQuery<ProfileSearchCriteria> query,
                                                            GazelleIdentity identity) {
      return doSearch(query, identity);
   }

   @Override
   public SearchResult<ValidationProfileWithService> search(SearchQuery<ProfileSearchCriteria> query,
                                                            List<String> attributePaths,
                                                            GazelleIdentity identity) {
      SearchResult<ValidationProfileWithService> result = doSearch(query, identity);
      List<String> normalized = normalizePresentationPaths(attributePaths);
      if (normalized.isEmpty()) {
         return result;
      }
      List<ValidationProfileWithService> presented = result.objects().stream()
            .map(item -> {
               if (item == null) {
                  return null;
               }
               ValidationProfile presentedProfile = presentationService.getPresentedObject(
                     item.getProfile(),
                     normalized
               );
               return new ValidationProfileWithService(item.getValidationService(), presentedProfile);
            })
            .toList();
      return new SearchResult<>(presented, result.offset(), result.limit(), result.totalObjects());
   }

    public List<String> getServiceNamesFromRegistry(ProfileSearchCriteria criteria, GazelleIdentity identity) {
        authz.assertAuthorized(identity, "profile:read");
        ProfileSearchCriteria effectiveCriteria = criteria != null ? criteria : new ProfileSearchCriteria();
        List<ResolvedValidationService> services = validationServiceResolver.resolve(effectiveCriteria, identity);
        if (services == null || services.isEmpty()) {
            return List.of();
        }
        return services.stream()
                .filter(Objects::nonNull)
                .map(ResolvedValidationService::serviceName)
                .filter(Objects::nonNull)
                .toList();
    }

   private SearchResult<ValidationProfileWithService> doSearch(SearchQuery<ProfileSearchCriteria> query,
                                                                     GazelleIdentity identity) {
      authz.assertAuthorized(identity, "profile:read");
      SearchQuery<ProfileSearchCriteria> effectiveQuery = normalize(query);
      List<ResolvedValidationService> validationServices = validationServiceResolver
            .resolve(effectiveQuery.searchCriteria(), identity);

      List<ValidationProfileWithService> filteredProfiles = filter(validationServices, effectiveQuery.searchCriteria());
      List<ValidationProfileWithService> sortedProfiles = sort(filteredProfiles, effectiveQuery.sorts());
      List<ValidationProfileWithService> pagedProfiles = paginate(sortedProfiles, effectiveQuery.range());

      return new SearchResult<>(pagedProfiles, effectiveQuery.range().getOffset(), effectiveQuery.range().getLimit(),
            filteredProfiles.size());
   }

   private SearchQuery<ProfileSearchCriteria> normalize(SearchQuery<ProfileSearchCriteria> query) {
      ProfileSearchCriteria criteria = query != null && query.searchCriteria() != null ?
            query.searchCriteria() : new ProfileSearchCriteria();
      Range range = query != null && query.range() != null ? query.range() : new Range(DEFAULT_OFFSET, DEFAULT_LIMIT);
      Range.validateRange(range);
      List<Sort> sorts = query != null && query.sorts() != null ? query.sorts() : emptyList();
      return new SearchQuery<>(criteria, range, sorts);
   }

   private List<ValidationProfileWithService> filter(List<ResolvedValidationService> services,
                                                     ProfileSearchCriteria criteria) {
      if (services == null || services.isEmpty()) {
         return emptyList();
      }
      List<ValidationProfileWithService> results = new ArrayList<>();
      for (ResolvedValidationService service : services) {
         if (service != null
               && service.validationService() != null
               && matches(service.serviceName(), criteria.getValidationService())) {
            List<ValidationProfile> profiles;
            try {
               profiles = service.validationService().getValidationProfiles();
            } catch (RuntimeException e) {
               logger.error("Failed to retrieve validation profiles from service '{}'",
                     service.serviceName(), e);
               profiles = null;
            }
            if (profiles == null || profiles.isEmpty()) {
               logger.warn("No validation profiles retrieved from service '{}'", service.serviceName());
            } else {
               profiles.stream()
                     .filter(profile -> matches(profile.getProfileID(), criteria.getProfileId()))
                     .filter(profile -> matches(profile.getProfileName(), criteria.getProfileName()))
                     .filter(profile -> matches(profile.getVersion(), criteria.getProfileVersion()))
                     .filter(profile -> matches(profile.getDomain(), criteria.getDomain()))
                     .filter(profile -> matchesAny(profile.getCoveredItems(), criteria.getCoveredItems()))
                     .filter(profile -> matchesAny(profile.getStandards(), criteria.getStandards()))
                     .filter(profile -> matchesAny(profile.getTags(), criteria.getTags()))
                     .map(profile -> new ValidationProfileWithService(service.serviceName(), profile))
                     .forEach(results::add);
            }
         }
      }
      return results;
   }

   private boolean matches(String candidate, SearchParameter parameter) {
      if (parameter == null || parameter.getValues() == null || parameter.getValues().isEmpty()) {
         return true;
      }
      if (candidate == null) {
         return false;
      }
      String normalizedCandidate = candidate.toLowerCase(Locale.ROOT);
      return parameter.getValues().stream()
            .map(value -> value != null ? value.toString() : null)
            .filter(Objects::nonNull)
            .map(value -> value.toLowerCase(Locale.ROOT))
            .anyMatch(normalizedCandidate::contains);
   }

   private boolean matchesAny(List<String> candidates, SearchParameter parameter) {
      if (parameter == null || parameter.getValues() == null || parameter.getValues().isEmpty()) {
         return true;
      }
      if (candidates == null || candidates.isEmpty()) {
         return false;
      }
      List<String> expected = parameter.getValues().stream()
            .map(value -> value != null ? value.toString() : null)
            .filter(Objects::nonNull)
            .map(value -> value.toLowerCase(Locale.ROOT))
            .toList();
      return candidates.stream()
            .filter(Objects::nonNull)
            .map(value -> value.toLowerCase(Locale.ROOT))
            .anyMatch(candidateValue -> expected.stream().anyMatch(candidateValue::contains));
   }

   private List<ValidationProfileWithService> sort(List<ValidationProfileWithService> profiles, List<Sort> sorts) {
      if (profiles.isEmpty() || sorts == null || sorts.isEmpty()) {
         return profiles;
      }
      Comparator<ValidationProfileWithService> comparator = null;
      for (Sort sort : sorts) {
         Comparator<ValidationProfileWithService> comparatorForSort = buildComparator(sort);
         comparator = comparator == null ? comparatorForSort : comparator.thenComparing(comparatorForSort);
      }
      return profiles.stream().sorted(comparator).toList();
   }

   private Comparator<ValidationProfileWithService> buildComparator(Sort sort) {
      if (sort == null || sort.getField() == null) {
         throw new UnknownSortParameterException("Sort field must be provided.");
      }
      Comparator<ValidationProfileWithService> comparator = switch (sort.getField()) {
         case ProfileSearchCriteria.PROFILE_ID, "id" -> comparingBy(ValidationProfile::getProfileID);
         case ProfileSearchCriteria.PROFILE_NAME, "name" -> comparingBy(ValidationProfile::getProfileName);
         case ProfileSearchCriteria.PROFILE_VERSION, "version" -> comparingBy(ValidationProfile::getVersion);
         case ProfileSearchCriteria.DOMAIN -> comparingBy(ValidationProfile::getDomain);
         default -> throw new UnknownSortParameterException("Unknown sort parameter: " + sort.getField());
      };
      return sort.getOrder() == Sort.Order.DESCENDING ? comparator.reversed() : comparator;
   }

   private Comparator<ValidationProfileWithService> comparingBy(Function<ValidationProfile, String> extractor) {
      return Comparator.comparing(
            profileWithService -> {
               ValidationProfile profile = profileWithService != null ? profileWithService.getProfile() : null;
               String value = profile != null ? extractor.apply(profile) : null;
               return value != null ? value.toLowerCase(Locale.ROOT) : null;
            },
            Comparator.nullsLast(Comparator.naturalOrder())
      );
   }

   private List<ValidationProfileWithService> paginate(List<ValidationProfileWithService> profiles, Range range) {
      if (profiles.isEmpty()) {
         return profiles;
      }
      int offset = range.getOffset();
      int limit = range.getLimit();
      if (offset >= profiles.size()) {
         return emptyList();
      }
      int endIndex = Math.min(profiles.size(), offset + limit);
      return profiles.subList(offset, endIndex);
   }

   private List<String> normalizePresentationPaths(List<String> attributePaths) {
      if (attributePaths == null || attributePaths.isEmpty()) {
         return List.of();
      }
      List<String> normalized = new ArrayList<>();
      for (String path : attributePaths) {
         if (path != null && !path.isBlank()) {
            String trimmed = path.trim();
            if (trimmed.startsWith("profile.")) {
               trimmed = trimmed.substring("profile.".length());
            }
            if (!"validationService".equalsIgnoreCase(trimmed)
                  && !"profile".equalsIgnoreCase(trimmed)
                  && !trimmed.isEmpty()) {
               normalized.add(trimmed);
            }
         }
      }
      return normalized;
   }
}
