package net.ihe.gazelle.validation.gateway.quarkus.ws;

import net.ihe.gazelle.validation.gateway.business.ValidationProfileWithService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationProfileWithServiceDTOTest {

   @Test
   void fromMapsServiceAndProfile() {
      ValidationProfile profile = new ValidationProfile().setProfileID("ID-1");
      ValidationProfileWithService profileWithService = new ValidationProfileWithService("svc", profile);

      ValidationProfileWithServiceDTO dto = ValidationProfileWithServiceDTO.from(profileWithService);

      assertThat(dto.validationService(), is("svc"));
      assertThat(dto.profile().getProfileID(), is("ID-1"));
   }

   @Test
   void fromRejectsNull() {
      assertThrows(IllegalArgumentException.class, () -> ValidationProfileWithServiceDTO.from(null));
   }
}
