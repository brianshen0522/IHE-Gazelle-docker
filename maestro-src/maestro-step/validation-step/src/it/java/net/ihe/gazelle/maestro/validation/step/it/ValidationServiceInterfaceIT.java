/*
 * Copyright 2025 Kereval.
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

package net.ihe.gazelle.maestro.validation.step.it;

import net.ihe.gazelle.certificate.client.CertificateValidationServiceClientFactory;
import net.ihe.gazelle.itb.client.ItbValidationServiceClientFactory;
import net.ihe.gazelle.maestro.api.business.MaestroObserver;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.engine.business.context.ReadSessionStore;
import net.ihe.gazelle.maestro.spi.business.Handler;
import net.ihe.gazelle.maestro.spi.technical.ConfigProvider;
import net.ihe.gazelle.maestro.spi.technical.HandlerContext;
import net.ihe.gazelle.maestro.spi.technical.HandlerProvider;
import net.ihe.gazelle.maestro.validation.step.business.ValidationHandler;
import net.ihe.gazelle.maestro.validation.step.business.ValidationStepDefinition;
import net.ihe.gazelle.maestro.validation.step.technical.factory.ValidationStepExecutorFactory;
import net.ihe.gazelle.maestro.validation.step.technical.handler.ValidationServiceHandler;
import net.ihe.gazelle.modelbased.client.ModelBasedValidationServiceClientFactory;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import technical.provider.HandlerProviderSPI;
import technical.provider.ServiceRegistry;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ValidationServiceInterfaceIT {

   private static final String MODEL_BASED_INTERFACE = ModelBasedValidationServiceClientFactory.INTERFACE_NAME;
   private static final String CERTIFICATE_INTERFACE = CertificateValidationServiceClientFactory.INTERFACE_NAME;
   private static final String GITB_INTERFACE = ItbValidationServiceClientFactory.INTERFACE_NAME;

   @ParameterizedTest
   @MethodSource("serviceDefinitions")
   void shouldResolveHandlerFromValidationServiceProperty(String serviceName, String interfaceName) {
      ServiceRegistry registry = new InMemoryServiceRegistry(Map.of(
            serviceName, createService(serviceName, interfaceName)
      ));
      ConfigProvider configProvider = key -> null;
      ReadSessionStore<MaestroObserver> observerStore = id -> null;
      HandlerProvider handlerProvider = new HandlerProviderSPI(registry, configProvider, observerStore);

      Step step = new Step()
            .setName("validation")
            .setType(ValidationStepDefinition.TYPE)
            .setProperties(List.of(
                  new StringProperty(ValidationStepDefinition.VALIDATION_SERVICE, serviceName),
                  new StringProperty(ValidationStepDefinition.VALIDATION_PROFILE, "PROFILE"),
                  new ByteArrayProperty(ValidationStepDefinition.CONTENT_TO_VALIDATE,
                        "payload".getBytes(StandardCharsets.UTF_8))
            ));

      ValidationStepExecutorFactory executorFactory = new ValidationStepExecutorFactory();
      Map<String, Class<? extends Handler>> requiredHandlers = executorFactory.getRequiredServices(step);
      assertEquals(ValidationHandler.class, requiredHandlers.get(serviceName));

      Handler handler = handlerProvider.getHandler(new HandlerContext(serviceName), ValidationHandler.class);
      assertNotNull(handler);
      assertInstanceOf(ValidationServiceHandler.class, handler);
   }

   private static Stream<Arguments> serviceDefinitions() {
      return Stream.of(
            Arguments.of(MODEL_BASED_INTERFACE, MODEL_BASED_INTERFACE),
            Arguments.of(CERTIFICATE_INTERFACE, CERTIFICATE_INTERFACE),
            Arguments.of(GITB_INTERFACE, GITB_INTERFACE)
      );
   }

   private static Service createService(String serviceName, String interfaceName) {
      HttpRestBinding binding = new HttpRestBinding();
      binding.setServiceUrl("http://localhost:8080/validation");
      ProvidedInterface providedInterface = new ProvidedInterface();
      providedInterface.setInterfaceName(interfaceName);
      providedInterface.setInterfaceVersion("1.0.0");
      providedInterface.addBinding(binding);
      return new Service()
            .setName(serviceName)
            .setVersion("1.0.0")
            .setInstanceId(serviceName + "-instance")
            .setReplicaId(serviceName + "-replica")
            .setProvidedInterfaces(List.of(providedInterface));
   }

   private record InMemoryServiceRegistry(Map<String, Service> services) implements ServiceRegistry {

      @Override
         public List<Service> getServices() {
            return List.copyOf(services.values());
         }

         @Override
         public Service getService(String serviceName) {
            Service service = services.get(serviceName);
            if (service == null) {
               throw new IllegalArgumentException("Service not found: " + serviceName);
            }
            return service;
         }
      }
}
