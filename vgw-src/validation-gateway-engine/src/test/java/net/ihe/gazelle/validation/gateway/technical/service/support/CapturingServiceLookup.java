package net.ihe.gazelle.validation.gateway.technical.service.support;

import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.SearchService;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;

import java.util.ArrayList;
import java.util.List;

public class CapturingServiceLookup implements SearchService<DeployedService, ServiceSearchCriteria> {

   private SearchQuery<ServiceSearchCriteria> lastQuery;
   private SearchResult<DeployedService> result = new SearchResult<>(List.of(), 0, 0, 0);

   public CapturingServiceLookup withServices(List<DeployedService> services) {
      this.result = new SearchResult<>(new ArrayList<>(services), 0, services.size(), services.size());
      return this;
   }

   public SearchQuery<ServiceSearchCriteria> getLastQuery() {
      return lastQuery;
   }

   @Override
   public SearchResult<DeployedService> search(SearchQuery<ServiceSearchCriteria> query, GazelleIdentity identity) {
      this.lastQuery = query;
      return result;
   }

   @Override
   public SearchResult<DeployedService> search(SearchQuery<ServiceSearchCriteria> query, List<String> attributePaths,
                                              GazelleIdentity identity) {
      return search(query, identity);
   }
}
