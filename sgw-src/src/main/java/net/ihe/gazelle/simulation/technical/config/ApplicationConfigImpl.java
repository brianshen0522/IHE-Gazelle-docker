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

import jakarta.enterprise.context.RequestScoped;
import net.ihe.gazelle.simulation.business.ApplicationConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Implementation of the {@link ApplicationConfig} interface.
 */
@RequestScoped
public class ApplicationConfigImpl implements ApplicationConfig {

    @ConfigProperty(name = "gzl.service.registry.url")
    String serviceRegistryUrl;

    @ConfigProperty(name = "gzl.simulation.gateway.services.cache.timeout.minutes")
    int servicesCacheTimeoutMinutes;

    @ConfigProperty(name = "gzl.simulation.gateway.sequences.cache.max.timeout.minutes")
    int sequencesCacheMaxTimeoutMinutes;

    @ConfigProperty(name = "gzl.svs.url")
    String svsUrl;

   /**
    * Constructor
    */
   public ApplicationConfigImpl() {
       // Empty
    }

    @Override
    public String getServiceRegistryUrl() {
        return serviceRegistryUrl;
    }

    @Override
    public int getServicesCacheTimeoutMinutes() {
        return servicesCacheTimeoutMinutes;
    }

    @Override
    public int getSequencesCacheMaxTimeoutMinutes() {
        return sequencesCacheMaxTimeoutMinutes;
    }

    @Override
    public String getSvsUrl() {
        return svsUrl;
    }
}
