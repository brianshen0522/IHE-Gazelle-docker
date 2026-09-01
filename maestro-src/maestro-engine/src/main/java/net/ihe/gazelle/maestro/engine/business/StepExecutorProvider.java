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

package net.ihe.gazelle.maestro.engine.business;

import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.spi.business.StepExecutor;

/**
 * SPI for Step executor interface
 */
public interface StepExecutorProvider {

   /**
    * Get the StepExecutor able to execute the given step
    *
    * @param sessionId The internal session id
    * @param step The step we want StepExecutor to execute
    *
    * @return A StepExecutor able to run step
    *
    * @throws UnknownStepException if no StepExecutor is found for the given step
    */
   StepExecutor getExecutor(String sessionId, Step step);

}
