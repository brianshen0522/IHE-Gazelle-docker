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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import net.ihe.gazelle.search.api.IndexedField;
import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.Sort;
import net.ihe.gazelle.simulation.business.ApplicationConfig;
import net.ihe.gazelle.simulation.business.ExpirationTimer;
import net.ihe.gazelle.simulation.business.SequenceChecksumCache;
import net.ihe.gazelle.simulation.business.SimulationSequenceLookupDAO;
import net.ihe.gazelle.simulation.business.model.SimulationSequenceExtended;
import net.ihe.gazelle.simulation.business.search.SequenceSearchCriteria;

import java.time.Duration;
import java.util.List;

/**
 * The AutoRefreshSequenceLookupDAO class provides an implementation of the SimulationSequenceLookupDAO interface
 * with an automatic cache refresh mechanism.
 */
@Default
@ApplicationScoped
public class AutoRefreshSequenceLookupDAO implements SimulationSequenceLookupDAO {

    private final ExpirationTimer lookupCacheTimer;
    private final SequenceChecksumCache sequenceChecksumCache;
    private final SimulationSequenceLookupDAO simulationSequenceLookupDAO;

   /**
    * Constructs an instance of {@code AutoRefreshSequenceLookupDAO}.
    *
    * @param config The application configuration providing cache timeout values and other settings.
    * @param sequenceChecksumCache The cache mechanism used to determine if the sequence cache is outdated.
    * @param simulationSequenceLookupDAO The direct implementation of the SimulationSequenceLookupDAO interface providing
    *                                     the underlying DAO operations for simulation sequences.
    */
    @Inject
    public AutoRefreshSequenceLookupDAO(ApplicationConfig config, SequenceChecksumCache sequenceChecksumCache, @DirectDAO SimulationSequenceLookupDAO simulationSequenceLookupDAO) {
        this.sequenceChecksumCache = sequenceChecksumCache;
        this.simulationSequenceLookupDAO = simulationSequenceLookupDAO;
        this.lookupCacheTimer = new ExpirationTimer(Duration.ofMinutes(config.getSequencesCacheMaxTimeoutMinutes()));
        init();
    }

    @Override
    public List<String> getPossibleValues(IndexedField indexedField, SequenceSearchCriteria searchCriteria) {
        return simulationSequenceLookupDAO.getPossibleValues(indexedField, searchCriteria);
    }

    @Override
    public SearchResult<SimulationSequenceExtended> searchWithSortingAndPagination(SequenceSearchCriteria searchCriteria, Range range, List<Sort> sortParameters) {
        resetIfExpired();
        return simulationSequenceLookupDAO.searchWithSortingAndPagination(searchCriteria, range, sortParameters);
    }

    @Override
    public SimulationSequenceExtended getSimulationSequenceById(String id) {
        resetIfExpired();
        return simulationSequenceLookupDAO.getSimulationSequenceById(id);
    }

    @Override
    public void init() {
        simulationSequenceLookupDAO.init();
    }

    @Override
    public void reset() {
        simulationSequenceLookupDAO.reset();
    }

    private void resetIfExpired() {
        if (lookupCacheTimer.isExpired() || sequenceChecksumCache.isOutDated()) {
            lookupCacheTimer.reset();
            reset();
        }
    }
}
