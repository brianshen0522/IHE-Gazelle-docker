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

package net.ihe.gazelle.serviceregistry.api.business.lookup;

import net.ihe.gazelle.search.api.SearchCriteria;
import net.ihe.gazelle.search.api.SearchParameter;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


/**
 * ServiceSearchCriteria defines the search parameters for querying services in the Service Registry. It implements the
 * SearchCriteria interface and provides methods to set and retrieve search parameters.
 */
public class ServiceSearchCriteria implements SearchCriteria {

   private SearchParameter nameParam = null;
   private SearchParameter instanceIdParam = null;
   private SearchParameter selfRegisteredParam = null;
   private SearchParameter statusParam = null;
   private SearchParameter providedInterfaceParam = null;
   private SearchParameter consumedInterfaceParam = null;

   /**
    * Default constructor for ServiceSearchCriteria. Initializes the search parameters to null.
    */
   public ServiceSearchCriteria() {
      // Default constructor
   }

   /**
    * Get the search parameter for the service name.
    *
    * @return the search parameter for the service name
    */
   public SearchParameter getName() {
      return nameParam;
   }

   /**
    * Set the name values to search for in services.
    *
    * @param names the name values to search for in services. Multiple values represent an OR condition.
    *
    * @return this ServiceSearchCriteria instance for method chaining
    */
   public ServiceSearchCriteria setName(String... names) {
      this.nameParam = new SearchParameter()
            .setName(ServiceIndexService.NAME)
            .setValues(Arrays.asList((Object[]) names));
      return this;
   }

   /**
    * Get the search parameter for the service instance ID.
    *
    * @return the search parameter for the service instance ID
    */
   public SearchParameter getInstanceId() {
      return instanceIdParam;
   }

   /**
    * Set the instanceId values to search for in services.
    *
    * @param instanceIds the instanceId values to search for in services. multiple values represent an OR condition.
    *
    * @return this ServiceSearchCriteria instance for method chaining
    */
   public ServiceSearchCriteria setInstanceId(String... instanceIds) {
      this.instanceIdParam = new SearchParameter()
            .setName(ServiceIndexService.INSTANCE_ID)
            .setValues(Arrays.asList((Object[]) instanceIds));
      return this;
   }

   /**
    * Get the search parameter for self-registered services.
    *
    * @return the search parameter for self-registered services
    */
   public SearchParameter getSelfRegistered() {
      return selfRegisteredParam;
   }

   /**
    * Set the selfRegistered value to search for in services.
    *
    * @param selfRegistered the selfRegistered value to search for in services.
    *
    * @return this ServiceSearchCriteria instance for method chaining
    */
   public ServiceSearchCriteria setSelfRegistered(boolean selfRegistered) {
      this.selfRegisteredParam = new SearchParameter()
            .setName(ServiceIndexService.SELF_REGISTERED)
            .setValue(selfRegistered);
      return this;
   }

   /**
    * Get the search parameter for the service status.
    *
    * @return the search parameter for the service status
    */
   public SearchParameter getStatus() {
      return statusParam;
   }

   /**
    * Set the status values to search for in services
    *
    * @param status the status values to search for in services. Multiple values represent an OR condition.
    *
    * @return this ServiceSearchCriteria instance for method chaining
    */
   public ServiceSearchCriteria setStatus(Status... status) {
      this.statusParam = new SearchParameter()
            .setName(ServiceIndexService.STATUS)
            .setValues(Arrays.asList((Object[]) status));
      return this;
   }

   /**
    * Get the search parameter for the provided interface.
    *
    * @return the search parameter for the provided interface
    */
   public SearchParameter getProvidedInterface() {
      return providedInterfaceParam;
   }

   /**
    * Set the providedInterface name values to search for in services.
    *
    * @param providedInterfaces the providedInterface name values to search for in services. Multiple values represent
    *                           an OR condition.
    *
    * @return this ServiceSearchCriteria instance for method chaining
    */
   public ServiceSearchCriteria setProvidedInterface(String... providedInterfaces) {
      this.providedInterfaceParam = new SearchParameter()
            .setName(ServiceIndexService.PROVIDED_INTERFACE)
            .setValues(Arrays.asList((Object[]) providedInterfaces));
      return this;
   }

   /**
    * Get the search parameter for the consumed interface.
    *
    * @return the search parameter for the consumed interface
    */
   public SearchParameter getConsumedInterface() {
      return consumedInterfaceParam;
   }

   /**
    * Set the consumedInterface name values to search for in services.
    *
    * @param consumedInterfaces the consumedInterface name values to search for in services. Multiple values represent
    *                           an OR condition.
    *
    * @return this ServiceSearchCriteria instance for method chaining
    */
   public ServiceSearchCriteria setConsumedInterface(String... consumedInterfaces) {
      this.consumedInterfaceParam = new SearchParameter()
              .setName(ServiceIndexService.CONSUMED_INTERFACE)
              .setValues(Arrays.asList((Object[]) consumedInterfaces));
      return this;
   }

   @Override
   public List<SearchParameter> getSearchParameters() {
      return Stream.of(nameParam, instanceIdParam, selfRegisteredParam, statusParam, providedInterfaceParam)
            .filter(Objects::nonNull).toList();
   }

}
