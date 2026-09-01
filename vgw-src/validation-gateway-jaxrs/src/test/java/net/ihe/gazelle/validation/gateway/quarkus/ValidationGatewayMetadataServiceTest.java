package net.ihe.gazelle.validation.gateway.quarkus;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

class ValidationGatewayMetadataServiceTest {

   @Test
   void exposesServiceMetadata() {
      ValidationGatewayMetadataService service = new ValidationGatewayMetadataService();

      assertThat(service.getServiceName(), is("Validation Gateway"));
      assertThat(service.getServiceDescription(), containsString("validation"));
   }
}
