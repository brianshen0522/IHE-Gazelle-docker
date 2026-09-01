package net.ihe.gazelle.validation.gateway.quarkus.service;

import net.ihe.gazelle.search.api.IndexedField;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class ValidationProfileIndexServiceTest {

   @Test
   void exposesIndexedFields() {
      ValidationProfileIndexService indexService = new ValidationProfileIndexService();

      Set<IndexedField> fields = indexService.getIndexedFields();

      assertThat(fields, hasSize(8));
      assertThat(indexService.isIndexedField("profileID"), is(true));
      assertThat(indexService.isIndexedField("unknown"), is(false));
      assertThat(indexService.getIndexedField("profileID"), notNullValue());
      assertThat(indexService.getIndexedField("profileID").getFieldType(), is(IndexedField.Type.STRING));
   }
}
