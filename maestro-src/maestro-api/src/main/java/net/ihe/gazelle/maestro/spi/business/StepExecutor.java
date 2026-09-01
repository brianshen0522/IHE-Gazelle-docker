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

package net.ihe.gazelle.maestro.spi.business;

import net.ihe.gazelle.maestro.api.business.property.MissingPropertyException;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;

/**
 * Interface to perform the execution of a step
 */
public interface StepExecutor {

   /**
    * The method that trigger step execution
    * @param stepRun contain a step to run
    *
    * @return the report with information to build a step report
    *
    * @throws MissingPropertyException if a required property is missing to execute the step
    * @throws ClassCastException if a property has not the expected type
    */
   StepRunReport execute(StepRun stepRun);

}
