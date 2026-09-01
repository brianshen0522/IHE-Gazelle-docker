package net.ihe.gazelle.validation.gateway.quarkus.config;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class QuarkusValidationCacheConfigurationTest {

   @Test
   void exposesConfiguredValues() {
      QuarkusValidationCacheConfiguration configuration = new QuarkusValidationCacheConfiguration();
      configuration.profileCacheMaxSize = 512;
      configuration.profileCacheTtlSeconds = 900;

      assertThat(configuration.getProfileCacheMaxSize(), is(512L));
      assertThat(configuration.getProfileCacheTtlSeconds(), is(900L));
   }
}
