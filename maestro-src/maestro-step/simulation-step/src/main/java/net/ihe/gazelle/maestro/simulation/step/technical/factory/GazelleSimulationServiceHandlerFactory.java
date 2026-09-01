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

package net.ihe.gazelle.maestro.simulation.step.technical.factory;

import net.ihe.gazelle.maestro.simulation.step.technical.handler.GazelleSimulationServiceHandler;
import net.ihe.gazelle.maestro.spi.business.Handler;
import net.ihe.gazelle.maestro.spi.technical.ConfigProvider;
import net.ihe.gazelle.maestro.spi.technical.HandlerContext;
import net.ihe.gazelle.maestro.spi.technical.HandlerFactory;
import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterface;
import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterfaceBuilder;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.simulation.callback.jaxrs.server.technical.SimulationCallbackFactory;
import net.ihe.gazelle.simulation.client.business.SimulationClientImpl;
import net.ihe.gazelle.simulation.client.technical.SimulationServiceClientImpl;
import net.ihe.gazelle.simulation.jaxrs.api.technical.ws.SimulationAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Factory for creating GazelleSimulationServiceHandler instances.
 * This factory creates handlers that interface with Gazelle simulation services.
 */
public class GazelleSimulationServiceHandlerFactory implements HandlerFactory {

    /**
     * Default constructor.
     */
    public GazelleSimulationServiceHandlerFactory() { /* Default constructor */ }

    @Override
    public Class<? extends Handler> getHandlerType() {
        return GazelleSimulationServiceHandler.class;
    }

    @Override
    public Handler createHandler(HandlerContext handlerContext, ProvidedInterface providedInterface, ConfigProvider configProvider) throws IllegalArgumentException {
        try {
            HttpRestBinding binding = (HttpRestBinding) providedInterface.getBindings().getFirst();
            return new GazelleSimulationServiceHandler(
                    new SimulationClientImpl(
                            new SimulationServiceClientImpl(binding.getServiceUrl(), configProvider.getConfig("gzl.maestro.url")),
                            SimulationCallbackFactory.getSimulationSessionDAO()
                    )
            );
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Service " + providedInterface.getInterfaceName() + " has no bindings");
        }
    }

    @Override
    public List<ConsumedInterface> getConsumedInterfaces() {
        List <ConsumedInterface> consumedInterfaces = new ArrayList<>();
        consumedInterfaces.add(new ConsumedInterfaceBuilder()
                .setInterfaceName(SimulationAPI.INTERFACE_NAME)
                .setRequired(true)
                .addSupportedBinding(HttpRestBinding.TYPE)
                .addSupportedVersion("1.0.0")
                .build());
        return consumedInterfaces;
    }
}
