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

package net.ihe.gazelle.maestro.simulation.step.technical.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ihe.gazelle.simulation.business.callback.SimulationCallbackService;
import net.ihe.gazelle.simulation.callback.jaxrs.server.technical.SimulationCallbackController;


/**
 * Implementation of SimulationCallbackController for handling simulation callbacks.
 * This controller receives and processes simulation callbacks from the simulation service.
 */
@Path("/")
public class SimulationCallbackControllerImpl extends SimulationCallbackController {

    /**
     * Constructor for SimulationCallbackControllerImpl.
     * @param simulationCallback the simulation callback service to handle callbacks
     */
    @Inject
    public SimulationCallbackControllerImpl(SimulationCallbackService simulationCallback) {
        super(simulationCallback);
    }
}
