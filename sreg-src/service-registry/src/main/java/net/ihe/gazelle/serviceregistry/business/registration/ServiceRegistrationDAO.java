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

package net.ihe.gazelle.serviceregistry.business.registration;

import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.ServiceId;

import java.util.List;

/**
 * ServiceRegistrationDAO is the Data Access Object interface for managing service registrations. It provides methods to
 * create, read, update, delete, and check the registration status of services.
 */
public interface ServiceRegistrationDAO {

   /**
    * Creates a new service entry.
    * <p>
    * Warning, this method must not be called if the service already exists. isServiceRegistered(ServiceId serviceId) should
    * be used to check that, call {@link #update(DeployedService)} if the service already exists.
    *
    * @param service the DeployedService to register
    */
   void create(DeployedService service);

   /**
    * Reads a service entry by its ServiceId.
    *
    * @param serviceId the ServiceId of the service to read
    *
    * @return the DeployedService associated with the given ServiceId, or null if not found.
    */
   DeployedService read(ServiceId serviceId);

   /**
    * Updates an existing service. Or create it if it does not exist.
    *
    * @param service the DeployedService with updated information
    *
    */
   void update(DeployedService service);

   /**
    * Deletes a service entry by its ServiceId. Does nothing if the service does not exist.
    *
    * @param serviceId the ServiceId of the service to delete
    */
   void delete(ServiceId serviceId);

   /**
    * Checks if a service is registered by its ServiceId.
    *
    * @param serviceId the ServiceId of the service to check
    *
    * @return true if the service is registered, false otherwise
    */
   boolean isServiceRegistered(ServiceId serviceId);

   /**
    * Retrieves a list of all self-registered services.
    *
    * @return a List of DeployedService objects that are self-registered
    */
   List<DeployedService> getSelfRegisteredServices();

}
