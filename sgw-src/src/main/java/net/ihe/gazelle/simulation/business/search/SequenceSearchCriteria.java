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

import net.ihe.gazelle.search.api.SearchCriteria;
import net.ihe.gazelle.search.api.SearchParameter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Criteria for searching for simulation sequences.
 */
public class SequenceSearchCriteria implements SearchCriteria {

   private SearchParameter serviceName;
   private SearchParameter id;
   private SearchParameter transaction;
   private SearchParameter standard;
   private SearchParameter simulatedRole;
   private SearchParameter testedRole;
   private SearchParameter shortDescription;
   private SearchParameter runnable;
   private SearchParameter valid;

   /**
    * Constructor.
    */
   public SequenceSearchCriteria() {
      // Empty
   }

   /**
    * Get the search parameter for the sequence/service name.
    *
    * @return the SearchParameter representing the name criterion
    */
   public SearchParameter getServiceName() {
      return serviceName;
   }

   /**
    * Set the search parameter for the sequence/service name.
    *
    * @param serviceName the SearchParameter to use for filtering by name
    * @return this SequenceSearchCriteria for method chaining
    */
   public SequenceSearchCriteria setServiceName(SearchParameter serviceName) {
      this.serviceName = serviceName;
      return this;
   }

   /**
    * Get the search parameter for the unique identifier of the sequence.
    *
    * @return the SearchParameter representing the identifier criterion
    */
   public SearchParameter getId() {
      return id;
   }

   /**
    * Set the search parameter for the unique identifier of the sequence.
    *
    * @param id the SearchParameter to use for filtering by identifier
    * @return this SequenceSearchCriteria for method chaining
    */
   public SequenceSearchCriteria setId(SearchParameter id) {
      this.id = id;
      return this;
   }

   /**
    * Get the search parameter for transactions involved in the sequence.
    *
    * @return the SearchParameter representing the transaction criterion
    */
   public SearchParameter getTransaction() {
      return transaction;
   }

   /**
    * Set the search parameter for transactions involved in the sequence.
    *
    * @param transaction the SearchParameter to use for filtering by transaction(s)
    * @return this SequenceSearchCriteria for method chaining
    */
   public SequenceSearchCriteria setTransaction(SearchParameter transaction) {
      this.transaction = transaction;
      return this;
   }

   /**
    * Get the search parameter for standards or profiles associated with the sequence.
    *
    * @return the SearchParameter representing the standard criterion
    */
   public SearchParameter getStandard() {
      return standard;
   }

   /**
    * Set the search parameter for standards or profiles associated with the sequence.
    *
    * @param standard the SearchParameter to use for filtering by standard(s)
    * @return this SequenceSearchCriteria for method chaining
    */
   public SequenceSearchCriteria setStandard(SearchParameter standard) {
      this.standard = standard;
      return this;
   }

   /**
    * Get the search parameter for the simulated role within the sequence.
    *
    * @return the SearchParameter representing the simulated role criterion
    */
   public SearchParameter getSimulatedRole() {
      return simulatedRole;
   }

   /**
    * Set the search parameter for the simulated role within the sequence.
    *
    * @param simulatedRole the SearchParameter to use for filtering by simulated role(s)
    * @return this SequenceSearchCriteria for method chaining
    */
   public SequenceSearchCriteria setSimulatedRole(SearchParameter simulatedRole) {
      this.simulatedRole = simulatedRole;
      return this;
   }

   /**
    * Get the search parameter for the tested role within the sequence.
    *
    * @return the SearchParameter representing the tested role criterion
    */
   public SearchParameter getTestedRole() {
      return testedRole;
   }

   /**
    * Set the search parameter for the tested role within the sequence.
    *
    * @param testedRole the SearchParameter to use for filtering by tested role(s)
    * @return this SequenceSearchCriteria for method chaining
    */
   public SequenceSearchCriteria setTestedRole(SearchParameter testedRole) {
      this.testedRole = testedRole;
      return this;
   }

   /**
    * Get the search parameter for a short textual description of the sequence.
    *
    * @return the SearchParameter representing the short description criterion
    */
   public SearchParameter getShortDescription() {
      return shortDescription;
   }

   /**
    * Set the search parameter for a short textual description of the sequence.
    *
    * @param shortDescription the SearchParameter to use for filtering by short description
    * @return this SequenceSearchCriteria for method chaining
    */
   public SequenceSearchCriteria setShortDescription(SearchParameter shortDescription) {
      this.shortDescription = shortDescription;
      return this;
   }

   /**
    * Get the search parameter indicating whether the sequence is runnable.
    *
    * @return the SearchParameter representing the runnable criterion
    */
   public SearchParameter getRunnable() {
      return runnable;
   }

   /**
    * Set the search parameter indicating whether the sequence is runnable.
    *
    * @param runnable the SearchParameter to use for filtering by runnable state
    * @return this SequenceSearchCriteria for method chaining
    */
   public SequenceSearchCriteria setRunnable(SearchParameter runnable) {
      this.runnable = runnable;
      return this;
   }

   /**
    * Get the search parameter indicating whether the sequence is valid.
    *
    * @return the SearchParameter representing the valid criterion
    */
   public SearchParameter getValid() {
      return valid;
   }

   /**
    * Set the search parameter indicating whether the sequence is valid.
    *
    * @param valid the SearchParameter to use for filtering by validity state
    * @return this SequenceSearchCriteria for method chaining
    */
   public SequenceSearchCriteria setValid(SearchParameter valid) {
      this.valid = valid;
      return this;
   }

   /**
    * Collect all non-null search parameters configured on this criteria object.
    *
    * @return a List of SearchParameter instances to be used when executing the search
    */
   @Override
   public List<SearchParameter> getSearchParameters() {
      return Stream.of(serviceName, id, transaction, standard, simulatedRole, testedRole, shortDescription, runnable, valid)
            .filter(Objects::nonNull)
            .toList();
   }

}
