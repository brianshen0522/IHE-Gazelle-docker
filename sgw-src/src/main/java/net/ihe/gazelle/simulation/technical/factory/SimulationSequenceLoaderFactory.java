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

package net.ihe.gazelle.simulation.technical.factory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.ihe.gazelle.simulation.business.*;

/**
 * Factory class that produces the {@link LoadSimulationSequence} implementation for use within the application context.
 */
@ApplicationScoped
public class SimulationSequenceLoaderFactory {

   private final ServiceRegistryDAO serviceRegistryDAO;
   private final SimulationSequenceDAO simulationSequenceDAO;

   /**
    * Constructs an instance of {@code SimulationSequenceLoaderFactory} with the specified dependencies.
    *
    * @param serviceRegistryDAO    the DAO used to interact with the service registry, providing access to information about registered simulation services
    * @param simulationSequenceDAO the DAO used to interact with simulation services, providing access to simulation sequences and service-specific data
    */
   @Inject
   public SimulationSequenceLoaderFactory(ServiceRegistryDAO serviceRegistryDAO, SimulationSequenceDAO simulationSequenceDAO) {
      this.serviceRegistryDAO = serviceRegistryDAO;
      this.simulationSequenceDAO = simulationSequenceDAO;
   }

   /**
    * Produces an instance of {@link LoadSimulationSequence} for use within the application's context.
    *
    * @return an implementation of {@link LoadSimulationSequence}, specifically the {@link SimulationSequenceLoader},
    *         configured with the necessary DAOs to load and validate simulation sequences from registered services.
    */
   @Produces
   @ApplicationScoped
   public LoadSimulationSequence createSimulationSequenceLoader() {
      return new SimulationSequenceLoader(serviceRegistryDAO, simulationSequenceDAO);
   }

   /**
    * Creates and provides an instance of {@link SequenceChecksumCache}.
    *
    * @return an instance of {@link SequenceChecksumCache}, specifically
    *         {@link SequenceChecksumCacheImpl}, configured with the necessary dependencies.
    */
   @Produces
   @ApplicationScoped
   public SequenceChecksumCache createSequenceChecksumDAO() {
      return new SequenceChecksumCacheImpl(serviceRegistryDAO, simulationSequenceDAO);
   }
}
