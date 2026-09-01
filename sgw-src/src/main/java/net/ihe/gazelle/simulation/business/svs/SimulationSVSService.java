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

package net.ihe.gazelle.simulation.business.svs;

import net.ihe.gazelle.simulation.business.model.ResolvedSimulationSequence;
import net.ihe.gazelle.simulation.business.model.SimulationSequenceExtended;

/**
 * Service interface to resolve value sets in supported parameters.
 */
public interface SimulationSVSService {

    /**
     * This method is used to retrieve the list of values referenced by the valueSetId in supported parameters
     * @param sequence The sequence where we want to resolve the value Sets
     * @return A new sequence where valueSet ids are replaced by a list of options
     */
    ResolvedSimulationSequence resolveValueSets(SimulationSequenceExtended sequence);
}
