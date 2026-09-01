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

package net.ihe.gazelle.maestro.simulation.step.technical.handler;

import net.ihe.gazelle.maestro.simulation.step.business.SimulationHandler;
import net.ihe.gazelle.simulation.business.callback.SimulationReport;
import net.ihe.gazelle.simulation.business.sequence.SimulationChecksumService;
import net.ihe.gazelle.simulation.business.sequence.SimulationSequence;
import net.ihe.gazelle.simulation.business.setup.AdditionalInstructions;
import net.ihe.gazelle.simulation.business.setup.SimulationRequest;
import net.ihe.gazelle.simulation.client.business.SimulationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Handler for Gazelle simulation service.
 * This handler interfaces with the Gazelle simulation service to execute simulation sequences.
 */
public class GazelleSimulationServiceHandler implements SimulationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GazelleSimulationServiceHandler.class);

    private final SimulationClient simulationClient;

    /**
     * Constructor for GazelleSimulationServiceHandler.
     * @param simulationClient the simulation client to use for simulation operations
     */
    public GazelleSimulationServiceHandler(SimulationClient simulationClient) {
        this.simulationClient = simulationClient;
    }

    @Override
    public boolean isAvailable() {
        try {
            String checksum = simulationClient.getChecksum(List.of());
            return !SimulationChecksumService.DEFAULT_CHECKSUM.equals(checksum);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return false;
        }
    }

    @Override
    public String getChecksum(List<SimulationSequence> sequences) {
        throw new UnsupportedOperationException("Not supported by maestro.");
    }

    @Override
    public List<SimulationSequence> getSimulationSequences() {
        return simulationClient.getSimulationSequences();
    }

    @Override
    public void simulate(SimulationRequest simulationRequest, Consumer<AdditionalInstructions> instructionHandler, Consumer<SimulationReport> reportConsumer) {
        simulationClient.simulate(simulationRequest, instructionHandler, reportConsumer);
    }
}
