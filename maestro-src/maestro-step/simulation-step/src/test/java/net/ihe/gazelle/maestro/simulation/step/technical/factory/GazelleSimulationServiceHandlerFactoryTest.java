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

import io.quarkus.test.junit.QuarkusTest;
import net.ihe.gazelle.maestro.simulation.step.technical.handler.GazelleSimulationServiceHandler;
import net.ihe.gazelle.maestro.simulation.step.technical.mock.ConfigProviderMock;
import net.ihe.gazelle.maestro.spi.business.Handler;
import net.ihe.gazelle.maestro.spi.technical.HandlerContext;
import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterface;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.simulation.jaxrs.api.technical.ws.SimulationAPI;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class GazelleSimulationServiceHandlerFactoryTest {

    private final GazelleSimulationServiceHandlerFactory factory = new GazelleSimulationServiceHandlerFactory();

    @Test
    void should_be_type_simulation_handler() {
        assertEquals(GazelleSimulationServiceHandler.class, factory.getHandlerType());
    }

    @Test
    void should_get_consumed_interface() {
        List<ConsumedInterface> consumedInterfaces = factory.getConsumedInterfaces();
        assertNotNull(consumedInterfaces);
        ConsumedInterface consumedInterface = consumedInterfaces.getFirst();
        assertNotNull(consumedInterface);
        assertEquals(SimulationAPI.INTERFACE_NAME, consumedInterface.getInterfaceName());
    }

    @Test
    void should_create_simulation_handler() {
        HttpRestBinding binding = new HttpRestBinding();
        binding.setServiceUrl("http://localhost:80");
        ProvidedInterface providedInterface = new ProvidedInterface();
        providedInterface.addBinding(binding);
        providedInterface.setInterfaceVersion("1.0.0");
        providedInterface.setInterfaceName("test");
        Handler handler = factory.createHandler(new HandlerContext("simulator"), providedInterface, new ConfigProviderMock());
        assertNotNull(handler);
        assertInstanceOf(GazelleSimulationServiceHandler.class, handler);
    }

    @Test
    void no_binding_simulation_handler_throws_exception() {
        ProvidedInterface providedInterface = new ProvidedInterface();
        providedInterface.setInterfaceVersion("1.0.0");
        providedInterface.setInterfaceName("test");
        ConfigProviderMock configProvider = new ConfigProviderMock();
        HandlerContext handlerContext = new HandlerContext("simulator");
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.createHandler(handlerContext, providedInterface, configProvider)
        );
        assertEquals("Service test has no bindings", exception.getMessage());
    }
}
