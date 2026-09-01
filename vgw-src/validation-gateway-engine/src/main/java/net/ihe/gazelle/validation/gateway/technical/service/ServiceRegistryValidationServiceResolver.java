package net.ihe.gazelle.validation.gateway.technical.service;

import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.SearchService;
import net.ihe.gazelle.search.api.SearchParameter;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;
import net.ihe.gazelle.validation.gateway.business.ProfileSearchCriteria;
import net.ihe.gazelle.validation.gateway.business.ResolvedValidationService;
import net.ihe.gazelle.validation.gateway.business.ValidationServiceResolver;
import net.ihe.gazelle.validation.gateway.technical.cache.CachingValidationService;
import net.ihe.gazelle.validation.gateway.technical.cache.ValidationProfileCache;
import net.ihe.gazelle.validation.gateway.technical.override.OverridableValidationService;
import net.ihe.gazelle.validation.gateway.technical.override.ValidationProfilesOverride;
import net.ihe.gazelle.validation.v2.api.business.ValidationService;
import net.ihe.gazelle.validation.v2.client.ValidationServiceFactoryProvider;
import net.ihe.gazelle.validation.v2.client.ValidationServiceClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ServiceRegistryValidationServiceResolver implements ValidationServiceResolver {

   private static final Logger log = LoggerFactory.getLogger(ServiceRegistryValidationServiceResolver.class);
   private static final int DEFAULT_OFFSET = 0;

   private final SearchService<DeployedService, ServiceSearchCriteria> serviceLookup;
   private final List<ValidationServiceClientFactory> factories;
   private final ValidationProfileCache profileCache;
   private final ValidationProfilesOverride profilesOverride;

   public ServiceRegistryValidationServiceResolver(
         SearchService<DeployedService, ServiceSearchCriteria> serviceLookup,
         ValidationServiceFactoryProvider factoryProvider,
         ValidationProfileCache profileCache,
         ValidationProfilesOverride profilesOverride) {
      this.serviceLookup = Objects.requireNonNull(serviceLookup, "serviceLookup must not be null");
      this.factories = List.copyOf(Objects.requireNonNull(factoryProvider, "factoryProvider must not be null")
            .getFactories());
      this.profileCache = Objects.requireNonNull(profileCache, "profileCache must not be null");
      this.profilesOverride = Objects.requireNonNull(profilesOverride, "profilesOverride must not be null");
   }

   @Override
   public List<ResolvedValidationService> resolve(ProfileSearchCriteria criteria, GazelleIdentity identity) {
      ProfileSearchCriteria effectiveCriteria = criteria != null ? criteria : new ProfileSearchCriteria();
      List<DeployedService> services = resolveValidationServices(effectiveCriteria, identity);
      if (services.isEmpty()) {
         return List.of();
      }
      List<ResolvedValidationService> resolved = new ArrayList<>();
      for (DeployedService service : services) {
         Optional<ResolvedInterface> resolvedInterface = extractValidationServiceInterface(service);
         if (resolvedInterface.isEmpty()) {
            continue;
         }
         try {
            ResolvedInterface iface = resolvedInterface.get();
            ValidationService validationService = iface.factory().create(iface.providedInterface());
            ValidationService cached = getCachedService(service, validationService);
            resolved.add(new ResolvedValidationService(service.getName(), cached));
         } catch (RuntimeException e) {
            log.error("Failed to create ValidationService for deployed service '{}'", service.getName(), e);
         }
      }
      return resolved;
   }

   private ValidationService getCachedService(DeployedService service, ValidationService validationService) {
      ValidationService adapted = FetchCapableValidationServiceAdapter.supports(validationService)
            ? new FetchCapableValidationServiceAdapter(validationService)
            : validationService;
      ValidationService overridable = new OverridableValidationService(
            service.getName(),
            adapted,
            profilesOverride);
      return new CachingValidationService(service.getName(), overridable, profileCache);
   }

   private List<DeployedService> resolveValidationServices(ProfileSearchCriteria criteria, GazelleIdentity identity) {
      List<String> interfaceNames = factories.stream()
            .map(ValidationServiceClientFactory::getInterfaceName)
            .distinct()
            .toList();
      ServiceSearchCriteria serviceCriteria = new ServiceSearchCriteria()
            .setProvidedInterface(interfaceNames.toArray(new String[0]))
            .setStatus(DeployedService.Status.AVAILABLE, DeployedService.Status.UNKNOWN);
      SearchParameter serviceParam = criteria.getValidationService();
      if (serviceParam != null && serviceParam.getValues() != null && !serviceParam.getValues().isEmpty()) {
         List<String> names = serviceParam.getValues().stream().map(String::valueOf).toList();
         serviceCriteria.setName(names.toArray(new String[0]));
      }
      SearchResult<DeployedService> services = serviceLookup.search(
            new SearchQuery<>(serviceCriteria, new Range(DEFAULT_OFFSET, Integer.MAX_VALUE), Collections.emptyList()),
            identity);
      return services.objects();
   }

   private Optional<ResolvedInterface> extractValidationServiceInterface(DeployedService deployedService) {
      if (deployedService == null || deployedService.getProvidedInterfaces() == null) {
         return Optional.empty();
      }
      List<ProvidedInterface> candidates = deployedService.getProvidedInterfaces().stream()
            .filter(this::isSupportedInterface)
            .toList();
      if (candidates.isEmpty()) {
         return Optional.empty();
      }
      return candidates.stream()
            .map(this::resolveInterfaceCandidate)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
   }

   private Optional<ResolvedInterface> resolveInterfaceCandidate(ProvidedInterface providedInterface) {
      Optional<ValidationServiceClientFactory> handler = factories.stream()
            .filter(factory -> factory.supports(providedInterface))
            .findFirst();
       return handler.map(validationServiceInterfaceFactory -> new ResolvedInterface(validationServiceInterfaceFactory, providedInterface));
   }

   private boolean isSupportedInterface(ProvidedInterface providedInterface) {
      return factories.stream().anyMatch(factory -> factory.supports(providedInterface));
   }

   private record ResolvedInterface(ValidationServiceClientFactory factory,
                                    ProvidedInterface providedInterface) {
   }

}
