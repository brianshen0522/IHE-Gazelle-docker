/*
 * Copyright 2025-2026 IHE International.
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

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import net.ihe.gazelle.itb.gateway.technical.client.ItbHttpClient;
import net.ihe.gazelle.maestro.itb.step.business.ItbHandler;
import net.ihe.gazelle.maestro.spi.technical.HandlerContext;
import net.ihe.gazelle.maestro.spi.technical.HandlerProvider;

/**
 * CDI Factory to produce ItbHttpClient instances for the Callback controller bean.
 * <p>
 * <i>This factory is wrapping a call to the HandlerProvider to maintain only one way of instantiating the client ITB
 * handler.</i>
 */
public class ItbClientFactory {

   private final HandlerProvider handlerProvider;

   /**
    * Constructs an instance of ItbClientFactory with the specified HandlerProvider.
    *
    * @param handlerProvider the provider responsible for retrieving handlers for ITB services
    */
   @Inject
   public ItbClientFactory(HandlerProvider handlerProvider) {
      this.handlerProvider = handlerProvider;
   }

   /**
    * Produces and provides a request-scoped instance of {@link ItbHttpClient}.
    * This method utilizes the {@link HandlerProvider} to retrieve a handler for the ITB service
    * based on the specified {@link HandlerContext}.
    *
    * @return an instance of {@link ItbHttpClient} generated through the {@link HandlerProvider}
    */
   @Produces
   @RequestScoped
   public ItbHttpClient createItbClient() {
      return (ItbHttpClient) handlerProvider.getHandler(new HandlerContext(ItbHandler.ITB_SERVICE_NAME), ItbHandler.class);
   }

}
