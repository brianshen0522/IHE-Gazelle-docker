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

package net.ihe.gazelle.maestro.simulation.step.business;

import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.test.SupportedInput;
import net.ihe.gazelle.maestro.api.business.test.SupportedTextInput;
import net.ihe.gazelle.maestro.spi.business.StepDefinition;

import java.io.Serial;
import java.util.List;
import java.util.Map;

/**
 * Step definition for simulation operations.
 * This step executes a simulation sequence on a specified simulation service.
 */
public class SimulationStepDefinition implements StepDefinition {

   /**
    * The type identifier for the simulation step.
    */
   public static final String TYPE = "SIMULATION";

   /**
    * Input property key for the simulation service name.
    */
   public static final String SIMULATION_SERVICE = "simulationService";

   /**
    * Input property key for the simulation sequence identifier.
    */
   public static final String SEQUENCE_ID = "sequenceId";

   /**
    * Output property key for the simulation report.
    */
   public static final String REPORT = "report";

   @Serial
   private static final long serialVersionUID = -2808674613571801798L;

   /**
    * Default constructor.
    */
   public SimulationStepDefinition() { /* Default constructor */ }

   @Override
   public String getType() {
      return TYPE;
   }

   @Override
   public List<SupportedInput> getSupportedInputs() {
      return List.of(
            new SupportedTextInput().setId(SIMULATION_SERVICE).setLabel("Simulation service").setRequired(true),
            new SupportedTextInput().setId(SEQUENCE_ID).setLabel("Sequence ID").setRequired(true)
      );
   }

   @Override
   public Map<String, Class<? extends Property>> getOutputsDefinition() {
      return Map.of(
            REPORT, ByteArrayProperty.class
      );
   }
}
