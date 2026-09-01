package net.ihe.gazelle.validation.gateway.app.itmock;

import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchException;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.SearchService;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;
import net.ihe.gazelle.validation.gateway.quarkus.factory.ServiceRegistryLookup;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.util.List;

@ApplicationScoped
@Alternative
@Priority(1)
@ServiceRegistryLookup
public class TestServiceRegistryLookup implements SearchService<DeployedService, ServiceSearchCriteria> {

    private static final String EMPTY_PROFILES_SERVICE_URL = "http://localhost:9/empty-validator/rest";

    private final String validationServiceUrl;
    private final String modelBasedServiceUrl;
    private final String serviceRegistryUrl;

    public TestServiceRegistryLookup(
          @ConfigProperty(name = "validation.service.url") String validationServiceUrl,
          @ConfigProperty(name = "validation.service.mb.url") String modelBasedServiceUrl,
          @ConfigProperty(name = "gzl.service.registry.url") String serviceRegistryUrl) {
        this.validationServiceUrl = validationServiceUrl;
        this.modelBasedServiceUrl = modelBasedServiceUrl;
        this.serviceRegistryUrl = serviceRegistryUrl;
    }

    @Override
    public SearchResult<DeployedService> search(SearchQuery<ServiceSearchCriteria> query, GazelleIdentity identity) {
        if (serviceRegistryUrl != null && serviceRegistryUrl.contains("localhost:1")) {
            throw new SearchException("Service registry lookup failed for url=" + serviceRegistryUrl);
        }
        List<DeployedService> services = List.of(
              buildV2Service(),
              buildModelBasedService(),
              buildEmptyProfilesService()
        );
        return new SearchResult<>(services, 0, services.size(), services.size());
    }

    @Override
    public SearchResult<DeployedService> search(SearchQuery<ServiceSearchCriteria> query,
                                                List<String> attributePaths,
                                                GazelleIdentity identity) {
        return search(query, identity);
    }

    private DeployedService buildV2Service() {
        HttpRestBinding restBinding = new HttpRestBinding().setServiceUrl(validationServiceUrl);
        ProvidedInterface providedInterface = new ProvidedInterface()
              .setInterfaceName("Validation Service API")
              .setInterfaceVersion("2.0.0")
              .setBindings(List.of(restBinding));

        return (DeployedService) new DeployedService()
              .setStatus(DeployedService.Status.AVAILABLE)
              .setName("mock-validation-service")
              .setProvidedInterfaces(List.of(providedInterface));
    }

    private DeployedService buildModelBasedService() {
        HttpRestBinding restBinding = new HttpRestBinding().setServiceUrl(modelBasedServiceUrl);
        ProvidedInterface providedInterface = new ProvidedInterface()
              .setInterfaceName("ModelBasedValidationWSService")
              .setInterfaceVersion("1.0.0")
              .setBindings(List.of(restBinding));

        return (DeployedService) new DeployedService()
              .setStatus(DeployedService.Status.AVAILABLE)
              .setName("mock-model-based-service")
              .setProvidedInterfaces(List.of(providedInterface));
    }

    private DeployedService buildEmptyProfilesService() {
        HttpRestBinding restBinding = new HttpRestBinding().setServiceUrl(EMPTY_PROFILES_SERVICE_URL);
        ProvidedInterface providedInterface = new ProvidedInterface()
              .setInterfaceName("Validation Service API")
              .setInterfaceVersion("2.0.0")
              .setBindings(List.of(restBinding));

        return (DeployedService) new DeployedService()
              .setStatus(DeployedService.Status.AVAILABLE)
              .setName("mock-empty-profiles-service")
              .setProvidedInterfaces(List.of(providedInterface));
    }
}
