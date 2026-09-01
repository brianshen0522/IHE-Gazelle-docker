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

import net.ihe.gazelle.simulation.business.model.SimulationServiceInfo;

import java.util.List;

/**
 * This interface is the DAO layer to interact with service registry.
 */
public interface ServiceRegistryDAO {

    /**
     * This method is used to retrieve information about all declared simulation services in service registry.
     * @return The list of service information retrieved.
     */
    List<SimulationServiceInfo> getAvailableSimulationServices();
}
