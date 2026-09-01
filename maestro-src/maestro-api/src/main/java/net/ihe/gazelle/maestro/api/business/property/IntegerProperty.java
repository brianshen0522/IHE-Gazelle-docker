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
 * Integer property
 */
public class IntegerProperty extends Property {

   @Serial
   private static final long serialVersionUID = 5284763560506283622L;

   /**
    * Default constructor
    */
   public IntegerProperty() {
      super();
   }

   /**
    * Constructs a new IntegerProperty with the specified name and value.
    *
    * @param name  the name of the property, which identifies it uniquely
    * @param value the value to be assigned to the property; must be a Serializable object
    */
   public IntegerProperty(String name, Serializable value) {
      super(name, value);
   }

   @Override
   public Property copy() {
      return new IntegerProperty(getName(), getCopyValue());
   }
}
