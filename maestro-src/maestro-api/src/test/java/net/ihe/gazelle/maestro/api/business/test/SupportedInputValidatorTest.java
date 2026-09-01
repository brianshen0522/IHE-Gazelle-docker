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

package net.ihe.gazelle.maestro.api.business.test;

import net.ihe.gazelle.framework.modelvalidator.business.ObjectResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.wildfly.common.Assert.assertFalse;

class SupportedInputValidatorTest {

   @Test
   void testValidSupportedFileInput() {
      SupportedFileInput fileInput = (SupportedFileInput) new SupportedFileInput()
            .setId("file1")
            .setLabel("A file")
            .setRequired(true);
      SupportedFileInputValidator validator = new SupportedFileInputValidator();
      assertDoesNotThrow(() -> validator.assertValid(fileInput));
   }

   @Test
   void testIdMustBeDefined() {
      SupportedFileInput fileInput = (SupportedFileInput) new SupportedFileInput()
            .setLabel("A file")
            .setRequired(true);
      SupportedFileInputValidator validator = new SupportedFileInputValidator();
      assertInvalid(validator.validate(fileInput), "ID must be set and not empty => invalid");
   }

   @Test
   void testLabelMustBeDefined() {
      SupportedFileInput fileInput = (SupportedFileInput) new SupportedFileInput()
            .setId("file1")
            .setRequired(true);
      SupportedFileInputValidator validator = new SupportedFileInputValidator();
      assertInvalid(validator.validate(fileInput), "Label must be set and not empty => invalid");
   }

   @Test
   void testValidSupportedTextInput() {
      SupportedTextInput textInput = (SupportedTextInput) new SupportedTextInput()
            .setId("text1")
            .setLabel("A text input")
            .setRequired(false);
      SupportedTextInputValidator validator = new SupportedTextInputValidator();
      Assertions.assertDoesNotThrow(() -> validator.assertValid(textInput));
   }

   @Test
   void testPossibleTextValueNotNullIfPresent() {
      SupportedTextInput textInput = (SupportedTextInput) new SupportedTextInput()
            .setPossibleValues(Arrays.asList(new String[]{"value1", null}))
            .setId("text1")
            .setLabel("A text input")
            .setRequired(false);
      SupportedTextInputValidator validator = new SupportedTextInputValidator();
      assertInvalid(validator.validate(textInput), "possibleValues must not contain null or empty values => invalid");
   }

   @Test
   void testValidSupportedBooleanInput() {
      SupportedBooleanInput booleanInput = (SupportedBooleanInput) new SupportedBooleanInput()
            .setId("bool1")
            .setLabel("A boolean input")
            .setRequired(true);
      SupportedBooleanInputValidator validator = new SupportedBooleanInputValidator();
      assertDoesNotThrow(() -> validator.assertValid(booleanInput));
   }

   @Test
   void testValidSupportedValueSetInput() {
      SupportedValueSetInput valueSet = (SupportedValueSetInput) new SupportedValueSetInput()
            .setValueSetId("1.2.3.4.5")
            .setId("set1")
            .setLabel("A value set");
      SupportedValueSetInputValidator validator = new SupportedValueSetInputValidator();
      assertDoesNotThrow(() -> validator.assertValid(valueSet));
   }

   @Test
   void testValueSetIdMustBeDefined() {
      SupportedValueSetInput valueSet = (SupportedValueSetInput) new SupportedValueSetInput()
            .setId("set1")
            .setLabel("A value set");
      SupportedValueSetInputValidator validator = new SupportedValueSetInputValidator();
      assertInvalid(validator.validate(valueSet), "valueSetId must be set and not empty => invalid");
   }

   private static void assertInvalid(ObjectResult report, String expectedMessage) {
      assertThat(
            report.toString(),
            containsString(expectedMessage)
      );
      assertFalse(report.isValid());
   }

}
