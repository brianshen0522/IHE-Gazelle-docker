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
import java.io.Serializable;

/**
 * Implementation of ValueHolder for concrete value
 */
class DirectValue implements ValueHolder {

   @Serial
   private static final long serialVersionUID = 5435920465930342207L;

   /**
    * The value.
    */
   private final Serializable value;

   /**
    * Constructs a new DirectValue with the specified value.
    *
    * @param value the Serializable value to be held; can be null
    */
   public DirectValue(Serializable value) {
      this.value = value;
   }

   @Override
   public Serializable getValue() {
      return value;
   }

   /**
    * Checks whether the value is defined.
    *
    * @return true if the value is not null, otherwise false
    */
   public boolean isValueDefined() {
      return value != null;
   }

}
