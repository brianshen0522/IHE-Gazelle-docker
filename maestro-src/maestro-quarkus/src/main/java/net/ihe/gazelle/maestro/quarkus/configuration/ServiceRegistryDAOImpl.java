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

package net.ihe.gazelle.maestro.quarkus.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import net.ihe.gazelle.lang.InterruptedRuntimeException;
import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;
import net.ihe.gazelle.serviceregistry.client.business.ServiceLookupClient;
import net.ihe.gazelle.serviceregistry.client.technical.rest.ServiceLookupClientImpl;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link ServiceRegistryDAO} that provides access to
 * {@link Service} instances from the database.
 */
@ApplicationScoped
public class ServiceRegistryDAOImpl implements ServiceRegistryDAO {

   @ConfigProperty(name = "gzl.service.registry.url")
   String serviceRegistryUrl;

   /**
    * Default constructor
    */
   public ServiceRegistryDAOImpl() {
      // Empty
   }

   @Override
   public List<Service> getServices() {
      try {
         ServiceLookupClient client = new ServiceLookupClientImpl(serviceRegistryUrl);
         SearchResult<DeployedService> searchResult = client.search(new SearchQuery<>(
               new ServiceSearchCriteria(),
               new Range(0, Integer.MAX_VALUE),
               null
         ));
         return new ArrayList<>(searchResult.objects());
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new InterruptedRuntimeException(e);
      }
   }

}
