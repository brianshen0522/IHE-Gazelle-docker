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


import net.ihe.gazelle.evsapi.client.business.ValidationClient;
import net.ihe.gazelle.maestro.validation.step.business.ValidationHandler;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest;

import java.util.List;

/**
 * ValidationHandler implementation for EVS (External Validation Service) Handles validation through EVS API for various
 * validation types
 */
public class EvsValidationHandler implements ValidationHandler {

   private final String serviceName;
   private final ValidationClient validationClient;
   private final EvsValidationMapper mapper = new EvsValidationMapper();

   /**
    * Constructor for EvsValidationHandler.
    * @param serviceName the name of the validation service
    * @param validationClient the client to communicate with the EVS validation service
    */
   public EvsValidationHandler(String serviceName, ValidationClient validationClient) {
      this.validationClient = validationClient;
      this.serviceName = serviceName;
   }

   @Override
   public boolean isAvailable() {
      List<ValidationProfile> profiles = getValidationProfiles();
      return profiles != null && !profiles.isEmpty();
   }

   @Override
   public ValidationReport validate(ValidationRequest validationRequest) {
      net.ihe.gazelle.evsapi.client.business.response.report.ValidationReport evsReport =
            validate(mapper.toEvsRequest(serviceName, validationRequest));
      return mapper.toValidationReport(evsReport, validationRequest);
   }

   @Override
   public List<ValidationProfile> getValidationProfiles() {
      List<net.ihe.gazelle.evsapi.client.business.ValidationServiceProfile> serviceProfiles =
            validationClient.getValidationProfilesByServiceName(serviceName);
      if (serviceProfiles == null || serviceProfiles.isEmpty()) {
         return List.of();
      }
      return mapper.toValidationProfiles(serviceProfiles);
   }

   private net.ihe.gazelle.evsapi.client.business.response.report.ValidationReport validate(
         net.ihe.gazelle.evsapi.client.business.request.ValidationRequest evsRequest) {
      String urlLocation = validationClient.validate(evsRequest);
      return validationClient.getValidationReportByOid(getOid(urlLocation));
   }

   private String getOid(String urlLocation) {
       if (urlLocation == null || urlLocation.isBlank()) {
           return null;
       }

       int lastSlashIndex = urlLocation.lastIndexOf('/');
       String lastSegment = lastSlashIndex >= 0
               ? urlLocation.substring(lastSlashIndex + 1)
               : urlLocation;

       int queryIndex = lastSegment.indexOf('?');
       return queryIndex >= 0
               ? lastSegment.substring(0, queryIndex)
               : lastSegment;
   }

}
