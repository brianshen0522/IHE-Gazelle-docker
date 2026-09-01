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

package net.ihe.gazelle.simulation;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchParameter;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.Sort;
import net.ihe.gazelle.simulation.business.search.SequenceIndexService;
import net.ihe.gazelle.simulation.business.model.SimulationSequenceExtended;
import net.ihe.gazelle.simulation.business.SimulationSequenceLookupDAO;
import net.ihe.gazelle.simulation.business.search.SequenceSearchCriteria;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.util.List;

import static net.ihe.gazelle.simulation.business.search.SequenceIndexService.TRANSACTION;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimulationLookupIT {

    @Inject
    SimulationSequenceLookupDAO simulationSequenceLookupDAO;

    @ConfigProperty(name = "gzl.it-port")
    int port;

    @BeforeAll
    public void setup() throws IOException {
        WireMockSingleton.startServer(port);
        WireMockSingleton.mockSimulationSequence();
        WireMockSingleton.mockServiceRegistry();
        WireMockSingleton.mockChecksum();
    }

    @Test
    void should_match_sequence_with_transaction_ITI_41() {
        SequenceSearchCriteria searchParameters = new SequenceSearchCriteria()
                .setTransaction(generateSearchParameter(TRANSACTION, "ITI-41"));
        Range range = new Range()
                .setLimit(1)
                .setOffset(0);
        Sort sortParameter = new Sort(SequenceIndexService.ID, Sort.Order.ASCENDING);

        SearchResult<SimulationSequenceExtended> result = simulationSequenceLookupDAO.searchWithSortingAndPagination(searchParameters, range, List.of(sortParameter));
        assertEquals(1, result.objects().size());
        assertEquals(2, result.totalObjects());
    }

    @Test
    void should_get_sequence_with_id_CHXDS_ITI() {
        SimulationSequenceExtended result = simulationSequenceLookupDAO.getSimulationSequenceById("CHXDS_ITI-41-42");
        assertEquals("CHXDS_ITI-41-42", result.getId());
    }

    private SearchParameter generateSearchParameter(String name, String value) {
        return new SearchParameter()
                .setName(name)
                .setValue(value);
    }
}
