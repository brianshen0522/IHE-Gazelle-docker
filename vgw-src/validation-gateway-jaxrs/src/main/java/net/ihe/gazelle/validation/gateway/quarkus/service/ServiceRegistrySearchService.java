package net.ihe.gazelle.validation.gateway.quarkus.service;

import net.ihe.gazelle.search.api.SearchException;
import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.SearchService;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;
import net.ihe.gazelle.serviceregistry.client.technical.rest.ServiceLookupClientImpl;
import net.ihe.gazelle.validation.gateway.quarkus.factory.ServiceRegistryLookup;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
@ServiceRegistryLookup
public class ServiceRegistrySearchService implements SearchService<DeployedService, ServiceSearchCriteria> {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistrySearchService.class);

    private final ServiceLookupClientImpl client;
    private final String serviceRegistryUrl;

    public ServiceRegistrySearchService(@ConfigProperty(name = "gzl.service.registry.url") String serviceRegistryUrl) {
        this.serviceRegistryUrl = serviceRegistryUrl;
        this.client = new ServiceLookupClientImpl(serviceRegistryUrl);
    }

    @Override
    public SearchResult<DeployedService> search(SearchQuery<ServiceSearchCriteria> query, GazelleIdentity identity) {
        try {
            return client.search(query);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Service registry lookup interrupted for url={}", serviceRegistryUrl, e);
            throw new SearchException("Interrupted while searching service registry.", e);
        } catch (RuntimeException e) {
            logger.error("Service registry lookup failed for url={}", serviceRegistryUrl, e);
            throw new SearchException("Service registry lookup failed for url=" + serviceRegistryUrl, e);
        }
    }

    @Override
    public SearchResult<DeployedService> search(SearchQuery<ServiceSearchCriteria> query, List<String> attributePaths,
                                               GazelleIdentity identity) {
        return search(query, identity);
    }
}
