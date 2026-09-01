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

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyResolverTest {

   @Test
   void shouldResolveDirectReference() {
      Property source = new StringProperty("source", "value");
      Property reference = new StringProperty("reference", "${source}");

      PropertyResolver.resolveProperties(List.of(source, reference));

      assertEquals("value", reference.getValue());
   }

   @Test
   void shouldResolveChainedReferences() {
      Property base = new StringProperty("base", "resolved");
      Property middle = new StringProperty("middle", "${base}");
      Property top = new StringProperty("top", "${middle}");

      PropertyResolver.resolveProperties(List.of(base, middle, top));

      assertEquals("resolved", middle.getValue());
      assertEquals("resolved", top.getValue());
   }

   @Test
   void shouldNotTreatDirectStringWithDashesAsReference() {
      Property profile = new StringProperty("validationProfile", "dicom-sr-conformance");

      PropertyResolver.resolveProperties(List.of(profile));

      assertEquals("dicom-sr-conformance", profile.getValue());
      assertNull(profile.getReferenceName());
   }

   @Test
   void shouldResolveReferenceWithDashesInPropertyName() {
      Property source = new StringProperty("dicom-sr-conformance", "profile-id");
      Property reference = new StringProperty("validationProfile", "${dicom-sr-conformance}");

      PropertyResolver.resolveProperties(List.of(source, reference));

      assertEquals("profile-id", reference.getValue());
   }

   @Test
   void shouldDetectCircularReferences() {
      Property first = new StringProperty("first", "${second}");
      Property second = new StringProperty("second", "${first}");

      List<Property> props = List.of(first, second);
      assertThrows(IllegalStateException.class, () ->
            PropertyResolver.resolveProperties(props)
      );
   }

   @Test
   void shouldDetectMissingReference() {
      Property orphan = new StringProperty("orphan", "${missing}");

      List<Property> orphans = List.of(orphan);
      assertThrows(MissingPropertyException.class, () ->
            PropertyResolver.resolveProperties(orphans)
      );
   }

   @Test
   void shouldDetectTypeMismatch() {
      Property integerProperty = new IntegerProperty("number", 42);
      Property stringReference = new StringProperty("stringRef", "${number}");

      List<Property> properties = List.of(integerProperty, stringReference);
      assertThrows(PropertyTypeMismatchException.class, () ->
            PropertyResolver.resolveProperties(properties)
      );
   }

   @Test
   void shouldPropagateByteArrayMetadataFromReferencedProperty() {
      ByteArrayProperty source = (ByteArrayProperty) new ByteArrayProperty("inputFile1", "payload".getBytes(StandardCharsets.UTF_8))
            .setFileName("input.xml")
            .setMimeType("application/xml");
      ByteArrayProperty reference = (ByteArrayProperty) new ByteArrayProperty("contentToValidate", "${inputFile1}");

      PropertyResolver.resolveProperties(List.of(source, reference));

      assertEquals("input.xml", reference.getFileName());
      assertEquals("application/xml", reference.getMimeType());
      assertEquals("inputFile1", reference.getReferenceName());
   }

   @Test
   void getReferenceNameShouldReturnNullForDirectValues() {
      Property property = new StringProperty("plain", "value");
      assertNull(property.getReferenceName());
   }
}
