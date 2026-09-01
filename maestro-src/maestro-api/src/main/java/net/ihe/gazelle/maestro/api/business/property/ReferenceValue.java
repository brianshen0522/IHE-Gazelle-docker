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
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ReferenceValue is an implementation of the ValueHolder interface that represents a value
 * which may reference other variables or values based on a defined syntax.
 * <p>
 * This class provides functionality to:
 * - Parse and store a reference string with a specific syntax pattern.
 * - Supply a dynamic value using a Supplier.
 * - Determine if the reference is supplied by a Supplier or formatted directly.
 * - Check if a reference is defined and access its content.
 */
class ReferenceValue implements ValueHolder {

   @Serial
   private static final long serialVersionUID = -8735006520614740322L;

   /**
    * The regular expression pattern used to parse the reference string.
    */
   static final String REFERENCE_REGEX = "^\\$\\{([^}]+)}$";

   /**
    * The compiled pattern used to match the reference string.
    */
   static final Pattern REFERENCE_PATTERN = Pattern.compile(REFERENCE_REGEX);

   /**
    * The reference string.
    */
   private String reference;

   /**
    * The supplier used to supply the value.
    */
   private transient Supplier<Serializable> valueSupplier;

   /**
    * Constructs a new {@code ReferenceValue} with the provided reference string.
    *
    * @param reference the reference string to be parsed and stored; expected to match
    *                  the syntax pattern defined by {@code REFERENCE_PATTERN}
    */
   ReferenceValue(String reference) {
      Matcher matcher = REFERENCE_PATTERN.matcher(reference);
      this.reference = matcher.matches() ? matcher.group(1) : reference;
   }

   /**
    * Retrieves the reference string associated with this {@code ReferenceValue}.
    *
    * @return the reference string, which may represent the identifier or value
    *         based on the syntax defined by the {@code REFERENCE_PATTERN}
    */
   String getReference() {
      return reference;
   }

   /**
    * Sets a new reference string for this {@code ReferenceValue}.
    *
    * @param reference the new reference string to be assigned; it may follow the syntax defined by {@code REFERENCE_PATTERN}
    * @return the updated {@code ReferenceValue} instance with the new reference string
    */
   ReferenceValue setReference(String reference) {
      this.reference = reference;
      return this;
   }

   @Override
   public Serializable getValue() {
      return valueSupplier != null ? valueSupplier.get() : format(reference);
   }

   /**
    * Sets a supplier to provide the value dynamically. The supplied value will be used
    * as the reference value instead of directly using a static reference or value.
    *
    * @param valueSupplier a {@code Supplier} that supplies a {@code Serializable} value.
    *                      This allows dynamic assignment of the value based on custom logic.
    * @return the current {@code ReferenceValue} instance with the supplied value configured.
    */
   ReferenceValue supply(Supplier<Serializable> valueSupplier) {
      this.valueSupplier = valueSupplier;
      return this;
   }

   /**
    * Checks whether the {@code ReferenceValue} instance has a supplier for dynamically
    * providing its value.
    *
    * @return {@code true} if a value supplier is defined, otherwise {@code false}.
    */
   boolean isSupplied() {
      return valueSupplier != null;
   }

   /**
    * Formats the provided reference identifier into a string representation
    * by encapsulating it within a specific syntax pattern.
    *
    * @param referenceId the identifier to be formatted; typically represents
    *                    a reference or key that will be encapsulated
    * @return a formatted string containing the reference identifier within
    *         a specific syntax pattern
    */
   static String format(String referenceId) {
      return "${" + referenceId + "}";
   }

   /**
    * Checks whether the reference value is defined.
    * A reference is considered defined if it is not null and is not a blank string.
    *
    * @return true if the reference is not null and not blank, otherwise false
    */
   public boolean isReferenceDefined() {
      return reference != null && !reference.isBlank();
   }
}
