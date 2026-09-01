/*
 * Copyright 2025 IHE International.
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

package net.ihe.gazelle.user.management.quarkus.interlay.metadata;

import net.ihe.gazelle.servicemetadata.api.technical.AbstractMetadataService;

/**
 * Metadata service definition for Gazelle User Management.
 */
public class GUMMetadataService extends AbstractMetadataService {

    /**
     * Creates the metadata service with its class reference.
     */
    public GUMMetadataService() {
        super(GUMMetadataService.class);
    }

    @Override
    public String getServiceName() {
        return "User Management";
    }

    @Override
    public String getServiceDescription() {
        return "Gazelle user management is the service responsible to manage users and all the authentication and authorization aspects.";
    }

}
