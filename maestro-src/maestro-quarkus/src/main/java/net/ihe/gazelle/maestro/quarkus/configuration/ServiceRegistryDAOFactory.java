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

package net.ihe.gazelle.maestro.quarkus.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;

/**
 * Factory class to produce {@link ServiceRegistryDAO} instances for CDI injection.
 */
@ApplicationScoped
public class ServiceRegistryDAOFactory {

   /**
    * Default constructor
    */
   public ServiceRegistryDAOFactory() {
      // Empty
   }

   /**
    * Produces a new {@link ServiceRegistryDAO} instance.
    *
    * @return a new {@link ServiceRegistryDAOImpl} instance
    */
   @Produces
   ServiceRegistryDAO getServiceRegistry() {
      return new ServiceRegistryDAOImpl();
   }
}
