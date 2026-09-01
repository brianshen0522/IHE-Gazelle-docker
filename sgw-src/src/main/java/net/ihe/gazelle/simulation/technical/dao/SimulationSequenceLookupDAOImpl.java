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

import com.google.common.collect.TreeMultimap;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ihe.gazelle.search.api.*;
import net.ihe.gazelle.simulation.business.LoadSimulationSequence;
import net.ihe.gazelle.simulation.business.SimulationSequenceLookupDAO;
import net.ihe.gazelle.simulation.business.model.SimulationSequenceExtended;
import net.ihe.gazelle.simulation.business.search.SequenceSearchCriteria;
import net.ihe.gazelle.simulation.business.sequence.SimulatedRole;
import net.ihe.gazelle.simulation.business.sequence.TestedRole;

import java.util.*;

import static net.ihe.gazelle.simulation.business.search.SequenceIndexService.*;

/**
 * Implementation of the {@link SimulationSequenceLookupDAO} interface that provides
 * mechanisms to query, initialize, and manage simulation sequences using in-memory indexed
 * data structures.
 * <p>
 * This data access object (DAO) uses various indexes to efficiently filter and sort
 * simulation sequences based on multi-field criteria such as service name, transaction type,
 * standard, roles, and other attributes.
 * <p>
 * It integrates with {@link LoadSimulationSequence} to load, initialize, and reset simulation
 * sequences as well.
 * <p>
 * The class supports advanced query operations, including sorting and pagination, while ensuring
 * efficient lookups and appropriate validation of input parameters.
 * <p>
 * Thread-safety of this implementation relies upon the inherent thread-safety of the underlying
 * data structures (e.g., TreeMultimap) and the design choice to clear or manage indexes
 * atomically during lifecycle operations.
 */
@DirectDAO
@ApplicationScoped
public class SimulationSequenceLookupDAOImpl implements SimulationSequenceLookupDAO {

