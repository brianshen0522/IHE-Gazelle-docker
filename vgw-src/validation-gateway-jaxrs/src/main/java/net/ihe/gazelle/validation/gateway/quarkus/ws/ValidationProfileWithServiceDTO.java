package net.ihe.gazelle.validation.gateway.quarkus.ws;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.validation.gateway.business.ValidationProfileWithService;
import net.ihe.gazelle.validation.v2.api.technical.dto.profile.ValidationProfileDTO;

@JsonPropertyOrder({"validationService", "profile"})
public record ValidationProfileWithServiceDTO(String validationService, ValidationProfileDTO profile) {

   public static ValidationProfileWithServiceDTO from(ValidationProfileWithService profileWithService) {
      if (profileWithService == null) {
         throw new IllegalArgumentException("profileWithService must not be null");
      }
      return new ValidationProfileWithServiceDTO(
              profileWithService.getValidationService(),
              new ValidationProfileDTO(profileWithService.getProfile())
      );
   }

   @Override
   @JsonProperty("validationService")
   public String validationService() {
      return validationService;
   }

   @Override
   @JsonProperty("profile")
   public ValidationProfileDTO profile() {
      return profile;
   }

}
