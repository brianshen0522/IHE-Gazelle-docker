/*
 * Copyright 2026 IHE International.
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

package net.ihe.gazelle.maestro.validation.step.technical.factory;

import net.ihe.gazelle.maestro.spi.business.Handler;
import net.ihe.gazelle.maestro.spi.technical.HandlerContext;
import net.ihe.gazelle.maestro.validation.step.technical.handler.EvsValidationHandler;
import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterface;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.servicemetadata.api.business.WebBinding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EvsValidationServiceHandlerFactoryTest {

   private final HandlerContext handlerContext = new HandlerContext("EVS");
   private EvsValidationServiceHandlerFactory factory;

   @BeforeEach
   void setUp() {
      factory = new EvsValidationServiceHandlerFactory();
   }

   @Test
   void shouldExposeEvsValidationHandlerType() {
      assertEquals(EvsValidationHandler.class, factory.getHandlerType());
   }

   @Test
   void shouldDescribeConsumedInterface() {
      List<ConsumedInterface> consumed = factory.getConsumedInterfaces();
      assertEquals(1, consumed.size());
      ConsumedInterface evsInterface = consumed.getFirst();
      assertEquals("EVS Client API", evsInterface.getInterfaceName());
      assertEquals(List.of(HttpRestBinding.TYPE), evsInterface.getSupportedBindings());
   }

   @Test
   void shouldCreateHandlerWhenHttpBindingIsPresent() {
      ProvidedInterface providedInterface = httpProvidedInterface("http://localhost:8080/rest");

      Handler handler = factory.createHandler(handlerContext, providedInterface, null);

      assertInstanceOf(EvsValidationHandler.class, handler);
   }

   @Test
   void shouldFailWhenProvidedInterfaceIsNull() {
      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> factory.createHandler(handlerContext, null, null));
      assertEquals("ProvidedInterface cannot be null", exception.getMessage());
   }

   @Test
   void shouldFailWhenBindingsAreEmpty() {
      ProvidedInterface providedInterface = new ProvidedInterface();

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> factory.createHandler(handlerContext, providedInterface, null));
      assertEquals("Provided interface has no bindings configured", exception.getMessage());
   }

   @Test
   void shouldFailWhenServiceUrlIsBlank() {
      ProvidedInterface providedInterface = httpProvidedInterface("   ");

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> factory.createHandler(handlerContext, providedInterface, null));
      assertEquals("Provided interface has no valid service URL", exception.getMessage());
   }

   @Test
   void shouldFailWhenFirstBindingIsNotHttpRest() {
      ProvidedInterface providedInterface = new ProvidedInterface();
      providedInterface.addBinding(new WebBinding());

      IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> factory.createHandler(handlerContext, providedInterface, null));
      assertEquals("First binding is not an HttpRestBinding", exception.getMessage());
   }

   private ProvidedInterface httpProvidedInterface(String serviceUrl) {
      HttpRestBinding binding = new HttpRestBinding();
      binding.setServiceUrl(serviceUrl);
      ProvidedInterface providedInterface = new ProvidedInterface();
      providedInterface.setInterfaceName("EVS");
      providedInterface.setInterfaceVersion("1.0.0");
      providedInterface.addBinding(binding);
      return providedInterface;
   }
}
