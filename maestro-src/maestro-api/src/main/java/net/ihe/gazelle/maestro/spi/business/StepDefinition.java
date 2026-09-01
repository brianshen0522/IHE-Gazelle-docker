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

import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.test.SupportedInput;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Defines a step in a test, including its type, supported inputs, and outputs.
 */
public interface StepDefinition extends Serializable {

   /**
    * Returns the type of the step.
    *
    * @return the step type
    */
   String getType();

   /**
    * Returns the list of inputs supported by this step.
    *
    * @return a list of supported inputs
    */
   List<SupportedInput> getSupportedInputs();

   /**
    * Returns the definition of outputs produced by this step.
    *
    * @return a map of output names to their corresponding property classes
    */
   Map<String, Class<? extends Property>> getOutputsDefinition();

}
