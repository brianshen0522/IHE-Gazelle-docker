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

package technical.provider;

import net.ihe.gazelle.servicemetadata.api.business.Service;

import java.util.List;

/**
 * ServiceRegistry provides access to all available {@link Service} instances.
 */
public interface ServiceRegistry {

    /**
     * Retrieves the list of all services available in the database.
     *
     * @return a list of {@link Service} instances
     */
    List<Service> getServices();

    /**
     * Retrieves a {@link Service} by its name.
     *
     * @param serviceName the name of the service to retrieve
     * @return the {@link Service} instance with the specified name
     * @throws IllegalArgumentException if no service with the specified name exists in the database
     */
    Service getService(String serviceName);
}
