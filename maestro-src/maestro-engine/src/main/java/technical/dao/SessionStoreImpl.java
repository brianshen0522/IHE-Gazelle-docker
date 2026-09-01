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

package technical.dao;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import net.ihe.gazelle.maestro.engine.business.context.SessionStore;

import java.util.concurrent.TimeUnit;

/**
 * The purpose of this class is to being able to access any TestRunSession or TestSuiteSession from any Maestro
 * Engine instance. The asynchronous execution of the Maestro engine involves that the executed StepRun or TestRun can be
 * managed by another Maestro instance than the one that launches its execution.
 * So we want that only with its session id, we can figure out from which TestRunSession or TestSuiteSession
 * this SteRun/TestRun depends on.
 *
 * @param <T> The type of object cached in the store
 */
public class SessionStoreImpl<T> implements SessionStore<T> {

   private final Cache<String, CacheEntry<T>> sessions;

   /**
    * Creates a new {@code SessionStoreImpl} with an internal cache that manages session expirations.
    */
   public SessionStoreImpl() {
      sessions = Caffeine.newBuilder()
            .expireAfter(new SessionExpiry())
            .build();
   }

   /**
    * Adds a session to the store with a specified timeout.
    *
    * @param id the unique session ID
    * @param session the session object to store
    * @param timeoutMillis the timeout in milliseconds for the session
    */
   @Override
   public synchronized void addSession(String id, T session, long timeoutMillis) {
      sessions.put(id, new CacheEntry<>(session, timeoutMillis));
   }

   /**
    * Checks if a session with the given ID exists in the store.
    *
    * @param id the session ID to check
    * @return {@code true} if a session exists for the given ID, {@code false} otherwise
    */
   @Override
   public synchronized boolean hasSession(String id) {
      return sessions.getIfPresent(id) != null;
   }

   /**
    * Retrieves the session associated with the given ID.
    *
    * @param id the session ID
    * @return the session object if found, or {@code null} if no session exists for the ID
    */
   @Override
   public synchronized T getSession(String id) {
      CacheEntry<T> entry = sessions.getIfPresent(id);
      return entry != null ? entry.value : null;
   }

   /**
    * Removes the session associated with the given ID from the store.
    *
    * @param id the session ID to remove
    */
   @Override
   public synchronized void removeSession(String id) {
      sessions.invalidate(id);
   }

   private final class SessionExpiry implements Expiry<String, CacheEntry<T>> {

      @Override
      public long expireAfterCreate(String key, CacheEntry<T> entry, long now) {
         return entry.ttlNanos;
      }

      @Override
      public long expireAfterUpdate(String key, CacheEntry<T> entry, long now, long currentDuration) {
         return currentDuration;
      }

      @Override
      public long expireAfterRead(String key, CacheEntry<T> entry, long now, long currentDuration) {
         return currentDuration;
      }
   }

   private record CacheEntry<T>(T value, long ttlNanos) {
      private CacheEntry(T value, long ttlNanos) {
         this.value = value;
         this.ttlNanos = TimeUnit.MILLISECONDS.toNanos(ttlNanos);
      }
   }
}