   private final TreeMultimap<String, String> serviceNameIndex = TreeMultimap.create(
         Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER), String.CASE_INSENSITIVE_ORDER
   );
   private final TreeMap<String, SimulationSequenceExtended> idIndex = new TreeMap<>(
         String.CASE_INSENSITIVE_ORDER
   );
   private final TreeMultimap<String, String> supportedTransactionIndex = TreeMultimap.create(
         Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER), String.CASE_INSENSITIVE_ORDER
   );
   private final TreeMultimap<String, String> standardIndex = TreeMultimap.create(
         Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER), String.CASE_INSENSITIVE_ORDER
   );
   private final TreeMultimap<String, String> simulatedRoleIndex = TreeMultimap.create(
         Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER), String.CASE_INSENSITIVE_ORDER
   );
   private final TreeMultimap<String, String> testedRoleIndex = TreeMultimap.create(
         Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER), String.CASE_INSENSITIVE_ORDER
   );
   private final TreeMultimap<String, String> shortDescriptionIndex = TreeMultimap.create(
         Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER), String.CASE_INSENSITIVE_ORDER
   );
   private final TreeMultimap<Boolean, String> runnableIndex = TreeMultimap.create(
         Comparator.nullsLast(Comparator.naturalOrder()), Comparator.naturalOrder()
   );
   private final TreeMultimap<Boolean, String> validIndex = TreeMultimap.create(
         Comparator.nullsLast(Comparator.naturalOrder()), Comparator.naturalOrder()
   );

   private final LoadSimulationSequence loadSimulationSequence;

   /**
    * Constructs an instance of SimulationSequenceLookupDAOImpl with the given LoadSimulationSequence implementation.
    *
    * @param loadSimulationSequence An implementation of the LoadSimulationSequence interface.
    *                               Used to load supported simulation sequences from registered simulation services.
    */
   @Inject
   public SimulationSequenceLookupDAOImpl(LoadSimulationSequence loadSimulationSequence) {
      this.loadSimulationSequence = loadSimulationSequence;
   }

   @Override
   public List<String> getPossibleValues(IndexedField indexedField, SequenceSearchCriteria searchQuery) {
      Set<String> possibleValuesSet = new LinkedHashSet<>();
      List<SimulationSequenceExtended> results = searchAvailableSequences(searchQuery, null);

      switch (indexedField.getName()) {
         case ID -> results.forEach(s -> addIFNotNull(possibleValuesSet, s.getId()));
         case SERVICE_NAME -> results.forEach(s -> addIFNotNull(possibleValuesSet, s.getSimulatorName()));
         case TRANSACTION -> results.forEach(s -> addAllIFNotNull(possibleValuesSet, s.getTransactions()));
         case STANDARD -> results.forEach(s -> addAllIFNotNull(possibleValuesSet, s.getStandards()));
         case SIMULATED_ROLE -> results.forEach(s -> addAllIFNotNull(
                     possibleValuesSet,
                     s.getSimulatedRoles()
                           .stream()
                           .map(SimulatedRole::getName)
                           .toList()
               )
         );
         case TESTED_ROLE -> results.forEach(s -> addAllIFNotNull(
                     possibleValuesSet,
                     s.getTestedRoles()
                           .stream()
                           .map(TestedRole::getName)
                           .toList()
               )
         );
         case SHORT_DESCRIPTION -> results.forEach(s -> addIFNotNull(possibleValuesSet, s.getShortDescription()));
         case RUNNABLE, VALID -> {
            possibleValuesSet.add(Boolean.TRUE.toString());
            possibleValuesSet.add(Boolean.FALSE.toString());
         }
         default -> throw new UnknownSearchParameterException("Unsupported indexed field: " + indexedField.getName());
      }
      return new ArrayList<>(possibleValuesSet);
   }

   @Override
   public SearchResult<SimulationSequenceExtended> searchWithSortingAndPagination(SequenceSearchCriteria searchQuery, Range range, List<Sort> sortParameters) {
      List<SimulationSequenceExtended> results = searchAvailableSequences(searchQuery, sortParameters);
      try {
         int limit = range.getLimit();
         int offset = range.getOffset();

         int totalResults = results.size();
         int startIndex = Math.max(0, offset);
         int endIndex = Math.min(startIndex + limit, totalResults);
         results = results.subList(startIndex, endIndex);

         return new SearchResult<>(results, offset, limit, totalResults);
      } catch (Exception e) {
         throw new SearchException("Error while reading sequences", e);
      }
   }

   @Override
   public SimulationSequenceExtended getSimulationSequenceById(String id) {
      if (!idIndex.containsKey(id)) {
         throw new NoSuchElementException("The SimulationSequence " + id + " does not exist.");
      }
      return idIndex.get(id);
   }

   @Override
   public void init() {
      loadSimulationSequence.getSupportedSequences().forEach(this::registerSequence);
   }

   @Override
   public void reset() {
      clear();
      init();
   }

   private void clear() {
      serviceNameIndex.clear();
      idIndex.clear();
      supportedTransactionIndex.clear();
      standardIndex.clear();
      simulatedRoleIndex.clear();
      testedRoleIndex.clear();
      shortDescriptionIndex.clear();
      runnableIndex.clear();
      validIndex.clear();
   }

   private void registerSequence(SimulationSequenceExtended sequence) {
      String sequenceId = sequence.getId();
      serviceNameIndex.put(sequence.getSimulatorName(), sequenceId);
      idIndex.put(sequence.getId(), sequence);
      boolean isRunnable = sequence.isRunnable() && sequence.isValid();
      for (String transaction : sequence.getTransactions()) {
         supportedTransactionIndex.put(transaction, sequenceId);
      }
      for (String standard : sequence.getStandards()) {
         standardIndex.put(standard, sequenceId);
      }
      for (SimulatedRole simulatedRole : sequence.getSimulatedRoles()) {
         simulatedRoleIndex.put(simulatedRole.getName(), sequenceId);
      }
      for (TestedRole testedRole : sequence.getTestedRoles()) {
         testedRoleIndex.put(testedRole.getName(), sequenceId);
      }
      shortDescriptionIndex.put(sequence.getShortDescription(), sequenceId);
      runnableIndex.put(isRunnable, sequenceId);
      validIndex.put(sequence.isValid(), sequenceId);
   }

   private void addIFNotNull(Set<String> possibleValuesSet, String value) {
      if (value != null) {
         possibleValuesSet.add(value);
      }
   }

   private void addAllIFNotNull(Set<String> possibleValuesSet, List<String> values) {
      if (values != null) {
         possibleValuesSet.addAll(values);
      }
   }

   private List<SimulationSequenceExtended> searchAvailableSequences(SequenceSearchCriteria searchQuery, List<Sort> sortParameters) {
      Collection<SimulationSequenceExtended> resultCollection = applySort(sortParameters);
      try {
         Set<SimulationSequenceExtended> result = new LinkedHashSet<>(resultCollection);
         retainAllMatches(serviceNameIndex, searchQuery.getServiceName(), result);
         retainAllMatchesIdIndex(idIndex, searchQuery.getId(), result);
         retainAllMatches(supportedTransactionIndex, searchQuery.getTransaction(), result);
         retainAllMatches(standardIndex, searchQuery.getStandard(), result);
         retainAllMatches(simulatedRoleIndex, searchQuery.getSimulatedRole(), result);
         retainAllMatches(testedRoleIndex, searchQuery.getTestedRole(), result);
         retainAllMatches(shortDescriptionIndex, searchQuery.getShortDescription(), result);
         retainAllBoolean(runnableIndex, searchQuery.getRunnable(), result);
         retainAllBoolean(validIndex, searchQuery.getValid(), result);
         return applySortOrder(result.stream().toList(), sortParameters);
      } catch (Exception e) {
         throw new SearchException("Error while reading sequences", e);
      }
   }

   private Collection<SimulationSequenceExtended> applySort(List<Sort> sortParameters) {
      if (sortParameters == null || sortParameters.isEmpty()) {
         return idIndex.values();
      }
      return switch (Objects.requireNonNullElse(sortParameters.getFirst().field(), ID)) {
         case SERVICE_NAME -> getSequencesFromIdIndex(serviceNameIndex.values());
         case TRANSACTION -> getSequencesFromIdIndex(supportedTransactionIndex.values());
         case STANDARD -> getSequencesFromIdIndex(standardIndex.values());
         case SIMULATED_ROLE -> getSequencesFromIdIndex(simulatedRoleIndex.values());
         case TESTED_ROLE -> getSequencesFromIdIndex(testedRoleIndex.values());
         case RUNNABLE -> getSequencesFromIdIndex(runnableIndex.values());
         case VALID -> getSequencesFromIdIndex(validIndex.values());
         case ID -> idIndex.values();
         default -> throw new UnknownSortParameterException("Unknown sort field: " + sortParameters.getFirst().field());
      };
   }

   private List<SimulationSequenceExtended> applySortOrder(List<SimulationSequenceExtended> result, List<Sort> sortParameters) {
      if (sortParameters != null && !sortParameters.isEmpty()) {
         return Sort.Order.DESCENDING.equals(Objects.requireNonNullElse(sortParameters.getFirst().order(), Sort.Order.ASCENDING))
               ? result.reversed()
               : result;
      }
      return result;
   }

   private void retainAllMatchesIdIndex(TreeMap<String, SimulationSequenceExtended> index, SearchParameter search, Set<SimulationSequenceExtended> result) {
      if (search != null) {
         result.retainAll(getMatchesContainsIgnoreCaseIdIndex(index, String.valueOf(search.getFirstValue())));
      }
   }

   private void retainAllMatches(TreeMultimap<String, String> index, SearchParameter search, Set<SimulationSequenceExtended> result) {
      if (search != null) {
         result.retainAll(getMatchesContainsIgnoreCase(index, String.valueOf(search.getFirstValue())));
      }
   }

   private void retainAllBoolean(TreeMultimap<Boolean, String> index, SearchParameter search, Set<SimulationSequenceExtended> result) {
      if (search != null) {
         Set<SimulationSequenceExtended> matches = new LinkedHashSet<>();
         index.keySet()
               .stream()
               .filter(key -> Objects.equals(key, Boolean.parseBoolean(String.valueOf(search.getFirstValue()))))
               .forEach(key -> matches.addAll(getSequencesFromIdIndex(index.get(key))));
         result.retainAll(matches);
      }
   }

   private Set<SimulationSequenceExtended> getMatchesContainsIgnoreCaseIdIndex(TreeMap<String, SimulationSequenceExtended> index, String search) {
      Set<SimulationSequenceExtended> result = new LinkedHashSet<>();
      index.keySet()
            .stream()
            .filter(key -> containsIgnoreCase(key, search))
            .forEach(key -> result.add(index.get(key)));
      return result;
   }

   private Set<SimulationSequenceExtended> getMatchesContainsIgnoreCase(TreeMultimap<String, String> index, String search) {
      Set<SimulationSequenceExtended> result = new LinkedHashSet<>();
      index.keySet()
            .stream()
            .filter(key -> containsIgnoreCase(key, search))
            .forEach(key -> result.addAll(getSequencesFromIdIndex(index.get(key))));
      return result;
   }

   private Collection<SimulationSequenceExtended> getSequencesFromIdIndex(Collection<String> ids) {
      Collection<SimulationSequenceExtended> resultCollection = new LinkedHashSet<>();
      for (String id : ids) {
         SimulationSequenceExtended simulationSequenceExtended = idIndex.get(id);
         if (simulationSequenceExtended != null) {
            resultCollection.add(simulationSequenceExtended);
         }
      }
      return resultCollection;
   }

   private boolean containsIgnoreCase(String item, String search) {
      return item != null && item.toLowerCase().contains(search.toLowerCase());
   }
}
