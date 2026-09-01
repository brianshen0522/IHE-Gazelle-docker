package net.ihe.gazelle.validation.gateway.technical.service;

import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.validation.gateway.business.ProfileSearchCriteria;
import net.ihe.gazelle.validation.gateway.business.ResolvedValidationService;
import net.ihe.gazelle.validation.gateway.technical.cache.ValidationProfileCache;
import net.ihe.gazelle.validation.gateway.technical.override.ValidationProfilesOverride;
import net.ihe.gazelle.validation.gateway.technical.service.support.CapturingHandler;
import net.ihe.gazelle.validation.gateway.technical.service.support.CapturingServiceLookup;
import net.ihe.gazelle.validation.v2.client.ValidationServiceFactoryProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class ServiceRegistryValidationServiceResolverTest {

   @Test
   void resolveBuildsServiceLookupQueryAndReturnsClientForResolvedUri() {
      CapturingServiceLookup serviceLookup = new CapturingServiceLookup()
            .withServices(List.of(deployedValidationService("svc-1", "http://validation.example")));

      AtomicReference<ProvidedInterface> requestedInterface = new AtomicReference<>();
      CapturingHandler handler = new CapturingHandler("Validation Service API", requestedInterface);
      ValidationServiceFactoryProvider factoryProvider = () ->
            List.of(handler, new CapturingHandler("ModelBasedValidationWSService"));
      ServiceRegistryValidationServiceResolver resolver = new ServiceRegistryValidationServiceResolver(
            serviceLookup,
            factoryProvider,
            new ValidationProfileCache(),
            ValidationProfilesOverride.none()
      );

      List<ResolvedValidationService> resolved = resolver.resolve(new ProfileSearchCriteria(), null);

      assertThat("resolved service returned", resolved, hasSize(1));
      assertThat(resolved.get(0).serviceName(), is("svc-1"));
      assertThat(resolved.get(0).validationService(), notNullValue());
      assertThat("service lookup query captured", serviceLookup.getLastQuery(), notNullValue());
      assertThat(serviceLookup.getLastQuery().range().getOffset(), is(0));
      assertThat(serviceLookup.getLastQuery().range().getLimit(), is(Integer.MAX_VALUE));
      assertThat(serviceLookup.getLastQuery().searchCriteria().getProvidedInterface().getFirstValue(),
            is("Validation Service API"));
      assertThat(serviceLookup.getLastQuery().searchCriteria().getStatus().getFirstValue(),
            is(DeployedService.Status.AVAILABLE));
      assertThat(requestedInterface.get().getInterfaceVersion(), is("1.0.0"));
   }

   @Test
   void resolvePopulatesServiceNameCriteriaFromValidationServiceParameter() {
      CapturingServiceLookup serviceLookup = new CapturingServiceLookup()
            .withServices(List.of(deployedValidationService("svc-1", "http://validation.example")));
      CapturingHandler handler = new CapturingHandler("Validation Service API");
      ValidationServiceFactoryProvider factoryProvider = () -> List.of(handler);
      ServiceRegistryValidationServiceResolver resolver = new ServiceRegistryValidationServiceResolver(
            serviceLookup,
            factoryProvider,
            new ValidationProfileCache(),
            ValidationProfilesOverride.none()
      );

      ProfileSearchCriteria criteria = new ProfileSearchCriteria().setValidationService("my-service");
      resolver.resolve(criteria, null);

      assertThat(serviceLookup.getLastQuery().searchCriteria().getName().getValues(), contains("my-service"));
   }

   @Test
   void resolveReturnsEmptyListWhenNoValidationServiceFound() {
      CapturingServiceLookup serviceLookup = new CapturingServiceLookup().withServices(List.of());
      CapturingHandler handler = new CapturingHandler("Validation Service API");
      ValidationServiceFactoryProvider factoryProvider = () -> List.of(handler);
      ServiceRegistryValidationServiceResolver resolver = new ServiceRegistryValidationServiceResolver(
            serviceLookup,
            factoryProvider,
            new ValidationProfileCache(),
            ValidationProfilesOverride.none()
      );

      List<ResolvedValidationService> resolved = resolver.resolve(new ProfileSearchCriteria(), null);

      assertThat(resolved, hasSize(0));
   }


   @Test
   void resolveSkipsNonMatchingInterfacesAndBlankUrls() {
      ProvidedInterface otherInterface = new ProvidedInterface()
            .setInterfaceName("Other API")
            .addBinding(new HttpRestBinding().setServiceUrl("http://other.example"));

      ProvidedInterface validationInterface = new ProvidedInterface()
            .setInterfaceName("Validation Service API")
            .setInterfaceVersion("1.0.0")
            .addBinding(new HttpRestBinding().setServiceUrl(" "))
            .addBinding(new HttpRestBinding().setServiceUrl("http://validation.example"));

      DeployedService deployedService = new DeployedService();
      deployedService.setName("svc-1");
      deployedService.setProvidedInterfaces(List.of(otherInterface, validationInterface));

      CapturingServiceLookup serviceLookup = new CapturingServiceLookup().withServices(List.of(deployedService));
      AtomicReference<ProvidedInterface> requestedInterface = new AtomicReference<>();
      CapturingHandler handler = new CapturingHandler("Validation Service API", requestedInterface);
      ValidationServiceFactoryProvider factoryProvider = () -> List.of(handler);
      ServiceRegistryValidationServiceResolver resolver = new ServiceRegistryValidationServiceResolver(
            serviceLookup,
            factoryProvider,
            new ValidationProfileCache(),
            ValidationProfilesOverride.none()
      );

      List<ResolvedValidationService> resolved = resolver.resolve(new ProfileSearchCriteria(), null);

      assertThat(resolved, hasSize(1));
      assertThat(requestedInterface.get().getInterfaceVersion(), is("1.0.0"));
   }

   @Test
   void resolveUsesFirstSupportedInterface() {
      DeployedService deployedService = new DeployedService();
      deployedService.setName("svc-1");
      ProvidedInterface v1Interface = new ProvidedInterface()
            .setInterfaceName("Validation Service API")
            .setInterfaceVersion("1.0.0")
            .addBinding(new HttpRestBinding().setServiceUrl("http://validation.example"));
      ProvidedInterface v2Interface = new ProvidedInterface()
            .setInterfaceName("Validation Service API")
            .setInterfaceVersion("2.0.0")
            .addBinding(new HttpRestBinding().setServiceUrl("http://validation-v2.example"));
      deployedService.setProvidedInterfaces(List.of(v1Interface, v2Interface));
      CapturingServiceLookup serviceLookup = new CapturingServiceLookup().withServices(List.of(deployedService));
      AtomicReference<ProvidedInterface> requestedInterface = new AtomicReference<>();
      CapturingHandler handler = new CapturingHandler("Validation Service API", requestedInterface);

      ValidationServiceFactoryProvider factoryProvider = () -> List.of(handler);
      ServiceRegistryValidationServiceResolver resolver = new ServiceRegistryValidationServiceResolver(
            serviceLookup,
            factoryProvider,
            new ValidationProfileCache(),
            ValidationProfilesOverride.none()
      );

      resolver.resolve(new ProfileSearchCriteria(), null);

      assertThat(requestedInterface.get().getInterfaceVersion(), is("1.0.0"));
   }

   @Test
   void resolveUsesModelBasedInterfaceWhenAvailable() {
      DeployedService deployedService = new DeployedService();
      deployedService.setName("mbv-1");
      deployedService.setProvidedInterfaces(List.of(new ProvidedInterface()
            .setInterfaceName("ModelBasedValidationWSService")
            .setInterfaceVersion("1.0.0")
            .addBinding(new HttpRestBinding().setServiceUrl("http://model.example"))));
      CapturingServiceLookup serviceLookup = new CapturingServiceLookup().withServices(List.of(deployedService));
      AtomicReference<ProvidedInterface> requestedInterface = new AtomicReference<>();
      CapturingHandler handler = new CapturingHandler("ModelBasedValidationWSService", requestedInterface);

      ValidationServiceFactoryProvider factoryProvider = () -> List.of(handler);
      ServiceRegistryValidationServiceResolver resolver = new ServiceRegistryValidationServiceResolver(
            serviceLookup,
            factoryProvider,
            new ValidationProfileCache(),
            ValidationProfilesOverride.none()
      );

      resolver.resolve(new ProfileSearchCriteria(), null);

      assertThat(requestedInterface.get().getInterfaceName(), is("ModelBasedValidationWSService"));
   }

   private static DeployedService deployedValidationService(String serviceName, String url) {
      DeployedService deployedService = new DeployedService();
      deployedService.setName(serviceName);
      deployedService.setProvidedInterfaces(List.of(new ProvidedInterface()
            .setInterfaceName("Validation Service API")
            .setInterfaceVersion("1.0.0")
            .addBinding(new HttpRestBinding().setServiceUrl(url))));
      return deployedService;
   }
}
