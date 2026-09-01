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

import net.ihe.gazelle.search.api.IndexedField;
import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceIndexService;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;

import java.util.List;
import java.util.Map;

import static net.ihe.gazelle.serviceregistry.business.permission.ServiceRegistryPermissionStore.PERMISSION_SERVICE_READ;

/**
 * Implementation of the ServiceLookup interface, providing methods to search for deployed services.
 * This class interacts with the ServiceLookupDAO to perform the actual search operations.
 */
public class ServiceLookupImpl implements ServiceLookup {

   /**
    * Default offset for search queries.
    */
   public static final int DEFAULT_OFFSET = 0;
   /**
    * Default limit for search queries.
    */
   public static final int DEFAULT_LIMIT = 25;

   private static final ServiceIndexService INDEX_SERVICE = new ServiceIndexService();

   private final ServiceLookupDAO dao;
   private final Authz authz;

   /**
    * Constructor for ServiceLookupImpl.
    *
    * @param dao the DAO used to perform service lookups
    * @param authz the authorization service used to check permissions
    */
   public ServiceLookupImpl(ServiceLookupDAO dao, Authz authz) {
      this.dao = dao;
      this.authz = authz;
   }

   @Override
   public Map<String, IndexedField> getIndexes() {
      return INDEX_SERVICE.getIndexes();
   }

   @Override
   public List<String> getSuggestions(String field, ServiceSearchCriteria criteria, GazelleIdentity identity) {
       authz.assertAuthorized(identity, PERMISSION_SERVICE_READ);
       if (field == null || field.trim().isEmpty()) {
           return List.of();
       }
       return dao.getSuggestions(field, criteria);
   }

   @Override
   public SearchResult<DeployedService> search(SearchQuery<ServiceSearchCriteria> query, GazelleIdentity identity) {
      if(query == null) {
         query = new SearchQuery<>(new ServiceSearchCriteria(), newDefaultRange(), null); // Default to no criteria if query is null
      } else {
         query = new SearchQuery<>(
                 query.searchCriteria() != null ? query.searchCriteria() : new ServiceSearchCriteria(),
                 query.range() != null ? query.range() : newDefaultRange(),
                 query.sorts()
         );
      }
      Range.validateRange(query.range());
      SearchResult<DeployedService> deployedServiceSearchResult = dao.search(query.searchCriteria(), query.range(), query.sorts());
      return filterServiceAttributesBasedOnAuthorization(deployedServiceSearchResult, identity);

   }

   private SearchResult<DeployedService> filterServiceAttributesBasedOnAuthorization(SearchResult<DeployedService> deployedServiceSearchResult, GazelleIdentity identity) {
      if (authz.isAuthorized(identity, PERMISSION_SERVICE_READ))
         return deployedServiceSearchResult;
      else {
         // For unauthorized users, we hide version and status of services
         List<DeployedService> filteredServices = deployedServiceSearchResult.objects().stream()
               .map(service -> {
                  DeployedService deployedService = new DeployedService(service);
                  deployedService.setVersion(null);
                  deployedService.setStatus(null);
                  return deployedService;
               })
               .toList();
         return new SearchResult<>(filteredServices, deployedServiceSearchResult.offset(), deployedServiceSearchResult.limit(), deployedServiceSearchResult.totalObjects());
      }
   }

   private static Range newDefaultRange() {
      return new Range().setOffset(DEFAULT_OFFSET).setLimit(DEFAULT_LIMIT);
   }

   @Override
   public SearchResult<DeployedService> search(SearchQuery<ServiceSearchCriteria> query, List<String> attributePaths, GazelleIdentity gazelleIdentity) {
      throw new UnsupportedOperationException("Presentation schema search not implemented");
   }

}
