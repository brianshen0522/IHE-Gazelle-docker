package net.ihe.gazelle.validation.gateway.business;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

class ProfileReadIdTest {

   @Test
   void equalsAndHashCodeUseProfileIdAndServiceName() {
      ProfileReadId first = new ProfileReadId("profile-1", "service-a");
      ProfileReadId second = new ProfileReadId("profile-1", "service-a");
      ProfileReadId differentProfile = new ProfileReadId("profile-2", "service-a");
      ProfileReadId differentService = new ProfileReadId("profile-1", "service-b");

      assertThat(first, is(second));
      assertThat(first.hashCode(), is(second.hashCode()));
      assertThat(first, is(not(differentProfile)));
      assertThat(first, is(not(differentService)));
      assertThat(first.equals(null), is(false));
      assertThat(first.getProfileId(), is("profile-1"));
      assertThat(first.getServiceName(), is("service-a"));
      assertThat(first.getProfileId(), is(not(nullValue())));
   }
}
