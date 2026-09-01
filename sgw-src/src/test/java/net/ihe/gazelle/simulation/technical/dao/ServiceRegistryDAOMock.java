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

package net.ihe.gazelle.simulation.technical.dao;

import net.ihe.gazelle.simulation.business.ServiceRegistryDAO;
import net.ihe.gazelle.simulation.business.model.SimulationServiceInfo;

import java.util.List;

public class ServiceRegistryDAOMock implements ServiceRegistryDAO {

    private final boolean isValid;

    public ServiceRegistryDAOMock(boolean isValid) {
        this.isValid = isValid;
    }

    @Override
    public List<SimulationServiceInfo> getAvailableSimulationServices() {
        if (isValid) {
            return List.of(
                    new SimulationServiceInfo("XDS_Simulator", "1.0.0", "XDS_Simulator.json"),
                    new SimulationServiceInfo("MHD_Simulator", "1.0.0","MHD_Simulator.json")
            );
        } else {
            return List.of(
                    new SimulationServiceInfo("Wrong_simulator", "1.0.0","wrong.json")
            );
        }
    }
}
