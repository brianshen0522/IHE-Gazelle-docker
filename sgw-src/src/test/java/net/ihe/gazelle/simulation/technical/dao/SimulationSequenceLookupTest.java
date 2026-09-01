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

import net.ihe.gazelle.search.api.*;
import net.ihe.gazelle.simulation.business.*;
import net.ihe.gazelle.simulation.business.model.SimulationSequenceExtended;
import net.ihe.gazelle.simulation.business.search.SequenceSearchCriteria;
import net.ihe.gazelle.simulation.technical.config.ApplicationConfigMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static net.ihe.gazelle.simulation.business.search.SequenceIndexService.*;
import static org.junit.jupiter.api.Assertions.*;

class SimulationSequenceLookupTest {

    private static SimulationSequenceLookupDAO dao;

    @BeforeAll
    static void setup() {
        ApplicationConfig applicationConfig = new ApplicationConfigMock();
        ServiceRegistryDAO serviceRegistryDAO = new ServiceRegistryDAOMock(true);
        SimulationSequenceDAO simulationSequenceDAOMock = new SimulationSequenceDAOMock();
        dao = new AutoRefreshSequenceLookupDAO(
                applicationConfig,
                new SequenceChecksumCacheImpl(serviceRegistryDAO, simulationSequenceDAOMock),
                new SimulationSequenceLookupDAOImpl(
                        new SimulationSequenceLoader(
                                serviceRegistryDAO,
                                simulationSequenceDAOMock
                        )
                )
        );
    }

    static Stream<Arguments> provideAttributes() {
        return Stream.of(
                Arguments.of(SERVICE_NAME, "XDS_Simulator"),
                Arguments.of(SIMULATED_ROLE, "MHD Document Source"),
                Arguments.of(TESTED_ROLE, "Document Registry"),
                Arguments.of(TRANSACTION, "ITI-71"),
                Arguments.of(STANDARD, "HTTP"),
                Arguments.of(SHORT_DESCRIPTION, "MHD Document Recipient receives a DocumentBundle with an Extended IUA token")
        );
    }

    @ParameterizedTest
    @MethodSource("provideAttributes")
    void should_get_possible_values(String field, String value) {
        IndexedField indexedField = new IndexedField(field, IndexedField.Type.STRING);
        List<String> possibleValues = dao.getPossibleValues(indexedField, new SequenceSearchCriteria());

        assertTrue(possibleValues.contains(value));
    }

    @Test
    void should_get_possible_values_throws_unknown_field() {
        IndexedField indexedField = new IndexedField("unknown", IndexedField.Type.STRING);
        SequenceSearchCriteria searchCriteria = new SequenceSearchCriteria();

        assertThrows(UnknownSearchParameterException.class, () -> dao.getPossibleValues(indexedField, searchCriteria));
    }

    @Test
    void should_match_all_sequence_with_no_search() {
        SearchResult<SimulationSequenceExtended> paginatedSequences = dao.searchWithSortingAndPagination(new SequenceSearchCriteria(), getRange(null, null), List.of());
        assertEquals(4, paginatedSequences.objects().size());
        assertEquals(4, paginatedSequences.totalObjects());
        dao.reset();
        paginatedSequences = dao.searchWithSortingAndPagination(new SequenceSearchCriteria(), getRange(null, null), List.of());
        assertEquals(4, paginatedSequences.objects().size());
        assertEquals(4, paginatedSequences.totalObjects());
        paginatedSequences = dao.searchWithSortingAndPagination(new SequenceSearchCriteria(), getRange(null, 2), List.of());
        assertEquals(2, paginatedSequences.objects().size());
        assertEquals(4, paginatedSequences.totalObjects());
    }

    @Test
    void should_create_parameters_list() {
        SequenceSearchCriteria params = new SequenceSearchCriteria()
                .setServiceName(generateSearchParameter(SERVICE_NAME, "XDS_Simulator"));
        List<SearchParameter> parameters = params.getSearchParameters();
        assertEquals(1, parameters.size());
    }

    @Test
    void should_match_sequence_with_service_name() {
        SequenceSearchCriteria params = new SequenceSearchCriteria()
                .setServiceName(generateSearchParameter(SERVICE_NAME, "XDS_Simulator"));
        SearchResult<SimulationSequenceExtended> paginatedSequences = dao.searchWithSortingAndPagination(params, getRange(null, null), List.of());
        assertEquals(2, paginatedSequences.objects().size());
        assertEquals(2, paginatedSequences.totalObjects());
    }

    @Test
    void should_match_sequence_with_id() {
        SequenceSearchCriteria params = new SequenceSearchCriteria()
                .setId(generateSearchParameter(ID, "CHXDS_ITI-41-42"));
        SearchResult<SimulationSequenceExtended> paginatedSequences = dao.searchWithSortingAndPagination(params, getRange(null, null), List.of());
        assertEquals(1, paginatedSequences.objects().size());
        assertEquals(1, paginatedSequences.totalObjects());
    }

