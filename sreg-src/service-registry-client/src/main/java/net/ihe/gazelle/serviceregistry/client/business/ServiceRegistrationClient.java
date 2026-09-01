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

package net.ihe.gazelle.serviceregistry.client.business;

import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.serviceregistry.api.business.ServiceId;

import java.util.function.Consumer;

/**
 * ServiceRegistrationClient is the interface for clients that want to register themselves into Service-Registry. It
 * provides methods to connect, register, and keep the connection alive.
 */
public interface ServiceRegistrationClient {

   /**
    * Connects to the service registry and register the service. This method blocks until the connection is established
    * or an error occurs.
    *
    * @param service       the service to register.
    * @param eventConsumer a consumer that will receive all the events related to the registration process as long as
    *                      the session is mmaintained open. All events will be ignored if null.
    *
    * @return a RegistrationSession that can be used to keep the connection alive by calling
    * {@link #keepAlive(RegistrationSession)}.
    *
    * @throws IllegalArgumentException     if the service is not valid.
    * @throws ServiceRegistrationException if the connection fails.
    */
   RegistrationSession connectAndRegister(Service service, Consumer<Event> eventConsumer);

   /**
    * Keeps the connection alive by re-sending registration message, or try to reconnect if the connection is lost.
    * There is no need to call {@link RegistrationSession#isOpen()}  before.
    * <p>
    * Service registry will regularly purge registration of services that are no longer connected, So it is recommended
    * to regularly call this method to ensure the connection remains active.
    *
    * @param session the session to keep alive.
    *
    * @throws ServiceRegistrationException if the re-establishing a lost connection fails.
    */
   void keepAlive(RegistrationSession session);


   /**
    * Represents a registration session of one service.
    */
   interface RegistrationSession extends AutoCloseable {

      /**
       * Is the session opened with the service registry?
       *
       * @return true if connected, false otherwise.
       */
      boolean isOpen();

      /**
       * Quietly closes the connection to the service registry. There is no need to call {@link #isOpen()} before.
       */
      @Override
      void close();

      /**
       * Gets the ServiceId associated with this registration session.
       *
       * @return the ServiceId of the service being registered
       */
      ServiceId getServiceId();

   }

   /**
    * Represent an event that can be emitted during the service registration process.
    *
    * @param level     the level of the event.
    * @param message   the description of the event
    * @param throwable an optional throwable that can be associated with the event. Can be null, see this other
    *                  constructor
    */
   record Event(Level level, String message, Throwable throwable) {

      /**
       * Represents the importance of the event.
       */
      public enum Level {
         /**
          * Debug level, used for detailed debugging information. Not relevant on a daily basis.
          */
         DEBUG,
         /**
          * Informational level, used for general information about the registration process.
          */
         INFO,
         /**
          * Warning level, used for events that are not errors but may require attention, either hazardous situation or
          * security information.
          */
         WARN,
         /**
          * Error level, used for events that indicate a failure in the registration process and probably need an action
          * from an administrator.
          */
         ERROR
      }

      /**
       * Constructs an Event with the specified level and message.
       *
       * @param level   the level of the event
       * @param message the description of the event
       */
      public Event(Level level, String message) {
         this(level, message, null);
      }

   }
}
