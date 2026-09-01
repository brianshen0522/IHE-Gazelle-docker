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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class for resolving property dependencies in a collection of {@code Property} objects.
 * Properties can hold direct values or reference other properties. The {@code resolveProperties} method
 * ensures all references are correctly resolved to their corresponding values while maintaining type safety
 * and detecting circular dependencies.
 */
public class PropertyResolver {

   private PropertyResolver() {
      // private constructor to prevent instantiation
   }

   /**
    * Resolves all ReferenceValue instances by linking them with directValues from the provided list of properties.
    *
    * @param properties the list of properties to resolve.
    *
    * @throws IllegalStateException    if a circular reference is detected
    * @throws MissingPropertyException if a referenced property is not found
    * @throws PropertyTypeMismatchException if a property references another property of a different type
    */
   public static void resolveProperties(Collection<Property> properties) {
      for (Property property : properties) {
         if (property.isReference()) {
            supplyValue(property, properties, new HashSet<>());
         }
      }
   }

   private static void supplyValue(Property property,
                                   Collection<Property> properties,
                                   Set<String> visited) {
      ReferenceValue referenceValue = (ReferenceValue) property.getValueHolder();
      if (!referenceValue.isSupplied()) {
         String refName = referenceValue.getReference();
         assertNotALoop(visited, refName);
         Property referencedProperty = getPropertyByName(properties, refName);
         assertTypeMatch(property, referencedProperty);
         // once we identify the referenced property, we immediately resolve it to detect if there is a circular
         // reference
         if (referencedProperty.isReference()) {
            supplyValue(referencedProperty, properties, visited);
         }
         propagateByteArrayMetadata(property, referencedProperty);
         referenceValue.supply(referencedProperty::getValue);
         visited.remove(refName);
      }
   }

   private static void propagateByteArrayMetadata(Property property, Property referencedProperty) {
      if (property instanceof ByteArrayProperty target && referencedProperty instanceof ByteArrayProperty source) {
         if (target.getFileName() == null || target.getFileName().isBlank()) {
            target.setFileName(source.getFileName());
         }
         if (target.getMimeType() == null || target.getMimeType().isBlank()) {
            target.setMimeType(source.getMimeType());
         }
      }
   }

   private static Property getPropertyByName(Collection<Property> properties, String name) {
      return properties.stream()
            .filter(p -> p.getName().equals(name)).findFirst()
            .orElseThrow(() -> new MissingPropertyException("No property found with name: " + name));
   }

   private static void assertTypeMatch(Property property, Property referencedProperty) {
      if(!property.getClass().equals(referencedProperty.getClass())) {
         throw new PropertyTypeMismatchException("Property '" + property.getName() + "' of type " +
                                                 property.getClass().getSimpleName() +
                                                 " cannot reference property '" + referencedProperty.getName() + "' of type " +
                                                 referencedProperty.getClass().getSimpleName());
      }
   }

   private static void assertNotALoop(Set<String> visited, String refName) {
      if (!visited.add(refName)) {
         throw new IllegalStateException("Property references are forming a loop " + toString(visited, refName));
      }
   }

   private static String toString(Set<String> elements, String repeatingElement) {
      return "[" + String.join(", ", elements) + ", " + repeatingElement + "]";
   }

}
