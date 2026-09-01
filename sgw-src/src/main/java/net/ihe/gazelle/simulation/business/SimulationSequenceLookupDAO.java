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

import net.ihe.gazelle.search.api.*;
import net.ihe.gazelle.simulation.business.model.SimulationSequenceExtended;
import net.ihe.gazelle.simulation.business.search.SequenceSearchCriteria;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * This interface is the internal simulation service API DAO layer for supported simulation sequences.
 */
public interface SimulationSequenceLookupDAO {

    /**
     * Get already existing values for a given field and given search parameters
     * @param indexedField The index field to which possible values are retrieved
     * @param searchCriteria The search parameters to constraint possibles values to subset of elements
     * @return The list of possible values as string
     * @throws UnknownSearchParameterException if an unknown search parameter is provided
     * @throws UnknownSortParameterException if an unknown sort parameter is provided
     * @throws SearchException if any unexpected error occurs during search process
     */
    List<String> getPossibleValues(IndexedField indexedField, SequenceSearchCriteria searchCriteria);

   /**
    * Searches simulation sequences based on the provided search criteria, applies sorting based on the specified
    * sort parameters, and supports pagination within the specified range.
    *
    * @param searchQuery the search criteria used to filter the simulation sequences
    * @param range the range defining the pagination constraints including the start and end positions
    * @param sortParameters a list of sort parameters defining the sorting order and fields
    * @return a search result containing a list of simulation sequences matching the criteria, sorted and paginated
    *
    * @throws UnknownSortParameterException if an unknown sort parameter is provided
    * @throws SearchException if any unexpected error occurs during search process
    */
    SearchResult<SimulationSequenceExtended> searchWithSortingAndPagination(SequenceSearchCriteria searchQuery, Range range, List<Sort> sortParameters);

    /**
     * Get a SimulationSequence from its unique identifier
     * @param id The id of the searched SimulationSequence
     * @return The found SimulationSequence with its service name and service url
     * @throws NoSuchElementException if the SimulationSequence identified by the id does not exist
     */
    SimulationSequenceExtended getSimulationSequenceById(String id);

    /**
     * Initialize the DAO with data from available services.
     */
    void init();

    /**
     * Reset the DAO state.
     */
    void reset();
}
