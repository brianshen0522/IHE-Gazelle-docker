/*
 * Copyright 2025-2026 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.maestro.validation.step.technical.handler;

import net.ihe.gazelle.maestro.spi.business.Handler;
import net.ihe.gazelle.maestro.validation.step.business.ValidationHandler;
import net.ihe.gazelle.validation.v2.api.business.ValidationService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Handler for validation services resolved through service metadata.
 * This handler interfaces with a validation service to perform validation operations.
 */
public final class ValidationServiceHandler implements ValidationHandler {

   private static final Logger LOG = LoggerFactory.getLogger(ValidationServiceHandler.class);

   private final ValidationService validationService;

   /**
    * Constructor for ValidationServiceHandler.
    * @param validationService the validation service to use for validation operations
    * @throws IllegalArgumentException if validationService is null
    */
   public ValidationServiceHandler(ValidationService validationService) {
      if (validationService == null) {
         throw new IllegalArgumentException("validationService cannot be null");
      }
      this.validationService = validationService;
   }

   /**
    * Override from {@link Handler} Interface
    */
   @Override
   public boolean isAvailable() {
      try {
         return validationService.getValidationProfiles() != null;
      } catch (Exception e) {
         LOG.error("Error while trying to reach validation service", e);
         return false;
      }
   }

   @Override
   public ValidationReport validate(ValidationRequest validationRequest) {
      if (validationRequest == null) {
         throw new IllegalArgumentException("validationRequest cannot be null");
      }
      return validationService.validate(validationRequest);
   }

   @Override
   public List<ValidationProfile> getValidationProfiles() {
       return validationService.getValidationProfiles();
   }
}
