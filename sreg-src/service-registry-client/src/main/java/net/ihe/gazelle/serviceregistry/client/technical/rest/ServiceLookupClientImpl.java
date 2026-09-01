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

package net.ihe.gazelle.serviceregistry.client.technical.rest;

import net.ihe.gazelle.m2m.client.technical.filter.vanilla.VanillaM2MHttpClientBuilder;
import net.ihe.gazelle.search.client.technical.AbstractSearchClient;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceIndexService;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;
import net.ihe.gazelle.serviceregistry.api.technical.dto.DeployedServiceDTO;
import net.ihe.gazelle.serviceregistry.client.business.ServiceLookupClient;

/**
 * ServiceLookupClientImpl is an implementation of the ServiceLookupClient interface that provides methods for searching
 * deployed services in the Service Registry. It extends AbstractSearchClient to handle the search operations.
 */
public class ServiceLookupClientImpl
      extends AbstractSearchClient<DeployedService, DeployedServiceDTO, ServiceSearchCriteria>
      implements ServiceLookupClient {

   /**
    * The resource path for accessing the services in the Service Registry.
    */
   public static final String SERVICES_RESOURCE_PATH = "/services";

   /**
    * Constructor for ServiceLookupClientImpl.
    *
    * @param serviceRegistryUrl the URL of the Service Registry to connect to.
    */
   public ServiceLookupClientImpl(String serviceRegistryUrl) {
      super(serviceRegistryUrl, new ServiceIndexService(), new VanillaM2MHttpClientBuilder().withAccessToken("GZL_SERVICE_K8S_ID"));
   }

   @Override
   protected Class<DeployedServiceDTO[]> getArrayClass() {
      return DeployedServiceDTO[].class;
   }

   @Override
   protected String getResourcePath() {
      return SERVICES_RESOURCE_PATH;
   }
}
