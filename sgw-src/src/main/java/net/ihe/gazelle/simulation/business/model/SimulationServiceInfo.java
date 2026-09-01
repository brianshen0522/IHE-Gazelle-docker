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

package net.ihe.gazelle.simulation.business.model;

import java.util.Objects;

/**
 * Holds metadata about a simulation service, including its name, version, and URL.
 * <p>
 * This class is typically used as a lightweight DTO to expose simulator
 * information in service responses, configuration, or registry contexts.
 */
public class SimulationServiceInfo {

    private String simulatorName;
    private String simulatorVersion;
    private String simulatorUrl;

    /**
     * Creates an empty {@code SimulationServiceInfo} instance.
     * <p>
     * Intended primarily for frameworks and DTO deserialization.
     */
    public SimulationServiceInfo() {
    }

    /**
     * Creates a {@code SimulationServiceInfo} with the given details.
     *
     * @param simulatorName    the name of the simulator
     * @param simulatorVersion the version of the simulator
     * @param simulatorUrl     the URL of the simulator service
     */
    public SimulationServiceInfo(String simulatorName, String simulatorVersion, String simulatorUrl) {
        this.simulatorName = simulatorName;
        this.simulatorVersion = simulatorVersion;
        this.simulatorUrl = simulatorUrl;
    }

    /**
     * Get the simulation service name.
     *
     * @return the name of the simulator
     */
    public String getSimulatorName() {
        return simulatorName;
    }

    /**
     * Set the simulation service name.
     *
     * @param simulatorName the name of the simulator
     * @return this instance for method chaining
     */
    public SimulationServiceInfo setSimulatorName(String simulatorName) {
        this.simulatorName = simulatorName;
        return this;
    }

    /**
     * Get the simulation service version.
     *
     * @return the version of the simulator
     */
    public String getSimulatorVersion() {
        return simulatorVersion;
    }

    /**
     * Set the simulation service version.
     *
     * @param simulatorVersion the version of the simulator
     * @return this instance for method chaining
     */
    public SimulationServiceInfo setSimulatorVersion(String simulatorVersion) {
        this.simulatorVersion = simulatorVersion;
        return this;
    }

    /**
     * Get the simulation service URL.
     *
     * @return the URL of the simulator service
     */
    public String getSimulatorUrl() {
        return simulatorUrl;
    }

    /**
     * Set the simulation service URL.
     *
     * @param simulatorUrl the URL of the simulator service
     * @return this instance for method chaining
     */
    public SimulationServiceInfo setSimulatorUrl(String simulatorUrl) {
        this.simulatorUrl = simulatorUrl;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SimulationServiceInfo that)) return false;
        return Objects.equals(simulatorName, that.simulatorName)
                && Objects.equals(simulatorVersion, that.simulatorVersion)
                && Objects.equals(simulatorUrl, that.simulatorUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(simulatorName, simulatorVersion, simulatorUrl);
    }
}
