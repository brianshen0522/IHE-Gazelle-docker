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

package net.ihe.gazelle.serviceregistry.technical.security;

import net.ihe.gazelle.security.technical.rbac.RBACMatrixProviderImpl;
import net.ihe.gazelle.serviceregistry.business.permission.ServiceRegistryPermissionStore;

/**
 * Technical PermissionStore wrapper to expose a constructor without parameters for SPI while respecting DIP.
 */
public class ServiceRegistryRBACPermissionStore extends ServiceRegistryPermissionStore {

    /** Public constructor for SPI */
    public ServiceRegistryRBACPermissionStore() {
        super(new RBACMatrixProviderImpl());
    }

}
