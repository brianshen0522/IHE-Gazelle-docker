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
import net.ihe.gazelle.search.api.SuggestionService;
import net.ihe.gazelle.search.jaxrs.api.QueryMapper;
import net.ihe.gazelle.simulation.business.SimulationSequenceLookupDAO;
import net.ihe.gazelle.simulation.business.search.SequenceIndexService;
import net.ihe.gazelle.simulation.business.search.SequenceSearchCriteria;
import net.ihe.gazelle.simulation.business.search.SequenceSuggestionService;
import net.ihe.gazelle.simulation.technical.mapper.SequenceQueryMapper;
import net.ihe.gazelle.simulation.technical.ws.SequenceQueryBeanParam;

/**
 * A factory class that provides producer methods for creating instances of query mappers
 * and suggestion services used for searching and filtering simulation sequences.
 */
@ApplicationScoped
public class SearchParameterServiceFactory {

   private final SequenceIndexService sequenceIndexService;
   private final SimulationSequenceLookupDAO simulationSequenceLookupDAO;

   /**
    * Constructs a {@code SearchParameterServiceFactory} instance with the injected dependencies.
    *
    * @param sequenceIndexService        the service providing access to the canonical set of index fields used for searching and filtering simulation sequences
    * @param simulationSequenceLookupDAO the DAO layer handling retrieval and search operations for simulation sequences
    */
   @Inject
   public SearchParameterServiceFactory(SequenceIndexService sequenceIndexService, SimulationSequenceLookupDAO simulationSequenceLookupDAO) {
      this.sequenceIndexService = sequenceIndexService;
      this.simulationSequenceLookupDAO = simulationSequenceLookupDAO;
   }

   /**
    * Produces a new instance of {@code QueryMapper} for mapping query parameters
    * of type {@code SequenceQueryBeanParam} to {@code SequenceSearchCriteria}.
    *
    * @return a {@code QueryMapper} instance that maps query parameters for simulation sequences.
    */
   @Produces
   public QueryMapper<SequenceQueryBeanParam, SequenceSearchCriteria> createQueryMapper() {
      return new SequenceQueryMapper(sequenceIndexService);
   }

   /**
    * Produces a {@code SuggestionService} instance for providing suggestions
    * based on {@code SequenceSearchCriteria} when searching for simulation sequences.
    *
    * @return a {@code SuggestionService} implementation that generates field-based
    *         suggestions for sequence searches using criteria and indexed data.
    */
   @Produces
   public SuggestionService<SequenceSearchCriteria> createSuggestionService() {
      return new SequenceSuggestionService(
            sequenceIndexService,
            simulationSequenceLookupDAO
      );
   }
}
