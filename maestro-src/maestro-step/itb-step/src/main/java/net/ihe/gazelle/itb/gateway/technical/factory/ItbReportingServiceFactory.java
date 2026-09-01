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

package net.ihe.gazelle.itb.gateway.technical.factory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.ihe.gazelle.itb.gateway.business.ItbClient;
import net.ihe.gazelle.itb.gateway.business.ItbReportingService;
import net.ihe.gazelle.itb.gateway.technical.dao.ItbSessionStoreImpl;

/**
 * CDI factory creating {@link ItbReportingService} with default session store implementation.
 */
@ApplicationScoped
public class ItbReportingServiceFactory {

   private final ItbClient itbClient;

   /**
    * Creates the factory.
    *
    * @param itbClient ITB client dependency
    */
   @Inject
   public ItbReportingServiceFactory(ItbClient itbClient) {
      this.itbClient = itbClient;
   }

   /**
    * Produces the default ITB reporting service bean.
    *
    * @return ITB reporting service
    */
   @Default
   @Produces
   public ItbReportingService createItbReportingService() {
      return new ItbReportingService(itbClient, new ItbSessionStoreImpl());
   }

}
