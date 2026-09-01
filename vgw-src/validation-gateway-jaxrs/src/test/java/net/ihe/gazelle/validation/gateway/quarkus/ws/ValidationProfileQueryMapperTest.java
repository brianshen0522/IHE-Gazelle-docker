package net.ihe.gazelle.validation.gateway.quarkus.ws;

import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.Sort;
import net.ihe.gazelle.validation.gateway.business.ProfileSearchCriteria;
import net.ihe.gazelle.validation.gateway.quarkus.service.ValidationProfileIndexService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

class ValidationProfileQueryMapperTest {

   @Test
   void unmapSearchCriteriaSplitsAndMapsValues() throws Exception {
      ValidationProfileQueryMapper mapper = new ValidationProfileQueryMapper(new ValidationProfileIndexService());
      ValidationProfileQueryBeanParam bean = new ValidationProfileQueryBeanParam();
      setField(bean, "validationService", "svc-1,svc-2");
      setField(bean, "profileID", "ID-1,ID-2");
      setField(bean, "profileName", "Alpha");
      setField(bean, "version", "1.0");
      setField(bean, "domain", "LAB");
      setField(bean, "coveredItems", "item1");
      setField(bean, "standards", "STD");
      setField(bean, "tags", "tag1,tag2");

      ProfileSearchCriteria criteria = mapper.unmapSearchCriteria(bean);

      assertThat(criteria.getValidationService().getValues(), contains("svc-1", "svc-2"));
      assertThat(criteria.getProfileId().getValues(), contains("ID-1", "ID-2"));
      assertThat(criteria.getProfileName().getValues(), contains("Alpha"));
      assertThat(criteria.getProfileVersion().getValues(), contains("1.0"));
      assertThat(criteria.getDomain().getValues(), contains("LAB"));
      assertThat(criteria.getCoveredItems().getValues(), contains("item1"));
      assertThat(criteria.getStandards().getValues(), contains("STD"));
      assertThat(criteria.getTags().getValues(), contains("tag1", "tag2"));
   }

   @Test
   void unmapRangeUsesDefaultsAndAll() {
      ValidationProfileQueryMapper mapper = new ValidationProfileQueryMapper(new ValidationProfileIndexService());

      Range defaultRange = mapper.unmapRange(null, null);
      assertThat(defaultRange.getOffset(), is(0));
      assertThat(defaultRange.getLimit(), is(10));

      Range allRange = mapper.unmapRange(5, Range.ALL);
      assertThat(allRange.getOffset(), is(5));
      assertThat(allRange.getLimit(), is(Integer.MAX_VALUE));
   }

   @Test
   void unmapSearchQueryHonorsSortOrder() throws Exception {
      ValidationProfileQueryMapper mapper = new ValidationProfileQueryMapper(new ValidationProfileIndexService());
      ValidationProfileQueryBeanParam bean = new ValidationProfileQueryBeanParam();
      bean.setSort("profileID");
      setField(bean, "sortOrder", "desc");

      SearchQuery<ProfileSearchCriteria> query = mapper.unmapSearchQuery(bean);

      List<Sort> sorts = query.sorts();
      assertThat(sorts.size(), is(1));
      assertThat(sorts.getFirst().getField(), is("profileID"));
      assertThat(sorts.getFirst().getOrder(), is(Sort.Order.DESCENDING));
   }

   private static void setField(Object target, String name, Object value) throws Exception {
      Field field = target.getClass().getDeclaredField(name);
      field.setAccessible(true);
      field.set(target, value);
   }
}
