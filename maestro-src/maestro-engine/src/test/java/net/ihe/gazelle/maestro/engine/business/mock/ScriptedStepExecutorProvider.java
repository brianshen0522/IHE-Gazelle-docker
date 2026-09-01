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

package net.ihe.gazelle.maestro.engine.business.mock;

import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.engine.business.StepExecutorProvider;
import net.ihe.gazelle.maestro.engine.business.UnknownStepException;
import net.ihe.gazelle.maestro.spi.business.StepExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScriptedStepExecutorProvider implements StepExecutorProvider {

   private final Map<String, StepExecutor> executorsByStepName = new ConcurrentHashMap<>();

   @Override
   public StepExecutor getExecutor(String sessionId, Step step) {
      StepExecutor executor = executorsByStepName.get(step.getName());
      if (executor == null) {
         throw new UnknownStepException("No executor registered for step " + step.getName());
      }
      return executor;
   }

   public ScriptedStepExecutorProvider returnsReport(String stepName, StepRunReport stepRunReport) {
      executorsByStepName.put(stepName, stepRun -> stepRunReport);
      return this;
   }

   public ScriptedStepExecutorProvider withExecutor(String stepName, StepExecutor executor) {
      executorsByStepName.put(stepName, executor);
      return this;
   }

   public ScriptedStepExecutorProvider failsWith(String stepName, RuntimeException exception) {
      executorsByStepName.put(stepName, stepRun -> {
         throw exception;
      });
      return this;
   }
}
