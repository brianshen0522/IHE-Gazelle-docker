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

import java.io.Serial;

/**
 * Exception thrown when there is an error during service registration.
 * This throwable extends RuntimeException and can be used to indicate
 * issues such as network errors, validation failures, or other unexpected
 * conditions that occur during the registration process.
 */
public class ServiceRegistrationException extends RuntimeException {

   @Serial
   private static final long serialVersionUID = -362035280894062161L;

   /**
    * Constructor for ServiceRegistrationException intialized with a message.
    *
    * @param message the detail message
    */
   public ServiceRegistrationException(String message) {
      super(message);
   }

   /**
    * Constructor for ServiceRegistrationException initialized with a message and a cause.
    *
    * @param message the detail message
    * @param cause   the cause of the throwable
    */
   public ServiceRegistrationException(String message, Throwable cause) {
      super(message, cause);
   }

   /**
    * Constructor for ServiceRegistrationException initialized with a cause.
    *
    * @param cause the cause of the throwable
    */
   public ServiceRegistrationException(Throwable cause) {
      super(cause);
   }
}
