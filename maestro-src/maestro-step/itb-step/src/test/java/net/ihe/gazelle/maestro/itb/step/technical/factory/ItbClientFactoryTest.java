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

package net.ihe.gazelle.maestro.itb.step.technical.factory;

import net.ihe.gazelle.itb.gateway.technical.client.ItbHttpClient;
import net.ihe.gazelle.maestro.itb.step.business.ItbHandler;
import net.ihe.gazelle.maestro.itb.step.technical.handler.ItbHandlerImpl;
import net.ihe.gazelle.maestro.spi.technical.HandlerContext;
import net.ihe.gazelle.maestro.spi.technical.HandlerProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ItbClientFactoryTest {

   @Test
   void createItbClientDelegatesToHandlerProvider() {
      HandlerProvider handlerProvider = mock(HandlerProvider.class);
      ItbHandlerImpl expectedClient = mock(ItbHandlerImpl.class);
      when(handlerProvider.getHandler(any(HandlerContext.class), eq(ItbHandler.class))).thenReturn(expectedClient);
      ItbClientFactory factory = new ItbClientFactory(handlerProvider);

      ItbHttpClient actualClient = factory.createItbClient();

      ArgumentCaptor<HandlerContext> contextCaptor = ArgumentCaptor.forClass(HandlerContext.class);
      verify(handlerProvider).getHandler(contextCaptor.capture(), eq(ItbHandler.class));
      assertEquals(ItbHandler.ITB_SERVICE_NAME, contextCaptor.getValue().serviceName());
      assertSame(expectedClient, actualClient);
   }
}
