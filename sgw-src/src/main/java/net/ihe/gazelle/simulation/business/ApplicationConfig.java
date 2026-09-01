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

/**
 * This interface is used for the simulation gateway application configuration.
 */
public interface ApplicationConfig {

    /**
     * Retrieve URL of the service registry.
     * @return service registry url as string.
     */
    String getServiceRegistryUrl();

    /**
     * Retrieve timeout of the cached simulation services in simulation gateway application
     * @return the value in minutes of this timeout.
     */
    int getServicesCacheTimeoutMinutes();

    /**
     * Retrieve timeout of the cached sequences in simulation gateway application.
     * @return the value in minutes of this timeout.
     */
    int getSequencesCacheMaxTimeoutMinutes();

    /**
     * Retrieve URL of the SVS Simulator.
     * @return SVS Simulator as string
     */
    String getSvsUrl();
}
