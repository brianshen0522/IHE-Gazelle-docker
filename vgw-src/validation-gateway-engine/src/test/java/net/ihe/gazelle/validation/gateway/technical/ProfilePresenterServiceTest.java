package net.ihe.gazelle.validation.gateway.technical;

import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class ProfilePresenterServiceTest {

   @Test
   void convertsProfileToMapAndBack() {
      TestableProfilePresenterService presenter = new TestableProfilePresenterService();
      ValidationProfile profile = new ValidationProfile()
            .setProfileID("ITI-18")
            .setProfileName("XDS.b")
            .setDomain("LAB")
            .setCoveredItems(List.of("item-1"));

      Map<String, Object> asMap = presenter.toMap(profile);
      Map<String, Object> cleaned = stripInternalFlags(asMap);
      ValidationProfile presented = presenter.fromMap(cleaned);

      assertThat(presented.getProfileID(), is("ITI-18"));
      assertThat(presented.getProfileName(), is("XDS.b"));
      assertThat(presented.getDomain(), is("LAB"));
      assertThat(presented.getCoveredItems(), is(List.of("item-1")));
   }

   @Test
   void presentsOnlyRequestedFields() {
      TestableProfilePresenterService presenter = new TestableProfilePresenterService();
      ValidationProfile profile = new ValidationProfile()
            .setProfileID("ITI-18")
            .setProfileName("XDS.b")
            .setDomain("LAB");

      Map<String, Object> asMap = presenter.toMap(profile);
      Map<String, Object> cleaned = stripInternalFlags(asMap);
      cleaned.keySet().removeIf(key -> !"profileID".equals(key));
      ValidationProfile presented = presenter.fromMap(cleaned);

      assertThat(presented.getProfileID(), is("ITI-18"));
      assertThat(presented.getProfileName(), is(nullValue()));
      assertThat(presented.getDomain(), is(nullValue()));
   }

   private static final class TestableProfilePresenterService extends ProfilePresenterService {
      @Override
      public Map<String, Object> toMap(ValidationProfile profile) {
         return super.toMap(profile);
      }

      @Override
      public ValidationProfile fromMap(Map<String, Object> map) {
         return super.fromMap(map);
      }
   }

   private static Map<String, Object> stripInternalFlags(Map<String, Object> source) {
      Map<String, Object> cleaned = new HashMap<>(source);
      cleaned.keySet().removeIf(key -> key.endsWith("DefinedIfPresent") || key.endsWith("Defined"));
      return cleaned;
   }
}
