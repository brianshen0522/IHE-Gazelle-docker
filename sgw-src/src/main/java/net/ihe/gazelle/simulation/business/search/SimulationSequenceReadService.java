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

package net.ihe.gazelle.simulation.business.search;

import net.ihe.gazelle.search.api.ReadException;
import net.ihe.gazelle.search.api.ReadService;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.simulation.business.SimulationSequenceLookupDAO;
import net.ihe.gazelle.simulation.business.model.ResolvedSimulationSequence;
import net.ihe.gazelle.simulation.business.model.SimulationSequenceExtended;
import net.ihe.gazelle.simulation.business.svs.SimulationSVSService;

/**
 * Implementation of the {@link ReadService} interface for {@link ResolvedSimulationSequence}.
 */
public class SimulationSequenceReadService implements ReadService<String, ResolvedSimulationSequence> {

    private final SimulationSequenceLookupDAO simulationSequenceLookupDAO;
    private final SimulationSVSService svsService;

   /**
    * Constructs a SimulationSequenceReadService with the required dependencies.
    *
    * @param simulationSequenceLookupDAO the DAO to access simulation sequence data
    * @param svsService the service responsible for resolving value sets in simulation sequences
    */
    public SimulationSequenceReadService(SimulationSequenceLookupDAO simulationSequenceLookupDAO, SimulationSVSService svsService) {
        this.simulationSequenceLookupDAO = simulationSequenceLookupDAO;
        this.svsService = svsService;
    }

    @Override
    public ResolvedSimulationSequence readObject(String id, GazelleIdentity identity) {
        if (id == null) {
            throw new ReadException("Field cannot be null");
        }
        SimulationSequenceExtended sequence = simulationSequenceLookupDAO.getSimulationSequenceById(id);
        return svsService.resolveValueSets(sequence);
    }

    @Override
    public ResolvedSimulationSequence readObject(String id, String presentationUrl, GazelleIdentity identity) {
        return readObject(id, identity);
    }
}
