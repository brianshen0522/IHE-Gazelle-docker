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
import net.ihe.gazelle.search.api.ReadService;
import net.ihe.gazelle.search.api.SearchService;
import net.ihe.gazelle.simulation.business.SimulationSequenceLookupDAO;
import net.ihe.gazelle.simulation.business.model.ResolvedSimulationSequence;
import net.ihe.gazelle.simulation.business.search.SequenceSearchCriteria;
import net.ihe.gazelle.simulation.business.search.SequenceSearchServiceImpl;
import net.ihe.gazelle.simulation.business.search.SimulationSequenceReadService;
import net.ihe.gazelle.simulation.business.svs.SimulationSVSServiceImpl;
import net.ihe.gazelle.svs.client.business.SVSService;

/**
 * Factory class that produces the {@link SearchService} and {@link ReadService} implementations for use within the application context.
 */
@ApplicationScoped
public class SimulationSequenceServiceFactory {

    private final SimulationSequenceLookupDAO simulationSequenceLookupDAO;
    private final SVSService svsService;

   /**
    * Constructs an instance of the {@code SimulationSequenceServiceFactory} with the required dependencies.
    *
    * @param simulationSequenceLookupDAO the DAO for accessing simulation sequence data
    * @param svsService the service used for interacting with the SVS service
    */
    @Inject
    public SimulationSequenceServiceFactory(SimulationSequenceLookupDAO simulationSequenceLookupDAO, SVSService svsService) {
        this.simulationSequenceLookupDAO = simulationSequenceLookupDAO;
        this.svsService = svsService;
    }

   /**
    * Creates and provides an instance of {@code SearchService} for performing search operations
    * on {@code ResolvedSimulationSequence} entities with {@code SequenceSearchCriteria} criteria.
    *
    * @return an instance of {@code SearchService} configured with the {@code SimulationSequenceLookupDAO}
    *         for accessing simulation sequence search data
    */
    @Produces
    public SearchService<ResolvedSimulationSequence, SequenceSearchCriteria> createSearchService() {
        return new SequenceSearchServiceImpl(simulationSequenceLookupDAO);
    }

   /**
    * Creates and provides an instance of {@code ReadService} for retrieving
    * {@code ResolvedSimulationSequence} entities based on their unique identifiers.
    *
    * @return an instance of {@code ReadService} configured with the {@code SimulationSequenceLookupDAO}
    *         for database access and an instance of {@code SVSServiceImpl} for interacting with the SVS service
    */
    @Produces
    public ReadService<String, ResolvedSimulationSequence> createReadService() {
        return new SimulationSequenceReadService(
                simulationSequenceLookupDAO,
                new SimulationSVSServiceImpl(svsService)
        );
    }
}
