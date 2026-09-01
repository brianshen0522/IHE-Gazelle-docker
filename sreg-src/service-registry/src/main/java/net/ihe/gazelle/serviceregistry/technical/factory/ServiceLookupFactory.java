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
import net.ihe.gazelle.serviceregistry.business.lookup.ServiceLookup;
import net.ihe.gazelle.serviceregistry.business.lookup.ServiceLookupDAO;
import net.ihe.gazelle.serviceregistry.business.lookup.ServiceLookupImpl;

/**
 * Factory class for producing instances of ServiceLookup.
 */
public class ServiceLookupFactory {

   private final ServiceLookupDAO dao;
   private final Authz authz;

   /**
    * Constructor for ServiceLookupFactory.
    *
    * @param dao the DAO for service lookups
    * @param authz the authorization service
    */
   @Inject
   public ServiceLookupFactory(ServiceLookupDAO dao, Authz authz) {
      this.dao = dao;
      this.authz = authz;
   }

   /**
    * Produces a default instance of ServiceLookup.
    *
    * @return a new instance of ServiceLookupImpl
    */
   @Default
   @Produces
   public ServiceLookup createServiceLookup() {
      return new ServiceLookupImpl(dao, authz);
   }

}
