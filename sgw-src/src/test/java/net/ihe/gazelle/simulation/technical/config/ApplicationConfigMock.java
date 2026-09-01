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

package net.ihe.gazelle.simulation.technical.config;

import net.ihe.gazelle.simulation.business.ApplicationConfig;

public class ApplicationConfigMock implements ApplicationConfig {

    private String serviceRegistryUrl;

    public ApplicationConfigMock() {
    }

    public ApplicationConfigMock(String serviceRegistryUrl) {
        this.serviceRegistryUrl = serviceRegistryUrl;
    }

    @Override
    public String getServiceRegistryUrl() {
        return serviceRegistryUrl;
    }

    @Override
    public int getServicesCacheTimeoutMinutes() {
        return 1;
    }

    @Override
    public int getSequencesCacheMaxTimeoutMinutes() {
        return 10;
    }


    @Override
    public String getSvsUrl() {
        return "http://localhost:18080/SVSSimulator/rest";
    }
}
