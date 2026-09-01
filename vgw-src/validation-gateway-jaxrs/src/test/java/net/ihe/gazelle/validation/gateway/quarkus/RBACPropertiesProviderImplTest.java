package net.ihe.gazelle.validation.gateway.quarkus;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class RBACPropertiesProviderImplTest {

   @Test
   void loadsRbacPropertiesFromResources() {
      RBACPropertiesProviderImpl provider = new RBACPropertiesProviderImpl();

      Properties properties = provider.getRBACProperties();

      assertThat(properties, notNullValue());
      assertThat(properties.getProperty("profile:read"), containsString("role:sut_operator"));
      assertThat(properties.getProperty("profile:read"), is(properties.get("profile:read")));
   }
}
