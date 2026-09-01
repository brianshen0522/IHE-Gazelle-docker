/*
 * Copyright 2026 IHE International.
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

package net.ihe.gazelle.serviceregistry.technical.job;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import net.ihe.gazelle.security.business.BaseGazelleIdentity;
import net.ihe.gazelle.security.business.Groups;
import net.ihe.gazelle.servicemetadata.api.business.MetadataService;
import net.ihe.gazelle.serviceregistry.business.registration.ServiceRegistration;

import java.util.Set;

/**
 * Auto-register Service Registry metadata
 */
public class SelfRegistrationJob {

    private final MetadataService serviceRegistryMetadata;
    private final ServiceRegistration serviceRegistration;

    /**
     * Default Constructor
     *
     * @param serviceRegistryMetadata Service Metadata of Service Registry
     * @param serviceRegistration     ServiceRegistration service
     */
    @Inject
    public SelfRegistrationJob(MetadataService serviceRegistryMetadata, ServiceRegistration serviceRegistration) {
        this.serviceRegistryMetadata = serviceRegistryMetadata;
        this.serviceRegistration = serviceRegistration;
    }

    /**
     * Startup event listener that triggers the Service Registry metadata registration.
     *
     * @param startupEvent startup event to react to.
     */
    public void onStart(@Observes StartupEvent startupEvent) {
        serviceRegistration.connectService(
                serviceRegistryMetadata.getMetadata(),
                new BaseGazelleIdentity(() -> "Service Registry").setGroups(Set.of(Groups.MACHINE))
        );
    }

}
