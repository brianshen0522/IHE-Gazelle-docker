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

package net.ihe.gazelle.maestro.engine.business;

import net.ihe.gazelle.maestro.api.business.test.Step;

import java.io.Serial;

/**
 * Exception thrown when a {@link Step} is not recognized or cannot be found.
 */
public class UnknownStepException extends RuntimeException {

   @Serial
   private static final long serialVersionUID = -2327363841694476903L;

   /**
    * Creates a new {@code UnknownStepException} with the specified message.
    *
    * @param message the detail message
    */
   public UnknownStepException(String message) {
      super(message);
   }
}
