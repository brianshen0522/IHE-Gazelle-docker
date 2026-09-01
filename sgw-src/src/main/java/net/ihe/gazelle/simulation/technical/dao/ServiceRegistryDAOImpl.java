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

package net.ihe.gazelle.simulation.technical.dao;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ihe.gazelle.lang.InterruptedRuntimeException;
import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.servicemetadata.api.business.Binding;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;
import net.ihe.gazelle.serviceregistry.client.business.ServiceLookupClient;
import net.ihe.gazelle.serviceregistry.client.technical.rest.ServiceLookupClientImpl;
import net.ihe.gazelle.simulation.business.ApplicationConfig;
import net.ihe.gazelle.simulation.business.ServiceRegistryDAO;
import net.ihe.gazelle.simulation.business.model.SimulationServiceInfo;
import net.ihe.gazelle.simulation.jaxrs.api.technical.ws.SimulationAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the {@link ServiceRegistryDAO} interface.
 */
@ApplicationScoped
public class ServiceRegistryDAOImpl implements ServiceRegistryDAO {

   private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistryDAOImpl.class);

   private final LoadingCache<String, List<SimulationServiceInfo>> servicesCache;

   private final ApplicationConfig config;

   /**
    * Constructs an instance of ServiceRegistryDAOImpl using the provided application configuration.
    *
    * @param config The application configuration object containing service registry and cache timeout settings.
    *               Must not be null.
    */
   @Inject
   public ServiceRegistryDAOImpl(ApplicationConfig config) {
      this(config, Duration.ofMinutes(config.getServicesCacheTimeoutMinutes()));
   }

   /**
    * Constructs an instance of ServiceRegistryDAOImpl with the specified application configuration
    * and cache timeout duration.
    *
    * @param config The application configuration object containing details such as the service registry URL
    *               and other application settings. Must not be null.
    * @param timeout The duration for which cached simulation services remain valid before expiration.
    *                Must not be null.
    */
   public ServiceRegistryDAOImpl(ApplicationConfig config, Duration timeout) {
      this.config = config;
      servicesCache = Caffeine.newBuilder()
            .expireAfterWrite(timeout)
            .maximumSize(1)
            .build(key -> loadAvailableSimulationServices());
   }

   @Override
   public List<SimulationServiceInfo> getAvailableSimulationServices() {
      return servicesCache.get("services");
   }

   private List<SimulationServiceInfo> loadAvailableSimulationServices() {
      try {
         ServiceLookupClient client = new ServiceLookupClientImpl(config.getServiceRegistryUrl());
         LOG.debug("Loading available services from {}", config.getServiceRegistryUrl());
         SearchResult<DeployedService> searchResult = client.search(new SearchQuery<>(
               new ServiceSearchCriteria()
                     .setProvidedInterface(SimulationAPI.INTERFACE_NAME)
                     .setStatus(DeployedService.Status.AVAILABLE, DeployedService.Status.UNKNOWN),
               new Range(0, Integer.MAX_VALUE),
               null
         ));
         return extractServiceInfo(searchResult.objects());
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new InterruptedRuntimeException(e);
      }
   }

   private List<SimulationServiceInfo> extractServiceInfo(List<DeployedService> deployedServices) {
      List<SimulationServiceInfo> servicesInfo = new ArrayList<>();
      for (DeployedService service : deployedServices) {
         String simulatorName = service.getName();
         String simulatorVersion = service.getVersion();
         for (ProvidedInterface providedInterface : service.getProvidedInterfaces()) {
            for (Binding binding : providedInterface.getBindings()) {
               if (binding instanceof HttpRestBinding restBinding) {
                  String simulatorUrl = restBinding.getServiceUrl();
                  servicesInfo.add(new SimulationServiceInfo(simulatorName, simulatorVersion, simulatorUrl));
               }
            }
         }
      }
      return servicesInfo;
   }

}
