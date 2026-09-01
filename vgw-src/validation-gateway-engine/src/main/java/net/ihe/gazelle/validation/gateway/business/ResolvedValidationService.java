package net.ihe.gazelle.validation.gateway.business;

import net.ihe.gazelle.validation.v2.api.business.ValidationService;

import java.util.Objects;

public record ResolvedValidationService(String serviceName, ValidationService validationService) {

   public ResolvedValidationService(String serviceName, ValidationService validationService) {
      this.serviceName = serviceName;
      this.validationService = Objects.requireNonNull(validationService, "validationService must not be null");
   }
}
