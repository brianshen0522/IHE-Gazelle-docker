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

package net.ihe.gazelle.maestro.api.business.property;

import java.io.Serial;

/**
 * Exception thrown when a property is missing
 */
public class MissingPropertyException extends RuntimeException {

   @Serial
   private static final long serialVersionUID = 3899927895039751597L;

   /**
    * Constructs a new MissingPropertyException with the specified detail message.
    *
    * @param message the detail message, providing more information about the exception
    */
   public MissingPropertyException(String message) {
      super(message);
   }
}
