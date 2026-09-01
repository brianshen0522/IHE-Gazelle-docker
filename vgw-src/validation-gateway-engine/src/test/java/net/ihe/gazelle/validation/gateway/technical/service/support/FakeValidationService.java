package net.ihe.gazelle.validation.gateway.technical.service.support;

import net.ihe.gazelle.validation.v2.api.business.ValidationService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest;

import java.util.List;

public class FakeValidationService implements ValidationService {

   private final List<ValidationProfile> validationProfiles;

   public FakeValidationService(List<ValidationProfile> validationProfiles) {
      this.validationProfiles = validationProfiles;
   }

   @Override
   public ValidationReport validate(ValidationRequest validationRequest) {
      throw new UnsupportedOperationException("Not needed for these tests.");
   }

   @Override
   public List<ValidationProfile> getValidationProfiles() {
      return validationProfiles;
   }
}
