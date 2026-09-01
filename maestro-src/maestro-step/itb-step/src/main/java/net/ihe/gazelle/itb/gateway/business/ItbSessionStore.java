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

package net.ihe.gazelle.itb.gateway.business;

import net.ihe.gazelle.itb.gateway.business.reporting.ItbReporting;

import java.util.concurrent.CompletableFuture;

/**
 * Stores pending ITB session callbacks keyed by session identifier.
 */
public interface ItbSessionStore {
   /**
    * Retrieves callback future for a session.
    *
    * @param sessionId session identifier
    * @return registered future, or {@code null}
    */
   CompletableFuture<ItbReporting> get(String sessionId);

   /**
    * Registers callback future for a session.
    *
    * @param sessionId session identifier
    * @param future callback future
    */
   void add(String sessionId, CompletableFuture<ItbReporting> future);

   /**
    * Removes callback registration for a session.
    *
    * @param sessionId session identifier
    */
   void remove(String sessionId);
}
