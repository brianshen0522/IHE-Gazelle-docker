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

package net.ihe.gazelle.simulation.business;

import net.ihe.gazelle.simulation.business.sequence.SimulationSequence;

import java.util.List;

/**
 * This interface is the DAO layer to interact with simulation services.
 */
public interface SimulationSequenceDAO {

    /**
     * This method is the client side checksum retrieving to help determine if caches need to be reset
     * @param serviceUrl The service URL to query th checksum
     * @return The String value of the checksum formatter like Ox1234ABED
     */
    String getServiceChecksum(String serviceUrl);

    /**
     * This method is used to retrieve simulation sequences in a given simulation service.
     * @param serviceUrl The URL of the simulation service.
     * @return The list of supported simulation sequences by this service.
     */
    List<SimulationSequence> getSimulationSequences(String serviceUrl);
}