    @Test
    void should_match_sequence_with_transaction_ITI_41() {
        SequenceSearchCriteria params = new SequenceSearchCriteria()
                .setTransaction(generateSearchParameter(TRANSACTION, "ITI-41"));
        SearchResult<SimulationSequenceExtended> paginatedSequences = dao.searchWithSortingAndPagination(params, getRange(null, null), generateSortParameterByName(TRANSACTION));
        assertEquals(2, paginatedSequences.objects().size());
        assertEquals(2, paginatedSequences.totalObjects());
        paginatedSequences = dao.searchWithSortingAndPagination(params, getRange(1, null), generateSortParameterByName(SERVICE_NAME));
        assertEquals(1, paginatedSequences.objects().size());
        assertEquals(2, paginatedSequences.totalObjects());
        assertEquals("CHXDS_ITI-43", paginatedSequences.objects().getFirst().getId());
    }

    @Test
    void should_match_sequence_with_standard_HTTP() {
        SequenceSearchCriteria params = new SequenceSearchCriteria()
                .setStandard(generateSearchParameter(STANDARD, "HTTP"));
        SearchResult<SimulationSequenceExtended> paginatedSequences = dao.searchWithSortingAndPagination(params, getRange(null, null), generateSortParameterByName(STANDARD));
        assertEquals(2, paginatedSequences.objects().size());
        assertEquals(2, paginatedSequences.totalObjects());
    }

    @Test
    void should_match_sequence_with_short_description() {
        SequenceSearchCriteria params = new SequenceSearchCriteria()
                .setShortDescription(generateSearchParameter(SHORT_DESCRIPTION, "XDS"));
        SearchResult<SimulationSequenceExtended> paginatedSequences = dao.searchWithSortingAndPagination(params, getRange(null, null), generateSortParameterByName(ID));
        assertEquals(2, paginatedSequences.objects().size());
        assertEquals(2, paginatedSequences.totalObjects());
    }

    @Test
    void should_match_sequence_with_simulated_role_document_source() {
        SequenceSearchCriteria params = new SequenceSearchCriteria()
                .setSimulatedRole(generateSearchParameter(SIMULATED_ROLE, "Document Consumer"));
        SearchResult<SimulationSequenceExtended> paginatedSequences = dao.searchWithSortingAndPagination(params, getRange(null, null), generateSortParameterByName(SIMULATED_ROLE));
        assertEquals(2, paginatedSequences.objects().size());
        assertEquals(2, paginatedSequences.totalObjects());
    }

    @Test
    void should_match_sequence_with_tested_role_document_source() {
        SequenceSearchCriteria params = new SequenceSearchCriteria()
                .setTestedRole(generateSearchParameter(TESTED_ROLE, "Document Registry"));
        SearchResult<SimulationSequenceExtended> paginatedSequences = dao.searchWithSortingAndPagination(params, getRange(null, null), generateSortParameterByName(TESTED_ROLE));
        assertEquals(1, paginatedSequences.objects().size());
        assertEquals(1, paginatedSequences.totalObjects());
    }

    @Test
    void should_match_sequence_with_sort_order_desc() {
        SequenceSearchCriteria params = new SequenceSearchCriteria()
                .setTransaction(generateSearchParameter(TRANSACTION, "ITI-41"));
        SearchResult<SimulationSequenceExtended> paginatedSequences = dao.searchWithSortingAndPagination(params, getRange(null, null), generateSortParameter(TRANSACTION, Sort.Order.ASCENDING));
        assertEquals(2, paginatedSequences.objects().size());
        assertEquals(2, paginatedSequences.totalObjects());
        assertEquals("CHXDS_ITI-41-42", paginatedSequences.objects().getFirst().getId());

        paginatedSequences = dao.searchWithSortingAndPagination(params, getRange(null, null), generateSortParameter(TRANSACTION, Sort.Order.DESCENDING));
        assertEquals(2, paginatedSequences.objects().size());
        assertEquals(2, paginatedSequences.totalObjects());
        assertEquals("CHXDS_ITI-43", paginatedSequences.objects().getFirst().getId());
    }

    @Test
    void should_match_nothing() {
        SequenceSearchCriteria params = new SequenceSearchCriteria()
                .setId(generateSearchParameter(ID, "non existing"));
        SearchResult<SimulationSequenceExtended> paginatedSequences = dao.searchWithSortingAndPagination(params, getRange(null, null), List.of());
        assertEquals(0, paginatedSequences.objects().size());
        assertEquals(0, paginatedSequences.totalObjects());
    }

    @Test
    void should_throw_unknown_sort_param() {
        SequenceSearchCriteria params = new SequenceSearchCriteria();
        Range range = getRange(null, null);
        List<Sort> sorts = generateSortParameter("unknown", Sort.Order.ASCENDING);

        assertThrows(UnknownSortParameterException.class, () -> dao.searchWithSortingAndPagination(params, range, sorts));
    }

    private SearchParameter generateSearchParameter(String name, String value) {
        return new SearchParameter()
                .setName(name)
                .setValue(value);
    }

    private Range getRange(Integer offset, Integer limit) {
        return new Range()
                .setOffset(Objects.requireNonNullElse(offset, 0))
                .setLimit(Objects.requireNonNullElse(limit, 25));
    }

    private List<Sort> generateSortParameterByName(String name) {
        return List.of(new Sort(name));
    }

    private List<Sort> generateSortParameter(String name, Sort.Order order) {
        return List.of(
                new Sort(name, order)
        );
    }
}
