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

package net.ihe.gazelle.maestro.api.business.property;

import java.io.Serial;
import java.io.Serializable;

/**
 * A property containing a byte array
 */
public class ByteArrayItemProperty extends ByteArrayProperty {

   @Serial
   private static final long serialVersionUID = 7729334463586120850L;

   /**
    * The type of the item.
    */
   private String itemType;

   /**
    * The reference to the item.
    */
   private String reference;

   /**
    * Default constructor
    */
   public ByteArrayItemProperty() {
      super();
   }

   /**
    * Constructs a new {@code ByteArrayItemProperty} with the specified name and value.
    *
    * @param name  the name of the property, which uniquely identifies it
    * @param value the value to be assigned to this property; must be a {@code Serializable} object
    */
   public ByteArrayItemProperty(String name, Serializable value) {
      super(name, value);
   }

   /**
    * Constructs a new {@code ByteArrayItemProperty} instance by copying the details
    * from the given {@code ByteArrayProperty}.
    *
    * @param property the {@code ByteArrayProperty} instance whose details (name, value,
    *                 file name, and MIME type) will be used to initialize the newly
    *                 created {@code ByteArrayItemProperty}
    */
   public ByteArrayItemProperty(ByteArrayProperty property) {
      super(property.getName(), property.getValue());
      super.setFileName(property.getFileName());
      super.setMimeType(property.getMimeType());
   }

   /**
    * Retrieves the item type of this {@code ByteArrayItemProperty}.
    *
    * @return the item type as a {@code String}, or {@code null} if the item type has not been defined
    */
   public String getItemType() {
      return itemType;
   }

   /**
    * Sets the item type for this {@code ByteArrayItemProperty}.
    *
    * @param itemType the item type to be assigned as a {@code String}
    * @return the updated {@code ByteArrayItemProperty} instance with the new item type
    */
   public ByteArrayItemProperty setItemType(String itemType) {
      this.itemType = itemType;
      return this;
   }

   /**
    * Retrieves the reference associated with this property.
    *
    * @return the reference as a String, or null if the reference has not been set
    */
   public String getReference() {
      return reference;
   }

   /**
    * Sets the reference for this {@code ByteArrayItemProperty}.
    *
    * @param reference the reference to be assigned as a {@code String}
    * @return the updated {@code ByteArrayItemProperty} instance with the new reference
    */
   public ByteArrayItemProperty setReference(String reference) {
      this.reference = reference;
      return this;
   }

   /**
    * Evaluates whether the reference is defined if the property value is null.
    *
    * @return {@code true} if the property value is not null or if the reference is not null;
    *         {@code false} otherwise.
    */
   public boolean isReferenceDefinedIfValueNull() {
      return getValue() != null || reference != null;
   }

}
