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

import net.ihe.gazelle.search.api.IndexService;
import net.ihe.gazelle.search.api.IndexedField;
import net.ihe.gazelle.search.api.SuggestionService;
import net.ihe.gazelle.search.api.UnknownFieldException;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.simulation.business.SimulationSequenceLookupDAO;

import java.util.List;

/**
 * Service class to provide suggestions for sequence searches based on search criteria.
 */
public class SequenceSuggestionService implements SuggestionService<SequenceSearchCriteria> {

   private final IndexService indexService;
   private final SimulationSequenceLookupDAO simulationSequenceLookupDAO;

   /**
    * Constructs a new SequenceSuggestionService.
    *
    * @param indexService The service responsible for managing indexed fields and their metadata.
    * @param simulationSequenceLookupDAO The data access object for querying simulation sequence data.
    */
   public SequenceSuggestionService(IndexService indexService, SimulationSequenceLookupDAO simulationSequenceLookupDAO) {
      this.indexService = indexService;
      this.simulationSequenceLookupDAO = simulationSequenceLookupDAO;
   }

   @Override
   public List<String> getSuggestions(String field, SequenceSearchCriteria searchCriteria, GazelleIdentity identity) {
      if (field == null) {
         throw new UnknownFieldException("Field cannot be null");
      }
      IndexedField indexedField = indexService.getIndexedField(field);
      if (indexedField == null) {
         throw new UnknownFieldException("Unknown field: " + field);
      }
      return simulationSequenceLookupDAO.getPossibleValues(indexedField, searchCriteria);
   }
}
