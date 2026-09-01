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

package net.ihe.gazelle.maestro.quarkus.websocket;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class responsible for managing WebSocket sessions.
 * Provides mechanisms to add and remove WebSocket sessions by their session ID.
 * Designed for use in a scoped application context.
 */
@ApplicationScoped
public class WebSocketSessionService {

   private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

   /**
    * Default constructor.
    */
   public WebSocketSessionService() {
      // Empty
   }

   /**
    * Adds a WebSocket session to the managed collection of sessions.
    *
    * @param sessionId the unique identifier for the WebSocket session
    * @param session the WebSocketSession instance to be associated with the provided sessionId
    */
   public void addSession(String sessionId, WebSocketSession session) {
      sessions.put(sessionId, session);
   }

   /**
    * Retrieves a WebSocket session associated with the specified session ID.
    *
    * @param sessionId the unique identifier of the WebSocket session to retrieve
    * @return the WebSocketSession instance associated with the provided session ID,
    *         or null if no session is found for the given ID
    */
   public WebSocketSession getSession(String sessionId) {
      return sessions.get(sessionId);
   }

   /**
    * Removes a WebSocket session from the managed collection of sessions.
    *
    * @param sessionId the unique identifier for the WebSocket session to be removed
    */
   public void removeSession(String sessionId) {
      sessions.remove(sessionId);
   }
}
