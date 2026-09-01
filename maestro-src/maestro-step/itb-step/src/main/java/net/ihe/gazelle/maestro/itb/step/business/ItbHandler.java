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

package net.ihe.gazelle.maestro.itb.step.business;

import net.ihe.gazelle.itb.gateway.business.ItbClient;
import net.ihe.gazelle.maestro.spi.business.Handler;

/**
 * Represents a handler interface for ITB (Integration Test Bed) services.
 * It combines the functionality of {@link ItbClient} and {@link Handler},
 * allowing implementations to manage ITB test sessions and check service availability.
 * <br>
 * The constant {@code ITB_SERVICE_NAME} provides the name of the ITB service.
 */
public interface ItbHandler extends ItbClient, Handler {

   /**
    * The name of the Integration Test Bed (ITB) service.
    * This constant is used to identify and reference the ITB service within the system.
    */
   String ITB_SERVICE_NAME = "ITB";

}
