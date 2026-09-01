package net.ihe.gazelle.validation.gateway.business;

import net.ihe.gazelle.search.api.SearchCriteria;
import net.ihe.gazelle.search.api.SearchParameter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ProfileSearchCriteria implements SearchCriteria {

   public static final String VALIDATION_SERVICE = "validationService";
   public static final String PROFILE_ID = "profileId";
   public static final String PROFILE_NAME = "profileName";
   public static final String PROFILE_VERSION = "profileVersion";
   public static final String DOMAIN = "domain";
   public static final String COVERED_ITEM = "coveredItem";
   public static final String STANDARD = "standard";
   public static final String TAG = "tag";

   private SearchParameter validationService;
   private SearchParameter profileId;
   private SearchParameter profileName;
   private SearchParameter profileVersion;
   private SearchParameter domainParameter;
   private SearchParameter coveredItems;
   private SearchParameter standards;
   private SearchParameter tags;

   public SearchParameter getValidationService() {
      return validationService;
   }

   public ProfileSearchCriteria setValidationService(String... serviceNames) {
      this.validationService = buildParameter(VALIDATION_SERVICE, serviceNames);
      return this;
   }

   public SearchParameter getProfileId() {
      return profileId;
   }

   public ProfileSearchCriteria setProfileId(String... profileIds) {
      this.profileId = buildParameter(PROFILE_ID, profileIds);
      return this;
   }

   public SearchParameter getProfileName() {
      return profileName;
   }

   public ProfileSearchCriteria setProfileName(String... profileNames) {
      this.profileName = buildParameter(PROFILE_NAME, profileNames);
      return this;
   }

   public SearchParameter getProfileVersion() {
      return profileVersion;
   }

   public ProfileSearchCriteria setProfileVersion(String... profileVersions) {
      this.profileVersion = buildParameter(PROFILE_VERSION, profileVersions);
      return this;
   }

   public SearchParameter getDomain() {
      return domainParameter;
   }

   public ProfileSearchCriteria setDomain(String... domains) {
      this.domainParameter = buildParameter(DOMAIN, domains);
      return this;
   }

   public SearchParameter getCoveredItems() {
      return coveredItems;
   }

   public ProfileSearchCriteria setCoveredItems(String... coveredItems) {
      this.coveredItems = buildParameter(COVERED_ITEM, coveredItems);
      return this;
   }

   public SearchParameter getStandards() {
      return standards;
   }

   public ProfileSearchCriteria setStandards(String... standards) {
      this.standards = buildParameter(STANDARD, standards);
      return this;
   }

   public SearchParameter getTags() {
      return tags;
   }

   public ProfileSearchCriteria setTags(String... tags) {
      this.tags = buildParameter(TAG, tags);
      return this;
   }

   @Override
   public List<SearchParameter> getSearchParameters() {
      return Stream.of(validationService, profileId, profileName, profileVersion, domainParameter, coveredItems, standards, tags)
            .filter(Objects::nonNull)
            .toList();
   }

   private SearchParameter buildParameter(String name, String... values) {
      if (values == null || values.length == 0) {
         return null;
      }
      return new SearchParameter()
            .setName(name)
            .setValues(Arrays.asList((Object[]) values));
   }
}
