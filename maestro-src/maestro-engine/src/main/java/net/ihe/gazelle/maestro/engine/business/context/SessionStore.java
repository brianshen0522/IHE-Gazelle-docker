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

package net.ihe.gazelle.maestro.engine.business.context;

/**
 * Stores session objects and provides access to them by their identifier.
 *
 * @param <T> the type of session object stored and retrieved
 */
public interface SessionStore<T> extends ReadSessionStore<T> {

   /**
    * Adds a session object with the specified identifier and timeout.
    *
    * @param id the identifier for the session
    * @param session the session object to store
    * @param timeout the time in milliseconds after which the session expires
    */
   void addSession(String id, T session, long timeout);

   /**
    * Checks whether a session exists for the given identifier.
    *
    * @param id the identifier of the session
    * @return {@code true} if a session exists, {@code false} otherwise
    */
   boolean hasSession(String id);

   /**
    * Removes the session associated with the given identifier.
    *
    * @param id the identifier of the session to remove
    */
   void removeSession(String id);

}
