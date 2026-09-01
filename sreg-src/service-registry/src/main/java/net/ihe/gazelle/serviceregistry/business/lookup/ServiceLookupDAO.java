/*
 * Copyright 2025 IHE International.
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

package net.ihe.gazelle.serviceregistry.business.lookup;

import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.Sort;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;

import java.util.List;

/**
 * DAO interface for looking up deployed services based on search criteria. This interface defines methods to search for
 * deployed services with pagination and sorting.
 */
public interface ServiceLookupDAO {

   /**
    * Searches for deployed services based on the provided search criteria, range, and sorting parameters.
    *
    * @param serviceSearchParameters the criteria to filter the services
    * @param range                   the pagination range for the results
    * @param sortParameters          the sorting parameters for the results
    * @return a SearchResult containing the deployed services matching the criteria
    */
   SearchResult<DeployedService> search(ServiceSearchCriteria serviceSearchParameters,
                                        Range range,
                                        List<Sort> sortParameters);

   /**
    * Provides search suggestions based on a field and criteria.
    *
    * @param field    the field for which suggestions are requested
    * @param criteria the criteria to filter suggestions
    * @return a list of suggested strings matching the search text
    */
   List<String> getSuggestions(String field, ServiceSearchCriteria criteria);

}
