package net.ihe.gazelle.maestro.validation.step.technical.factory;

import net.ihe.gazelle.maestro.spi.business.Handler;
import net.ihe.gazelle.maestro.spi.technical.HandlerContext;
import net.ihe.gazelle.maestro.validation.step.technical.handler.ValidationServiceHandler;
import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterface;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.validation.v2.api.business.ValidationService;
import net.ihe.gazelle.validation.v2.client.ValidationServiceClientFactory;
import net.ihe.gazelle.validation.v2.client.ValidationServiceFactoryProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ValidationServiceHandlerFactoryTest {

   private static final String TEST_INTERFACE = "Validation Service API";
   private static final String EVS_INTERFACE = "EVS Client API";

   private final HandlerContext handlerContext = new HandlerContext("GV");

   @Test
   void shouldExposeHandlerType() {
      ValidationServiceHandlerFactory factory = new ValidationServiceHandlerFactory(providerWith(TEST_INTERFACE));

      assertEquals(ValidationServiceHandler.class, factory.getHandlerType());
   }

   @Test
   void shouldDescribeConsumedInterfacesWithoutEvs() {
      ValidationServiceHandlerFactory factory = new ValidationServiceHandlerFactory(providerWith(TEST_INTERFACE, EVS_INTERFACE));

      List<ConsumedInterface> consumed = factory.getConsumedInterfaces();

      assertEquals(1, consumed.size());
      assertEquals(TEST_INTERFACE, consumed.getFirst().getInterfaceName());
   }

   @Test
   void shouldCreateHandlerUsingMatchingFactory() {
      ValidationServiceHandlerFactory factory = new ValidationServiceHandlerFactory(providerWith(TEST_INTERFACE));
      ProvidedInterface providedInterface = httpProvidedInterface(TEST_INTERFACE, "http://localhost:8080/validation", "2.0.0");

      Handler handler = factory.createHandler(handlerContext, providedInterface, null);

      assertInstanceOf(ValidationServiceHandler.class, handler);
   }

   @Test
   void shouldFailWhenNoBindingsAvailable() {
      ValidationServiceHandlerFactory factory = new ValidationServiceHandlerFactory(providerWith(TEST_INTERFACE));
      ProvidedInterface providedInterface = new ProvidedInterface();
      providedInterface.setInterfaceName(TEST_INTERFACE);

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> factory.createHandler(handlerContext, providedInterface, null));
      assertEquals("Service " + TEST_INTERFACE + " has no bindings", exception.getMessage());
   }

   @Test
   void shouldFailWhenNoFactoryMatchesInterface() {
      ValidationServiceHandlerFactory factory = new ValidationServiceHandlerFactory(providerWith(TEST_INTERFACE));
      ProvidedInterface providedInterface = httpProvidedInterface("Unknown Service", "http://localhost:8080/validation", "2.0.0");

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> factory.createHandler(handlerContext, providedInterface, null));
      assertEquals("No validation service factory found for interface Unknown Service", exception.getMessage());
   }



   private ValidationServiceFactoryProvider providerWith(String... interfaceNames) {
      List<ValidationServiceClientFactory> factories = Stream.of(interfaceNames)
            .map(name -> (ValidationServiceClientFactory) new StubValidationServiceInterfaceFactory(name, true))
            .toList();
      return () -> factories;
   }

   private ProvidedInterface httpProvidedInterface(String name, String url, String version) {
      HttpRestBinding binding = new HttpRestBinding();
      binding.setServiceUrl(url);
      ProvidedInterface providedInterface = new ProvidedInterface();
      providedInterface.setInterfaceName(name);
      providedInterface.setInterfaceVersion(version);
      providedInterface.addBinding(binding);
      return providedInterface;
   }

      private record StubValidationServiceInterfaceFactory(
              String interfaceName,
              boolean supports)
              implements ValidationServiceClientFactory {

         @Override
         public String getInterfaceName() {
            return interfaceName;
         }

         @Override
         public boolean supports(ProvidedInterface providedInterface) {
            return supports;
         }

         @Override
         public ValidationService create(ProvidedInterface providedInterface) {
            return mock(ValidationService.class);
         }
      }
}
