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

package net.ihe.gazelle.itb.gateway.business;

import jakarta.inject.Inject;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbReporting;
import java.util.NoSuchElementException;

/**
 * Receives ITB callbacks, enriches them with logs/PDF, and completes waiting session futures.
 */
public class ItbReportingService {

   private final ItbSessionStore sessionStore;
   private final ItbReportingEnrichmentService enrichmentService;

   /**
    * Creates an ITB reporting service.
    *
    * @param itbClient ITB HTTP client
    * @param sessionStore pending-session store
   */
   @Inject
   public ItbReportingService(ItbClient itbClient, ItbSessionStore sessionStore) {
      this.sessionStore = sessionStore;
      this.enrichmentService = new ItbReportingEnrichmentService(itbClient);
   }

   /**
    * Callback to receive ITB reporting
    *
    * @param itbReporting the ITB reporting
    * @throws NoSuchElementException if no session is found for the given itb session ID
    */
   public void receiveReporting(ItbReporting itbReporting) {
      String sessionID = itbReporting.getTestSession().getTestSessionIdentifier();
      var future = sessionStore.get(sessionID);
      if (future != null) {
         future.complete(enrichmentService.enrich(itbReporting));
         sessionStore.remove(sessionID);
      } else {
         throw new NoSuchElementException("No session found for ID " + sessionID);
      }
   }

}
