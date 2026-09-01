/*
 * Copyright 2022-2025 IHE International.
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

package net.ihe.gazelle.serviceregistry.technical.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import net.ihe.gazelle.servicemetadata.api.technical.AbstractMetadataService;
import net.ihe.gazelle.serviceregistry.api.business.ServiceRegistryDescriptor;

/**
 * Metadata service for the Service Registry.
 */
@Default
@ApplicationScoped
public class ServiceRegistryMetadata extends AbstractMetadataService {

   /**
    * Default constructor.
    */
   public ServiceRegistryMetadata() {
      super(ServiceRegistryMetadata.class);
   }

   @Override
   public String getServiceName() {
      return ServiceRegistryDescriptor.SERVICE_REGISTRY_NAME;
   }

   @Override
   public String getServiceDescription() {
      return "Registry of deployed services in Gazelle Test Bed.";
   }

}
