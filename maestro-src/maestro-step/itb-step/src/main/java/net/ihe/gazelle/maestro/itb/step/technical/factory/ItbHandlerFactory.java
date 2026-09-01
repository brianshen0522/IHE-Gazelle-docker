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

import net.ihe.gazelle.maestro.itb.step.business.ItbHandler;
import net.ihe.gazelle.maestro.itb.step.technical.handler.ItbHandlerImpl;
import net.ihe.gazelle.maestro.spi.business.Handler;
import net.ihe.gazelle.maestro.spi.technical.ConfigProvider;
import net.ihe.gazelle.maestro.spi.technical.HandlerContext;
import net.ihe.gazelle.maestro.spi.technical.HandlerFactory;
import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterface;
import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterfaceBuilder;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;

import java.util.List;

/**
 * Factory class for creating and configuring handlers for the ITB (Integration Test Bed) service.
 */
public class ItbHandlerFactory implements HandlerFactory {

   /**
    * Default constructor.
    */
   public ItbHandlerFactory() {
      // Empty
   }

    @Override
    public Class<? extends Handler> getHandlerType() {
        return ItbHandler.class;
    }

    @Override
    public Handler createHandler(HandlerContext handlerContext, ProvidedInterface providedInterface, ConfigProvider configProvider) throws IllegalArgumentException {
       String baseItbUrl = ((HttpRestBinding) providedInterface.getBindings().getFirst()).getServiceUrl();
       String itbApiKey = configProvider.getConfig("itb.api.key");
       return new ItbHandlerImpl(baseItbUrl, itbApiKey);
    }

    @Override
    public List<ConsumedInterface> getConsumedInterfaces() {
        return List.of(new ConsumedInterfaceBuilder()
                .setInterfaceName("ITB API")
                .setRequired(true)
                .setSupportedBindings(List.of(HttpRestBinding.TYPE))
                .setSupportedVersions(List.of("1.0.0"))
                .build());
    }

}

