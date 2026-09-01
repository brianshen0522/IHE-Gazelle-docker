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

package net.ihe.gazelle.simulation.business.model;

import net.ihe.gazelle.simulation.business.sequence.SimulationSequence;

import java.util.Objects;

/**
 * An extension of {@link SimulationSequence} that enriches the sequence definition
 * with additional simulator metadata and validation status.
 * <p>
 * This class is typically used in contexts where both the sequence and
 * the simulator implementation details are required, such as reporting,
 * UI presentation, or service responses.
 */
public class SimulationSequenceExtended extends SimulationSequence {

   private String simulatorName;
   private String simulatorVersion;
   private String simulatorUrl;
   private boolean valid;
   private String validReportMessage;

   /**
    * Creates an empty {@code SimulationSequenceExtended} instance.
    * <p>
    * Intended primarily for frameworks and DTO deserialization.
    */
   public SimulationSequenceExtended() {
      // For DTO
   }

   /**
    * Creates an extended simulation sequence by copying values from
    * an existing {@link SimulationSequence}.
    * <p>
    * All base fields from the given sequence are initialized, and the
    * {@code valid} flag is set to {@code true}.
    *
    * @param simulationSequence the sequence to copy from, must not be {@code null}
    */
   public SimulationSequenceExtended(SimulationSequence simulationSequence) {
      super.setId(simulationSequence.getId())
            .setVersion(simulationSequence.getVersion())
            .setTransactions(simulationSequence.getTransactions())
            .setShortDescription(simulationSequence.getShortDescription())
            .setDescription(simulationSequence.getDescription())
            .setRunnable(simulationSequence.isRunnable())
            .setStandards(simulationSequence.getStandards())
            .setSimulatedRoles(simulationSequence.getSimulatedRoles())
            .setTestedRoles(simulationSequence.getTestedRoles())
            .setSupportedParameters(simulationSequence.getSupportedParameters());
      valid = true;
   }

   /**
    * Retrieves the name of the simulator.
    *
    * @return the name of the simulator
    */
   public String getSimulatorName() {
      return simulatorName;
   }

   /**
    * Sets the name of the simulator.
    *
    * @param simulatorName the name of the simulator to be set
    * @return this instance for method chaining
    */
   public SimulationSequenceExtended setSimulatorName(String simulatorName) {
      this.simulatorName = simulatorName;
      return this;
   }

   /**
    * Retrieves the version of the simulator.
    *
    * @return the version of the simulator
    */
   public String getSimulatorVersion() {
      return simulatorVersion;
   }

   /**
    * Sets the version of the simulator for this simulation sequence.
    *
    * @param simulatorVersion the version of the simulator to be set
    * @return this instance for method chaining
    */
   public SimulationSequenceExtended setSimulatorVersion(String simulatorVersion) {
      this.simulatorVersion = simulatorVersion;
      return this;
   }

   /**
    * Retrieves the URL of the simulator service or resource.
    *
    * @return the URL of the simulator service or resource
    */
   public String getSimulatorUrl() {
      return simulatorUrl;
   }

   /**
    * Sets the URL of the simulator service or resource.
    *
    * @param simulatorUrl the URL of the simulator to be set
    * @return this instance for method chaining
    */
   public SimulationSequenceExtended setSimulatorUrl(String simulatorUrl) {
      this.simulatorUrl = simulatorUrl;
      return this;
   }

   /**
    * Determines whether the simulation sequence is considered valid.
    *
    * @return {@code true} if the simulation sequence is valid; {@code false} otherwise
    */
   public boolean isValid() {
      return valid;
   }

   /**
    * Sets the validity flag of this simulation sequence.
    *
    * @param valid the validity status to set; {@code true} for valid, {@code false} for invalid
    * @return this instance for method chaining
    */
   public SimulationSequenceExtended setValid(boolean valid) {
      this.valid = valid;
      return this;
   }

   /**
    * Retrieves the validation report message associated with this simulation sequence.
    *
    * @return the validation report message, or null if no message is set
    */
   public String getValidReportMessage() {
      return validReportMessage;
   }

   /**
    * Sets the validation report message associated with this simulation sequence.
    *
    * @param validReportMessage the validation report message to be set
    * @return this instance for method chaining
    */
   public SimulationSequenceExtended setValidReportMessage(String validReportMessage) {
      this.validReportMessage = validReportMessage;
      return this;
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof SimulationSequenceExtended that)) return false;
      if (!super.equals(o)) return false;
      return valid == that.valid && Objects.equals(simulatorName, that.simulatorName)
            && Objects.equals(simulatorVersion, that.simulatorVersion)
            && Objects.equals(simulatorUrl, that.simulatorUrl)
            && Objects.equals(validReportMessage, that.validReportMessage);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), simulatorName, simulatorVersion, simulatorUrl, valid, validReportMessage);
   }
}
