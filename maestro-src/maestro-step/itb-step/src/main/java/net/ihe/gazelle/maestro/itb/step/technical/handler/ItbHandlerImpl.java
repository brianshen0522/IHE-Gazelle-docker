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

package net.ihe.gazelle.maestro.itb.step.technical.handler;

import net.ihe.gazelle.itb.gateway.technical.client.ItbHttpClient;
import net.ihe.gazelle.maestro.itb.step.business.ItbHandler;

/**
 * Implementation of {@link ItbHandler}.
 */
public class ItbHandlerImpl extends ItbHttpClient implements ItbHandler {

   /**
    * Constructs an instance of {@code ItbHandlerImpl} by initializing its base URL and API key.
    *
    * @param baseItbUrl the base URL of the ITB service
    * @param itbApiKey the API key used for authenticating with the ITB service
    */
   public ItbHandlerImpl(String baseItbUrl, String itbApiKey) {
      super(baseItbUrl, itbApiKey);
   }

   @Override
   public boolean isAvailable() {
      // Availability probing can be added later; current behavior keeps compatibility with existing flow.
      return true;
   }

}
