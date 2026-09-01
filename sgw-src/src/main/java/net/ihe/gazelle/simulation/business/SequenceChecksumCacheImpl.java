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

package net.ihe.gazelle.simulation.business;

import net.ihe.gazelle.simulation.business.model.SimulationServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link SequenceChecksumCache} interface.
 */
public class SequenceChecksumCacheImpl implements SequenceChecksumCache {

   private static final Logger LOG = LoggerFactory.getLogger(SequenceChecksumCacheImpl.class);

   private final Map<String, String> checksums = new ConcurrentHashMap<>();

   private final ServiceRegistryDAO serviceRegistryDAO;
   private final SimulationSequenceDAO simulationSequenceDAO;

   /**
    * Constructs an instance of SequenceChecksumCacheImpl.
    * This implementation is responsible for maintaining a checksum cache for simulation sequences
    * and automatically invalidating the cache when there are changes detected in the service registry
    * or the simulation services.
    *
    * @param serviceRegistryDAO The DAO layer for retrieving information about simulation services from the service registry.
    * @param simulationSequenceDAO The DAO layer for interacting with simulation sequences and their checksums.
    */
   public SequenceChecksumCacheImpl(ServiceRegistryDAO serviceRegistryDAO, SimulationSequenceDAO simulationSequenceDAO) {
      this.serviceRegistryDAO = serviceRegistryDAO;
      this.simulationSequenceDAO = simulationSequenceDAO;
      init(getAvailableServices());
   }

   @Override
   public boolean isOutDated() {
      Set<String> oldKeys = checksums.keySet();
      Set<String> newKeys = getAvailableServices();

      if (!oldKeys.equals(newKeys)) {
         init(newKeys);
         LOG.debug("Services has changed since last update. Triggering reset...");
         return true;
      }

      for (Map.Entry<String, String> entry : checksums.entrySet()) {
         String serviceUrl = entry.getKey();
         String oldChecksum = entry.getValue();
         String newChecksum = simulationSequenceDAO.getServiceChecksum(serviceUrl);
         if (!Objects.equals(oldChecksum, newChecksum)) {
            checksums.put(serviceUrl, newChecksum);
            LOG.debug("Checksum has changed since last update. Triggering reset...");
            return true;
         }
      }
      return false;
   }

   private void init(Set<String> servicesUrl) {
      checksums.clear();
      for (String serviceUrl : servicesUrl) {
         String checksum = simulationSequenceDAO.getServiceChecksum(serviceUrl);
         checksums.put(serviceUrl, checksum);
      }
   }

   private Set<String> getAvailableServices() {
      return serviceRegistryDAO.getAvailableSimulationServices()
            .stream()
            .map(SimulationServiceInfo::getSimulatorUrl)
            .collect(Collectors.toSet());
   }
}
