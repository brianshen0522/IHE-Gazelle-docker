package net.ihe.gazelle.validation.gateway.quarkus.factory;

import net.ihe.gazelle.search.api.ReadService;
import net.ihe.gazelle.search.api.SearchService;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.validation.gateway.business.ProfileReadId;
import net.ihe.gazelle.validation.gateway.business.ReadProfileService;
import net.ihe.gazelle.validation.gateway.business.SearchProfileService;
import net.ihe.gazelle.validation.gateway.business.ValidationServiceResolver;
import net.ihe.gazelle.validation.gateway.technical.cache.ValidationCacheConfiguration;
import net.ihe.gazelle.validation.gateway.technical.cache.ValidationProfileCache;
import net.ihe.gazelle.validation.gateway.technical.override.ValidationProfilesOverride;
import net.ihe.gazelle.validation.gateway.technical.service.ServiceRegistryValidationServiceResolver;
import net.ihe.gazelle.validation.v2.client.SPIValidationServiceFactoryProvider;
import net.ihe.gazelle.validation.v2.client.ValidationServiceFactoryProvider;
import net.ihe.gazelle.validation.gateway.technical.ProfilePresenterService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ApplicationScoped
public class ValidationGatewayCDIFactory {

    private final ValidationCacheConfiguration cacheConfiguration;

    @Inject
    public ValidationGatewayCDIFactory(ValidationCacheConfiguration cacheConfiguration) {
        this.cacheConfiguration = cacheConfiguration;
    }

    @Produces
    @ApplicationScoped
   public ValidationServiceResolver validationServiceResolver(
          @ServiceRegistryLookup
          SearchService<DeployedService, ServiceSearchCriteria> serviceLookup,
          ValidationProfilesOverride profilesOverride) {
        return new ServiceRegistryValidationServiceResolver(serviceLookup, validationServiceFactoryProvider(),
              validationProfileCache(),
              profilesOverride);
    }

    @Produces
    @ApplicationScoped
    public SearchProfileService searchProfileService(ValidationServiceResolver validationServiceResolver, Authz authz) {
        return new SearchProfileService(validationServiceResolver, profilePresenterService(), authz);
    }

    @Produces
    @ApplicationScoped
    public ReadService<ProfileReadId, ValidationProfile> readProfileService(
            ValidationServiceResolver validationServiceResolver,
            Authz authz) {
        return new ReadProfileService(validationServiceResolver, authz);
    }

    @Produces
    @ApplicationScoped
    public ProfilePresenterService profilePresenterService() {
        return new ProfilePresenterService();
    }

    @Produces
    @ApplicationScoped
    public ValidationProfileCache validationProfileCache() {
        return new ValidationProfileCache(cacheConfiguration);
    }

    @Produces
    @ApplicationScoped
    public ValidationServiceFactoryProvider validationServiceFactoryProvider() {
        return new SPIValidationServiceFactoryProvider();
    }
}
