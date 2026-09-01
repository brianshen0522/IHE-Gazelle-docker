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

package net.ihe.gazelle.maestro.api.business.testreport;

import net.ihe.gazelle.errorhandling.business.UnexpectedError;
import net.ihe.gazelle.maestro.api.business.property.Property;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A structure for reporting the result of a step execution
 * mandatory fields : stepId, type, result
 */
public class StepRunReport implements Serializable {

   @Serial
   private static final long serialVersionUID = -5259327294299524545L;

   /**
    * The name of the step.
    */
   private String stepName;

   /**
    * The type of the step.
    */
   private String type;

   /**
    * The date and time at which the step was executed.
    */
   private Instant dateTime;

   /**
    * The result of the step execution.
    */
   private StepResult result;

   /**
    * The outputs of the step execution.
    */
   private List<Property> outputs;

   /**
    * The unexpected errors that occurred during the step execution.
    */
   private List<UnexpectedError> unexpectedErrors;

   /**
    * Default constructor.
    */
   public StepRunReport() {
      dateTime = Instant.now();
      result = StepResult.UNDEFINED;
      outputs = new ArrayList<>();
      unexpectedErrors = new ArrayList<>();
   }

   /**
    * Constructs a new instance of the StepRunReport class with the specified type and result.
    *
    * @param type   the type of the step or process being reported
    * @param result the result of the step or process execution
    */
   public StepRunReport(String type, StepResult result) {
      this();
      this.type = type;
      this.result = result;
   }

   /**
    * Retrieves the name of the step associated with this report.
    *
    * @return the name of the step, or null if the step name has not been defined
    */
   public String getStepName() {
      return stepName;
   }

   /**
    * Sets the name of the step associated with this report.
    *
    * @param stepName the name of the step to set
    * @return the current instance of StepRunReport
    */
   public StepRunReport setStepName(String stepName) {
      this.stepName = stepName;
      return this;
   }

   /**
    * Retrieves the type associated with the current instance.
    *
    * @return the type of the step or process being reported
    */
   public String getType() {
      return type;
   }

   /**
    * Sets the type associated with this instance.
    *
    * @param type the type of the step or process being reported
    * @return the current instance of StepRunReport
    */
   public StepRunReport setType(String type) {
      this.type = type;
      return this;
   }

   /**
    * Retrieves the timestamp associated with this step run report.
    *
    * @return the timestamp of the step or process execution as an {@link Instant},
    *         or null if the date and time have not been defined.
    */
   public Instant getDateTime() {
      return dateTime;
   }

   /**
    * Sets the timestamp associated with this step run report.
    *
    * @param dateTime the timestamp of the step or process execution as an {@link Instant}
    * @return the current instance of StepRunReport
    */
   public StepRunReport setDateTime(Instant dateTime) {
      this.dateTime = dateTime;
      return this;
   }

   /**
    * Retrieves the result of the step or process execution associated with this instance.
    *
    * @return the result of the step or process execution as a {@link StepResult},
    *         or null if the result has not been defined.
    */
   public StepResult getResult() {
      return result;
   }

   /**
    * Sets the result of the step or process execution associated with this report.
    *
    * @param result the result of the step or process execution to set, represented as a {@link StepResult}
    * @return the current instance of {@code StepRunReport}
    */
   public StepRunReport setResult(StepResult result) {
      this.result = result;
      return this;
   }

   /**
    * Retrieves the list of output properties associated with this step run report.
    *
    * @return a list of {@code Property} instances representing the output properties,
    *         or an empty list if no outputs are defined
    */
   public List<Property> getOutputs() {
      return new ArrayList<>(outputs);
   }

   /**
    * Get a single output property by its name.
    * @param name the name of the output property to retrieve
    * @param <T> the expected type of the output property
    * @return the output property with the specified name, or null if no such property exists
    */
   @SuppressWarnings("unchecked")
   public <T extends Property> T getOutput(String name) {
      return (T) outputs.stream()
            .filter(property -> property.getName().equals(name))
            .findFirst()
            .orElse(null);
   }

