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

package net.ihe.gazelle.maestro.api.business.test;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A structure contains in TestRun defining TestRun's steps.
 * <p>
 * - <b>id</b> TestRun id - <b>name</b> a string of test name - <b>steps</b> a list of step
 * </p>
 * {@link Step}
 */
public class Test implements Serializable {

   @Serial
   private static final long serialVersionUID = -7394123287397209441L;

   /**
    * For each step in the test, a margin is added to the timeout of the step to prevent computation time.
    */
   private static final int TIMEOUT_MARGIN_OFFSET = 3000;

   /**
    * The id of the test.
    */
   private String id;

   /**
    * The name of the test.
    */
   private String name;

   /**
    * The steps of the test.
    */
   private List<Step> steps = new ArrayList<>();

   /**
    * The list of supported inputs.
    */
   private List<SupportedInput> supportedInputs = new ArrayList<>();

   /**
    * Default constructor
    */
   public Test() {
      // Empty
   }

   /**
    * Retrieves the identifier associated with this instance.
    *
    * @return the identifier of this instance as a string
    */
   public String getId() {
      return id;
   }

   /**
    * Sets the identifier for this instance.
    *
    * @param id the identifier to be set for this instance
    * @return the current instance of the Test class
    */
   public Test setId(String id) {
      this.id = id;
      return this;
   }

   /**
    * Retrieves the name associated with this instance.
    *
    * @return the name of this instance as a string
    */
   public String getName() {
      return name;
   }

   /**
    * Sets the name for this instance.
    *
    * @param name the name to be set for this instance
    * @return the current instance of the Test class
    */
   public Test setName(String name) {
      this.name = name;
      return this;
   }

   /**
    * Retrieves the list of steps associated with this instance.
    *
    * @return a list of steps represented as {@code List<Step>}. The returned list is a copy
    * of the internal list to ensure immutability.
    */
   public List<Step> getSteps() {
      return new ArrayList<>(steps);
   }

   /**
    * Sets the list of steps for this instance.
    *
    * @param steps the list of steps to be set for this instance. This list represents
    *              {@code List<Step>} objects.
    * @return the current instance of the {@code Test} class for method chaining.
    */
   public Test setSteps(List<Step> steps) {
      this.steps = steps != null ? new ArrayList<>(steps) : new ArrayList<>();
      return this;
   }

   /**
    * Adds a step to the list of steps associated with this instance.
    *
    * @param step the step to be added, represented as an instance of the {@code Step} class
    * @return the current instance of the {@code Test} class
    */
   public Test addStep(Step step) {
      steps.add(step);
      return this;
   }

   /**
    * Retrieves the list of supported inputs for this instance.
    *
    * @return a new {@code List<SupportedInput>} containing the supported inputs
    */
   public List<SupportedInput> getSupportedInputs() {
      return new ArrayList<>(supportedInputs);
   }

   /**
    * Sets the list of supported inputs for this instance.
    *
    * @param supportedInputs the list of supported inputs to be set for this instance,
    *                        represented as {@code List<SupportedInput>}
    * @return the current instance of the {@code Test} class for method chaining
    */
   public Test setSupportedInputs(List<SupportedInput> supportedInputs) {
      this.supportedInputs = supportedInputs != null
            ? new ArrayList<>(supportedInputs)
            : new ArrayList<>();
      return this;
   }

   /**
    * Adds a supported input to the list of supported inputs for the current instance.
    *
    * @param supportedInput the supported input to be added, represented as an instance of {@code SupportedInput}
    * @return the current instance of the {@code Test} class for method chaining
    */
   public Test addSupportedInput(SupportedInput supportedInput) {
      this.supportedInputs.add(supportedInput);
      return this;
   }

   /**
    * Determines whether the identifier for this instance is defined and not blank.
    *
    * @return true if the identifier is neither null nor blank, false otherwise
    */
   public boolean isIdDefined() {
      return id != null && !id.isBlank();
   }

   /**
    * Determines whether the name for this instance is defined and not blank.
    *
    * @return true if the name is neither null nor blank, false otherwise
    */
   public boolean isNameDefined() {
      return name != null && !name.isBlank();
   }

   /**
    * Checks whether there is at least one step associated with this instance.
    *
    * @return true if the steps list is not null and contains at least one element, false otherwise
    */
   public boolean atLeastOneStep() {
      return steps != null && !steps.isEmpty();
   }

   /**
    * Determines whether all steps associated with this instance have unique names.
    *
    * @return true if all step names are unique, false if there is any duplicate step name
    */
   public boolean areStepsUnique() {
      List<String> names = new ArrayList<>();
      for (Step step : steps) {
         if (!names.contains(step.getName())) {
            names.add(step.getName());
         } else {
            return false;
         }
      }
      return true;
   }

   /**
    * Computes the total timeout value for a test run.
    *
    * @return the computed timeout value for the test run as a long value in milliseconds
    */
   public long computeTestRunTimeout() {
      return this.getSteps().stream()
            .mapToLong(Step::getTimeout)
            .sum() + TIMEOUT_MARGIN_OFFSET;
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof Test test)) {
         return false;
      }
      return Objects.equals(id, test.id)
            && Objects.equals(name, test.name)
            && Objects.equals(steps, test.steps)
            && Objects.equals(supportedInputs, test.supportedInputs);
   }

   @Override
   public int hashCode() {
      return Objects.hash(id, name, steps, supportedInputs);
   }
}

