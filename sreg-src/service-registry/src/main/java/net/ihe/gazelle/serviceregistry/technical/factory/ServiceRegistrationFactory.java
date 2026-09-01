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

package net.ihe.gazelle.serviceregistry.technical.factory;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.serviceregistry.business.registration.RegistrationConfiguration;
import net.ihe.gazelle.serviceregistry.business.registration.ServiceRegistration;
import net.ihe.gazelle.serviceregistry.business.registration.ServiceRegistrationDAO;

/**
 * Factory class for producing default instances of ServiceRegistration.
 */
public class ServiceRegistrationFactory {

   private final ServiceRegistrationDAO serviceRegistrationDAO;
   private final RegistrationConfiguration registrationConfiguration;
   private final Authz authz;

   /**
    * Constructor for ServiceRegistrationFactory.
    *
    * @param serviceRegistrationDAO the DAO for service registrations
    * @param registrationConfiguration the configuration for service registration
    * @param authz the authorization service
    */
   @Inject
   public ServiceRegistrationFactory(ServiceRegistrationDAO serviceRegistrationDAO,
                                     RegistrationConfiguration registrationConfiguration, Authz authz) {
      this.serviceRegistrationDAO = serviceRegistrationDAO;
      this.registrationConfiguration = registrationConfiguration;
      this.authz = authz;
   }

   /**
    * Produces a default instance of ServiceRegistration.
    *
    * @return a new instance of ServiceRegistration
    */
   @Default
   @Produces
   public ServiceRegistration createServiceRegistration() {
      return new ServiceRegistration(serviceRegistrationDAO, registrationConfiguration, authz);
   }

}
