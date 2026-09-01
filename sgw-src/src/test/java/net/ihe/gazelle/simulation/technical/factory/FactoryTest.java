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

import net.ihe.gazelle.search.api.ReadService;
import net.ihe.gazelle.search.api.SearchService;
import net.ihe.gazelle.search.api.SuggestionService;
import net.ihe.gazelle.search.jaxrs.api.QueryMapper;
import net.ihe.gazelle.simulation.business.LoadSimulationSequence;
import net.ihe.gazelle.simulation.business.SequenceChecksumCache;
import net.ihe.gazelle.simulation.business.model.ResolvedSimulationSequence;
import net.ihe.gazelle.simulation.business.search.SequenceIndexService;
import net.ihe.gazelle.simulation.business.search.SequenceSearchCriteria;
import net.ihe.gazelle.simulation.technical.config.ApplicationConfigImpl;
import net.ihe.gazelle.simulation.technical.dao.ServiceRegistryDAOMock;
import net.ihe.gazelle.simulation.technical.dao.SimulationSequenceDAOMock;
import net.ihe.gazelle.simulation.technical.dao.SimulationSequenceLookupDAOImpl;
import net.ihe.gazelle.simulation.technical.ws.SequenceQueryBeanParam;
import net.ihe.gazelle.svs.client.business.SVSServiceImpl;
import net.ihe.gazelle.svs.client.technical.SVSHttpClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FactoryTest {

    @Test
    void should_create_simulation_sequence_loader_factory() {
        SimulationSequenceLoaderFactory factory = new SimulationSequenceLoaderFactory(
                new ServiceRegistryDAOMock(true),
                new SimulationSequenceDAOMock()
        );
        LoadSimulationSequence sequenceLoader = factory.createSimulationSequenceLoader();
        SequenceChecksumCache sequenceChecksumCache = factory.createSequenceChecksumDAO();
        assertNotNull(sequenceLoader);
        assertNotNull(sequenceChecksumCache);
    }

    @Test
    void should_create_search_parameter_service_factory() {
        SimulationSequenceLoaderFactory loaderFactory = new SimulationSequenceLoaderFactory(
                new ServiceRegistryDAOMock(true),
                new SimulationSequenceDAOMock()
        );
        SearchIndexServiceFactory searchIndexServiceFactory = new SearchIndexServiceFactory();
        SequenceIndexService indexService = searchIndexServiceFactory.createSequenceIndexService();
        SearchParameterServiceFactory searchParameterServiceFactory = new SearchParameterServiceFactory(
                searchIndexServiceFactory.createSequenceIndexService(),
                new SimulationSequenceLookupDAOImpl(loaderFactory.createSimulationSequenceLoader())
        );

        QueryMapper<SequenceQueryBeanParam, SequenceSearchCriteria> queryMapper = searchParameterServiceFactory.createQueryMapper();

        SuggestionService<SequenceSearchCriteria> suggestionService = searchParameterServiceFactory.createSuggestionService();

        assertNotNull(queryMapper);
        assertNotNull(indexService);
        assertNotNull(suggestionService);
    }

    @Test
    void should_create_simulation_sequence_service_factory() {
        SimulationSequenceLoaderFactory loaderFactory = new SimulationSequenceLoaderFactory(
                new ServiceRegistryDAOMock(true),
                new SimulationSequenceDAOMock()
        );
        SimulationSequenceServiceFactory factory = new SimulationSequenceServiceFactory(
                new SimulationSequenceLookupDAOImpl(loaderFactory.createSimulationSequenceLoader()),
                new SVSServiceImpl(new SVSHttpClient(new ApplicationConfigImpl().getSvsUrl()))
        );
        SearchService<ResolvedSimulationSequence, SequenceSearchCriteria> searchService = factory.createSearchService();
        ReadService<String, ResolvedSimulationSequence> readService = factory.createReadService();
        assertNotNull(searchService);
        assertNotNull(readService);
    }
}
