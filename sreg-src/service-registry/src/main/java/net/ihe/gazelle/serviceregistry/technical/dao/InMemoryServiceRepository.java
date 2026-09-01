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

package net.ihe.gazelle.serviceregistry.technical.dao;

import com.google.common.collect.TreeMultimap;
import net.ihe.gazelle.search.api.*;
import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterface;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.ServiceId;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;
import net.ihe.gazelle.serviceregistry.business.lookup.ServiceLookupDAO;
import net.ihe.gazelle.serviceregistry.business.registration.ServiceRegistrationDAO;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.ihe.gazelle.search.api.Sort.Order.ASCENDING;
import static net.ihe.gazelle.search.api.Sort.Order.DESCENDING;
import static net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceIndexService.*;

/**
 * In-memory implementation of the ServiceRegistrationDAO interface. Can only be used if single application instance.
 * Must not be used in a clustered environment.
 */
public class InMemoryServiceRepository implements ServiceRegistrationDAO, ServiceLookupDAO {

   // service ids must be unique per service, so it's a simple TreeMap instead of a TreeMultimap for other attributes.
   private static final TreeMap<ServiceId, DeployedService> ID_INDEX = new TreeMap<>(Comparator.naturalOrder());

   private static final TreeMultimap<String, DeployedService> NAME_INDEX = TreeMultimap.create(
         Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER), Comparator.comparing(ServiceId::new));
   private static final TreeMultimap<String, DeployedService> INSTANCE_ID_INDEX = TreeMultimap.create(
         Comparator.nullsLast(Comparator.naturalOrder()), Comparator.comparing(ServiceId::new));
   private static final TreeMultimap<Boolean, DeployedService> SELF_REGISTERED_INDEX = TreeMultimap.create(
         Comparator.reverseOrder(), Comparator.comparing(ServiceId::new));
   private static final TreeMultimap<String, DeployedService> STATUS_INDEX = TreeMultimap.create(
         Comparator.naturalOrder(), Comparator.comparing(ServiceId::new));
   private static final TreeMultimap<String, DeployedService> PROVIDED_INTERFACE_INDEX = TreeMultimap.create(
         Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER), Comparator.comparing(ServiceId::new));
   private static final TreeMultimap<String, DeployedService> CONSUMED_INTERFACE_INDEX = TreeMultimap.create(
           Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER), Comparator.comparing(ServiceId::new));

   /**
    * Default constructor for InMemoryServiceRepository.
    */
   public InMemoryServiceRepository() {
      // Default constructor
   }

   @Override
   public synchronized void create(DeployedService service) {
      DeployedService copy = new DeployedService(service); // copy to avoid flooding with external state changes.
      ID_INDEX.put(new ServiceId(copy), copy);
      NAME_INDEX.put(copy.getName(), copy);
      INSTANCE_ID_INDEX.put(copy.getInstanceId(), copy);
      SELF_REGISTERED_INDEX.put(copy.isSelfRegistered(), copy);
      STATUS_INDEX.put(copy.getStatus().name(), copy);
      copy.getProvidedInterfaces().forEach(
            providedInterface -> PROVIDED_INTERFACE_INDEX.put(providedInterface.getInterfaceName(), copy)
      );
      copy.getConsumedInterfaces().forEach(
              consumedInterface -> CONSUMED_INTERFACE_INDEX.put(consumedInterface.getInterfaceName(), copy)
      );
   }

   @Override
   public boolean isServiceRegistered(ServiceId serviceId) {
      return ID_INDEX.containsKey(serviceId);
   }

   @Override
   public DeployedService read(ServiceId serviceId) {
      return new DeployedService(ID_INDEX.get(serviceId)); // Return a copy to avoid leaking internal state
   }

   @Override
   public synchronized void update(DeployedService service) {
      delete(new ServiceId(service));
      create(service);
   }

   @Override
   public synchronized void delete(ServiceId serviceId) {
      DeployedService service = ID_INDEX.remove(serviceId);
      if (service != null) {
         NAME_INDEX.remove(service.getName(), service);
         INSTANCE_ID_INDEX.remove(service.getInstanceId(), service);
         SELF_REGISTERED_INDEX.remove(service.isSelfRegistered(), service);
         STATUS_INDEX.remove(service.getStatus().name(), service);
         service.getProvidedInterfaces().forEach(
               providedInterface -> PROVIDED_INTERFACE_INDEX.remove(providedInterface.getInterfaceName(), service)
         );
         service.getConsumedInterfaces().forEach(
                 consumedInterface -> CONSUMED_INTERFACE_INDEX.remove(consumedInterface.getInterfaceName(), service)
         );
      }
   }

   @Override
   public List<DeployedService> getSelfRegisteredServices() {
      return searchAllServices(new ServiceSearchCriteria().setSelfRegistered(true), null);
   }

   @Override
   public SearchResult<DeployedService> search(ServiceSearchCriteria criteria, Range range,
                                               List<Sort> sorts) {
      List<DeployedService> allResults = searchAllServices(criteria, sorts);
      int startIndex = Math.max(0, range.getOffset());
      int endIndex = Math.min(startIndex + range.getLimit(), allResults.size());
      return new SearchResult<>(
            allResults.subList(startIndex, endIndex),
            range.getOffset(),
            range.getLimit(),
            allResults.size()
      );
   }

   /**
    * Empty all data of the in-memory service repository. This method is used by the unit-tests.
    */
   public synchronized void dropAll() {
      ID_INDEX.clear();
      NAME_INDEX.clear();
      INSTANCE_ID_INDEX.clear();
      SELF_REGISTERED_INDEX.clear();
      STATUS_INDEX.clear();
      PROVIDED_INTERFACE_INDEX.clear();
      CONSUMED_INTERFACE_INDEX.clear();
   }

   private List<DeployedService> searchAllServices(ServiceSearchCriteria criteria, List<Sort> sorts) {
      Collection<DeployedService> resultCollection = applySort(sorts);
      try {
         Set<DeployedService> result = new LinkedHashSet<>(resultCollection);
         retainAllStringMatches(NAME_INDEX, criteria.getName(), result);
         retainAllStringMatches(INSTANCE_ID_INDEX, criteria.getInstanceId(), result);
         retainAllObjectMatches(SELF_REGISTERED_INDEX, criteria.getSelfRegistered(), result);
         retainAllStringMatches(STATUS_INDEX, criteria.getStatus(), result);
         retainAllStringMatches(PROVIDED_INTERFACE_INDEX, criteria.getProvidedInterface(), result);
         retainAllStringMatches(CONSUMED_INTERFACE_INDEX, criteria.getConsumedInterface(), result);
         return applySortOrder(result.stream().toList(), sorts);
      } catch (Exception e) {
         throw new RuntimeException("Error while searching services", e);
      }
   }

   private Collection<DeployedService> applySort(List<Sort> sorts) {
      if (sorts == null || sorts.isEmpty()) {
         return NAME_INDEX.values();
      }
      Collection<DeployedService> resultCollection;
      switch (Objects.requireNonNullElse(sorts.getFirst().field(), NAME)) {
         case NAME -> resultCollection = NAME_INDEX.values();
         case INSTANCE_ID -> resultCollection = INSTANCE_ID_INDEX.values();
         case SELF_REGISTERED -> resultCollection = SELF_REGISTERED_INDEX.values();
         case STATUS -> resultCollection = STATUS_INDEX.values();
         case PROVIDED_INTERFACE -> resultCollection = PROVIDED_INTERFACE_INDEX.values();
         case CONSUMED_INTERFACE -> resultCollection = CONSUMED_INTERFACE_INDEX.values();
         default -> throw new UnknownSortParameterException(
               "Unknown sort field: " + sorts.getFirst().field());
      }
      return resultCollection;
   }

   private List<DeployedService> applySortOrder(List<DeployedService> result, List<Sort> sorts) {
      if (sorts != null && !sorts.isEmpty()) {
         return DESCENDING.equals(Objects.requireNonNullElse(sorts.getFirst().order(), ASCENDING)) ?
               result.reversed() :
               result;
      }
      return result;
   }

   private void retainAllStringMatches(TreeMultimap<String, DeployedService> index, SearchParameter search,
                                       Set<DeployedService> result) {
      if (search != null) {
         Set<Object> values = new HashSet<>(search.getValues());
         result.retainAll(
               getMatches(index, key -> values.stream().anyMatch(
                     value -> containsStringIgnoreCase(key, String.valueOf(value))
               ))
         );
      }
   }

   private <K> void retainAllObjectMatches(TreeMultimap<K, DeployedService> index, SearchParameter search,
                                        Set<DeployedService> result) {
      if (search != null) {
         Set<Object> values = new HashSet<>(search.getValues());
         result.retainAll(
               getMatches(index, values::contains)
         );
      }
   }

   private <K> Set<DeployedService> getMatches(TreeMultimap<K, DeployedService> index, Predicate<K> filter) {
      Set<DeployedService> result = new LinkedHashSet<>();
      index.keySet().stream()
            .filter(filter)
            .forEach(key -> result.addAll(index.get(key)));
      return result;
   }

   private boolean containsStringIgnoreCase(String item, String search) {
      return item != null && item.toLowerCase().contains(search.toLowerCase());
   }

   @Override
   public List<String> getSuggestions(String field, ServiceSearchCriteria criteria) {
      List<DeployedService> filteredServices = getServicesMatchingCriteriaExceptConcernedField(criteria, field);
      Set<String> suggestions = extractSuggestionsForField(field, filteredServices);

      return suggestions.stream()
            .filter(Objects::nonNull)
            .sorted(Comparator.naturalOrder())
            .toList();
   }

   /**
    * Extract suggestions for a specific field from the list of services.
    *
    * @param field the field to extract suggestions for
    * @param services the list of services to extract from
    * @return a set of unique suggestions
    */
   private Set<String> extractSuggestionsForField(String field, List<DeployedService> services) {
       Stream<DeployedService> serviceStream = services.stream();

      if (NAME.equalsIgnoreCase(field)) {
         return serviceStream.map(Service::getName).collect(Collectors.toSet());
      } else if (INSTANCE_ID.equalsIgnoreCase(field)) {
         return serviceStream.map(Service::getInstanceId).collect(Collectors.toSet());
      } else if (SELF_REGISTERED.equalsIgnoreCase(field)) {
          return serviceStream
                  .map(service -> String.valueOf(service.isSelfRegistered()))
                  .collect(Collectors.toSet());
      } else if (STATUS.equalsIgnoreCase(field)) {
         return serviceStream.map(service -> service.getStatus().name()).collect(Collectors.toSet());
      } else if (PROVIDED_INTERFACE.equalsIgnoreCase(field)) {
         return extractProvidedInterfaceSuggestions(serviceStream);
      } else if (CONSUMED_INTERFACE.equalsIgnoreCase(field)) {
         return extractConsumedInterfaceSuggestions(serviceStream);
      }

      return Set.of();
   }

   private Set<String> extractProvidedInterfaceSuggestions(Stream<DeployedService> services) {
      return services
            .flatMap(service -> service.getProvidedInterfaces().stream())
            .map(ProvidedInterface::getInterfaceName)
            .collect(Collectors.toSet()
      );
   }

   private Set<String> extractConsumedInterfaceSuggestions(Stream<DeployedService> services) {
      return services
              .flatMap(service -> service.getConsumedInterfaces().stream())
              .map(ConsumedInterface::getInterfaceName)
              .collect(Collectors.toSet()
              );
   }

   /**
    * Get services matching the criteria, excluding the specified field from filtering.
    * This allows getting suggestions for a field based on other criteria.
    *
    * @param criteria the search criteria
    * @param excludeField the field to exclude from filtering
    * @return list of services matching the criteria
    */
   private List<DeployedService> getServicesMatchingCriteriaExceptConcernedField(ServiceSearchCriteria criteria, String excludeField) {
      if (criteria == null) {
         return ID_INDEX.values().stream().toList();
      }

      Set<DeployedService> result = new LinkedHashSet<>(ID_INDEX.values());
      String lowerExcludeField = excludeField != null ? excludeField.toLowerCase() : "";

      // Apply filters for all fields except the excluded one
      if (!NAME.equalsIgnoreCase(lowerExcludeField) && criteria.getName() != null) {
         retainAllStringMatches(NAME_INDEX, criteria.getName(), result);
      }
      if (!INSTANCE_ID.equalsIgnoreCase(lowerExcludeField) && criteria.getInstanceId() != null) {
         retainAllStringMatches(INSTANCE_ID_INDEX, criteria.getInstanceId(), result);
      }
      if (!SELF_REGISTERED.equalsIgnoreCase(lowerExcludeField) && criteria.getSelfRegistered() != null) {
         retainAllObjectMatches(SELF_REGISTERED_INDEX, criteria.getSelfRegistered(), result);
      }
      if (!STATUS.equalsIgnoreCase(lowerExcludeField) && criteria.getStatus() != null) {
         retainAllStringMatches(STATUS_INDEX, criteria.getStatus(), result);
      }
      if (!PROVIDED_INTERFACE.equalsIgnoreCase(lowerExcludeField) && criteria.getProvidedInterface() != null) {
         retainAllStringMatches(PROVIDED_INTERFACE_INDEX, criteria.getProvidedInterface(), result);
      }
      if (!CONSUMED_INTERFACE.equalsIgnoreCase(lowerExcludeField) && criteria.getConsumedInterface() != null) {
         retainAllStringMatches(CONSUMED_INTERFACE_INDEX, criteria.getConsumedInterface(), result);
      }

      return result.stream().toList();
   }
}
