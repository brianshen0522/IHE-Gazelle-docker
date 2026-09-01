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

package net.ihe.gazelle.maestro.spi.technical;

import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.spi.business.Handler;
import net.ihe.gazelle.maestro.spi.business.StepExecutor;

import java.util.Map;

/**
 * Factory interface responsible for creating {@link StepExecutor} instances to execute {@link Step} objects.
 */
public interface StepExecutorFactory {

   /**
    * Returns the type name of the step supported by this factory.
    *
    * @return the supported step type name
    */
   String getSupportedStep();

   /**
    * Returns the list of required services to run the given step.
    *
    * @param step the step for which required services are requested
    * @return a map containing service names as keys and the required {@link Handler} classes as values
    */
   Map<String, Class<? extends Handler>> getRequiredServices(Step step);

   /**
    * Creates a {@link StepExecutor} instance capable of executing the given step using the provided handlers.
    *
    * @param step the step to execute
    * @param handlers a map of service names to handler instances that can be used to run the step
    * @return a {@code StepExecutor} instance for executing the step
    * @throws IllegalArgumentException if the provided handlers are not suitable to run the step
    */
   StepExecutor createStepExecutor(Step step, Map<String, Handler> handlers);

}
