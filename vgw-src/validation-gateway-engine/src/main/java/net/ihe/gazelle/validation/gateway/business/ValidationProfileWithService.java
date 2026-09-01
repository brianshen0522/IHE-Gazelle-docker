package net.ihe.gazelle.validation.gateway.business;

import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;

public class ValidationProfileWithService {

   private String validationService;
   private ValidationProfile profile;

   public ValidationProfileWithService() {
   }

   public ValidationProfileWithService(String validationService, ValidationProfile profile) {
      this.validationService = validationService;
      this.profile = profile;
   }

   public String getValidationService() {
      return validationService;
   }

   public void setValidationService(String validationService) {
      this.validationService = validationService;
   }

   public ValidationProfile getProfile() {
      return profile;
   }

   public void setProfile(ValidationProfile profile) {
      this.profile = profile;
   }
}