   /**
    * Sets the list of output properties associated with this step run report.
    *
    * @param outputs the list of {@code Property} instances representing the output properties to set;
    *                cannot be null but may be empty
    * @return the current instance of {@code StepRunReport} with the updated output properties
    */
   public StepRunReport setOutputs(List<Property> outputs) {
      this.outputs = new ArrayList<>(outputs);
      return this;
   }

   /**
    * Adds an output property to the list of outputs for this step run report.
    *
    * @param output the {@code Property} instance to be added to the list of outputs; must not be null
    * @return the current instance of {@code StepRunReport} with the updated outputs
    */
   public StepRunReport addOutput(Property output) {
      outputs.add(output);
      return this;
   }

   /**
    * Adds a list of output properties to the current list of outputs associated with this step run report.
    *
    * @param outputs the list of {@code Property} instances to be added to the existing outputs;
    *                must not be null but can be an empty list
    * @return the current instance of {@code StepRunReport} with the updated list of outputs
    */
   public StepRunReport addOutputs(List<Property> outputs) {
      this.outputs.addAll(outputs);
      return this;
   }

   /**
    * Retrieves the list of unexpected errors associated with the current instance.
    *
    * @return a list of {@code UnexpectedError} instances representing the unexpected errors,
    *         or an empty list if no unexpected errors are defined.
    */
   public List<UnexpectedError> getUnexpectedErrors() {
      return new ArrayList<>(unexpectedErrors);
   }

   /**
    * Sets the list of unexpected errors associated with this step run report.
    *
    * @param unexpectedErrors the list of {@code UnexpectedError} instances representing
    *                         the unexpected errors to set; can be null
    * @return the current instance of {@code StepRunReport} with the updated list of unexpected errors
    */
   public StepRunReport setUnexpectedErrors(List<UnexpectedError> unexpectedErrors) {
      this.unexpectedErrors = unexpectedErrors != null
            ? new ArrayList<>(unexpectedErrors)
            : new ArrayList<>();
      return this;
   }

   /**
    * Adds an unexpected error to the list of unexpected errors associated with this instance.
    *
    * @param unexpectedError the {@code UnexpectedError} instance to be added; must not be null
    * @return the current instance of {@code StepRunReport} with the updated list of unexpected errors
    */
   public StepRunReport addUnexpectedError(UnexpectedError unexpectedError) {
      unexpectedErrors.add(unexpectedError);
      return this;
   }

   /**
    * Determines whether the step name associated with this report has been defined
    * and is not blank.
    *
    * @return {@code true} if the step name is defined and not blank; {@code false} otherwise
    */
   public boolean isStepNameDefined() {
      return stepName != null && !stepName.isBlank();
   }

   /**
    * Determines whether the type associated with this report has been defined
    * and is not blank.
    *
    * @return true if the type is defined and not blank; false otherwise
    */
   public boolean isTypeDefined() {
      return type != null && !type.isBlank();
   }

   /**
    * Determines whether the result of the step or process execution has been defined.
    *
    * @return true if the result is not null; false otherwise
    */
   public boolean isResultDefined() {
      return result != null;
   }

   /**
    * Computes and returns the result of a step or process execution.
    * If there are any unexpected errors, the result is set to {@link StepResult#UNDEFINED}.
    *
    * @return the computed {@link StepResult}, which may reflect the current value
    *         of the result or {@link StepResult#UNDEFINED} if unexpected errors exist.
    */
   public StepResult computeResult() {
      if (!unexpectedErrors.isEmpty()) {
         result = StepResult.UNDEFINED;
      }
      return result;
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof StepRunReport report)) {
         return false;
      }
      return Objects.equals(stepName, report.stepName) && Objects.equals(type,
            report.type) && Objects.equals(dateTime,
            report.dateTime) && result == report.result && Objects.equals(outputs,
            report.outputs) && Objects.equals(unexpectedErrors, report.unexpectedErrors);
   }

   @Override
   public int hashCode() {
      return Objects.hash(stepName, type, dateTime, result, outputs, unexpectedErrors);
   }
}
