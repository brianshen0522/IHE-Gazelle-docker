package net.ihe.gazelle.validation.gateway.quarkus.ws;

import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.Sort;
import net.ihe.gazelle.search.jaxrs.api.QueryMapper;
import net.ihe.gazelle.search.jaxrs.api.QueryMapperUtil;
import net.ihe.gazelle.validation.gateway.business.ProfileSearchCriteria;
import net.ihe.gazelle.validation.gateway.quarkus.service.ValidationProfileIndexService;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static net.ihe.gazelle.search.api.Range.DEFAULT_OFFSET;

public class ValidationProfileQueryMapper
      implements QueryMapper<ValidationProfileQueryBeanParam, ProfileSearchCriteria> {

   private static final int DEFAULT_SEARCH_LIMIT = 10;

   private final QueryMapperUtil queryMapperUtil;

   public ValidationProfileQueryMapper(ValidationProfileIndexService indexService) {
      this.queryMapperUtil = new QueryMapperUtil(indexService);
   }

   @Override
   public SearchQuery<ProfileSearchCriteria> unmapSearchQuery(ValidationProfileQueryBeanParam queryParamBean) {
      return new SearchQuery<>(
            unmapSearchCriteria(queryParamBean),
            unmapRange(queryParamBean.getOffset(), queryParamBean.getLimit()),
            unmapSorts(queryParamBean.getSort(), queryParamBean.getSortOrder())
      );
   }

   @Override
   public ProfileSearchCriteria unmapSearchCriteria(ValidationProfileQueryBeanParam queryParamBean) {
      ProfileSearchCriteria criteria = new ProfileSearchCriteria();
      if (queryParamBean == null) {
         return criteria;
      }

      applyIfPresent(queryParamBean.getValidationService(), value -> criteria.setValidationService(splitValues(value)));
      applyIfPresent(queryParamBean.getProfileID(), value -> criteria.setProfileId(splitValues(value)));
      applyIfPresent(queryParamBean.getProfileName(), value -> criteria.setProfileName(splitValues(value)));
      applyIfPresent(queryParamBean.getVersion(), value -> criteria.setProfileVersion(splitValues(value)));
      applyIfPresent(queryParamBean.getDomain(), value -> criteria.setDomain(splitValues(value)));
      applyIfPresent(queryParamBean.getCoveredItems(), value -> criteria.setCoveredItems(splitValues(value)));
      applyIfPresent(queryParamBean.getStandards(), value -> criteria.setStandards(splitValues(value)));
      applyIfPresent(queryParamBean.getTags(), value -> criteria.setTags(splitValues(value)));

      return criteria;
   }

   @Override
   public Range unmapRange(Integer offset, String limit) {
      Range range = new Range();
      range.setOffset(Optional.ofNullable(offset).orElse(DEFAULT_OFFSET));
      if (limit == null || limit.isBlank()) {
         range.setLimit(DEFAULT_SEARCH_LIMIT);
      } else if (Range.ALL.equalsIgnoreCase(limit)) {
         range.setLimit(Integer.MAX_VALUE);
      } else {
         range.setLimit(Integer.parseInt(limit));
      }
      Range.validateRange(range);
      return range;
   }

   @Override
   public List<Sort> unmapSorts(String sortQueryParam) {
      return queryMapperUtil.buildSorts(sortQueryParam);
   }

   private List<Sort> unmapSorts(String sortQueryParam, String sortOrder) {
      return queryMapperUtil.buildSorts(normalizeSort(sortQueryParam, sortOrder));
   }

   private String normalizeSort(String sort, String sortOrder) {
      if (sort == null || sort.isBlank()) {
         return sort;
      }
      if (sortOrder == null || sortOrder.isBlank()) {
         return sort;
      }
      if (sort.contains(",")) {
         return sort;
      }

      boolean descending = "desc".equalsIgnoreCase(sortOrder);
      boolean ascending = "asc".equalsIgnoreCase(sortOrder);
      if (!descending && !ascending) {
         return sort;
      }

      if (descending && !sort.startsWith("-")) {
         return "-" + sort;
      }
      if (ascending && sort.startsWith("-")) {
         return sort.substring(1);
      }
      return sort;
   }

   private String[] splitValues(String raw) {
      return raw == null ? new String[0] : raw.split(",");
   }

   private void applyIfPresent(String raw, Consumer<String> consumer) {
      if (raw != null && !raw.isBlank()) {
         consumer.accept(raw);
      }
   }
}
