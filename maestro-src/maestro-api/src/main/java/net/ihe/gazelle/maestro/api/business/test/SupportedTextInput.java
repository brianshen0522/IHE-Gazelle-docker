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

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * A text input.
 */
public class SupportedTextInput extends SupportedInput {

   @Serial
   private static final long serialVersionUID = -4705877909585949476L;

   /**
    * The possible values.
    */
   private List<String> possibleValues = new ArrayList<>();

   /**
    * The default value.
    */
   private String defaultValue;

   /**
    * Default constructor
    */
   public SupportedTextInput() {
      // default constructor
   }

   /**
    * Retrieves a list of possible values for this input.
    *
    * @return a new list containing all possible values, or an empty list if none are defined.
    */
   public List<String> getPossibleValues() {
      return new ArrayList<>(possibleValues);
   }

   /**
    * Sets the possible values for this text input. If the provided list is null,
    * an empty list will be assigned.
    *
    * @param possibleValues a list of possible values to be set for the input.
    *                       If null, an empty list will be used.
    * @return the current instance of {@code SupportedTextInput} to allow method chaining.
    */
   public SupportedTextInput setPossibleValues(List<String> possibleValues) {
      this.possibleValues = possibleValues != null
              ? new ArrayList<>(possibleValues)
              : new ArrayList<>();
      return this;
   }

   /**
    * Retrieves the default value for this text input.
    *
    * @return the default value as a string, or null if no default value is set.
    */
   public String getDefaultValue() {
      return defaultValue;
   }

   /**
    * Sets the default value for this text input.
    *
    * @param defaultValue the default value to be set for the input
    * @return the current instance of {@code SupportedTextInput} to allow method chaining
    */
   public SupportedTextInput setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
      return this;
   }

   /**
    * Sets the unique identifier for this text input.
    *
    * @param id the unique identifier of the input as a String
    * @return the current instance of {@code SupportedTextInput} to allow method chaining
    */
   @Override
   public SupportedTextInput setId(String id) {
      super.setId(id);
      return this;
   }

   /**
    * Sets the label for this text input.
    *
    * @param label the label of the input as a String
    * @return the current instance of {@code SupportedTextInput} to allow method chaining
    */
   @Override
   public SupportedTextInput setLabel(String label) {
      super.setLabel(label);
      return this;
   }

   /**
    * Sets whether this text input is required.
    *
    * @param required a boolean indicating if the input should be marked as required
    * @return the current instance of {@code SupportedTextInput} to allow method chaining
    */
   @Override
   public SupportedTextInput setRequired(boolean required) {
      super.setRequired(required);
      return this;
   }

   /**
    * Checks if all possible values in the list are defined. A value is considered defined
    * if it is neither null nor empty.
    *
    * @return {@code true} if all possible values are defined, or if the list of possible values
    *         is empty; {@code false} otherwise.
    */
   public boolean areAllPossibleValuesDefined() {
      for(String possibleValue : possibleValues) {
         if (possibleValue == null || possibleValue.isEmpty()) {
            return false;
         }
      }
      return true;
   }
}
