/*
 * Copyright 2025 IHE International.
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

package net.ihe.gazelle.serviceregistry.client.technical.job;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.ihe.gazelle.servicemetadata.api.business.MetadataService;
import net.ihe.gazelle.serviceregistry.client.business.ServiceRegistrationClient;

/**
 * Factory class for creating instances of RegistrationJob.
 * This class is responsible for producing the RegistrationJob bean based on the configuration and metadata service.
 */
public class RegistrationJobCDIFactory {

   private final RegistrationJobConfig config;
   private final MetadataService metadataService;
   private final ServiceRegistrationClient registrationClient;

   /**
    * Constructor for RegistrationJobCDIFactory.
    *
    * @param config             the configuration for the registration job
    * @param metadataService    the service providing metadata about the service to be registered
    * @param registrationClient the client used to register the service with the service registry
    */
   @Inject
   public RegistrationJobCDIFactory(RegistrationJobConfig config, MetadataService metadataService, ServiceRegistrationClient registrationClient) {
      this.config = config;
      this.metadataService = metadataService;
      this.registrationClient = registrationClient;
   }

   /**
    * Produces a RegistrationJob instance if the service registry is enabled.
    *
    * @return a new instance of RegistrationJob or null if the service registry is not enabled
    */
   @Produces
   @ApplicationScoped
   public RegistrationJob createRegistrationJob() {
      if (config.isServiceRegistryEnabled()) {
         return new RegistrationJob(metadataService, config, registrationClient);
      }
      return null;
   }
}
