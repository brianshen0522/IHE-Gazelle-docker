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

package net.ihe.gazelle.maestro.spi.business;

import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.Step;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StepRunTest {

   @Test
   void getPropertyReferenceNameResolvesToTerminalInputReference() {
      ByteArrayProperty sourceInput = new ByteArrayProperty("inputFile1", "payload".getBytes(StandardCharsets.UTF_8));
      ByteArrayProperty mappedInput = new ByteArrayProperty("AuditMessage", "${inputFile1}");
      Step validationStep = new Step()
            .setType("VALIDATION")
            .setProperties(List.of(
                  new ByteArrayProperty("contentToValidate", "${AuditMessage}")
            ));

      StepRun stepRun = new StepRun(validationStep, List.of(sourceInput, mappedInput));

      assertEquals("inputFile1", stepRun.getPropertyReferenceName("contentToValidate"));
   }

   @Test
   void getPropertyReferenceNameSupportsReferenceNameWithDashes() {
      ByteArrayProperty sourceInput = new ByteArrayProperty("dicom-sr-conformance", "payload".getBytes(StandardCharsets.UTF_8));
      ByteArrayProperty mappedInput = new ByteArrayProperty("audit-input", "${dicom-sr-conformance}");
      Step validationStep = new Step()
            .setType("VALIDATION")
            .setProperties(List.of(
                  new ByteArrayProperty("contentToValidate", "${audit-input}"),
                  new StringProperty("validationProfile", "dicom-sr-conformance")
            ));

      StepRun stepRun = new StepRun(validationStep, List.of(sourceInput, mappedInput));

      assertEquals("dicom-sr-conformance", stepRun.getPropertyReferenceName("contentToValidate"));
      assertNull(stepRun.getPropertyReferenceName("validationProfile"));
   }
}
