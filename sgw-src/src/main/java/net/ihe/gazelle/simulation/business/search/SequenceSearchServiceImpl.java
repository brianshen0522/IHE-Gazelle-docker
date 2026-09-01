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

package net.ihe.gazelle.simulation.business.search;

import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.SearchService;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.simulation.business.SimulationSequenceLookupDAO;
import net.ihe.gazelle.simulation.business.model.ResolvedSimulationSequence;
import net.ihe.gazelle.simulation.business.model.SimulationSequenceExtended;

import java.util.List;

/**
 * Implementation of the {@link SearchService} interface for {@link SequenceSearchCriteria}.
 */
public class SequenceSearchServiceImpl implements SearchService<ResolvedSimulationSequence, SequenceSearchCriteria> {

   private final SimulationSequenceLookupDAO simulationSequenceLookupDAO;

   /**
    * Constructs a new SequenceSearchServiceImpl instance with the given SimulationSequenceLookupDAO.
    *
    * @param simulationSequenceLookupDAO the DAO used for searching simulation sequences and managing
    *                                    related data operations
    */
   public SequenceSearchServiceImpl(SimulationSequenceLookupDAO simulationSequenceLookupDAO) {
      this.simulationSequenceLookupDAO = simulationSequenceLookupDAO;
   }

   @Override
   public SearchResult<ResolvedSimulationSequence> search(SearchQuery<SequenceSearchCriteria> query, GazelleIdentity identity) {
      SearchResult<SimulationSequenceExtended> result = simulationSequenceLookupDAO.searchWithSortingAndPagination(query.searchCriteria(), query.range(), query.sorts());
      List<ResolvedSimulationSequence> resolvedSimulationSequences = result.objects().stream().map(
            ResolvedSimulationSequence::new
      ).toList();
      return new SearchResult<>(resolvedSimulationSequences, result.offset(), result.limit(), result.totalObjects());
   }

   @Override
   public SearchResult<ResolvedSimulationSequence> search(SearchQuery<SequenceSearchCriteria> query, List<String> attributePaths, GazelleIdentity identity) {
      return search(query, identity);
   }
}
