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

package net.ihe.gazelle.maestro.validation.step.technical.factory;

import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.spi.business.Handler;
import net.ihe.gazelle.maestro.spi.business.StepExecutor;
import net.ihe.gazelle.maestro.spi.technical.StepExecutorFactory;
import net.ihe.gazelle.maestro.validation.step.business.ValidationHandler;
import net.ihe.gazelle.maestro.validation.step.business.ValidationStepDefinition;
import net.ihe.gazelle.maestro.validation.step.business.ValidationStepExecutor;
import net.ihe.gazelle.maestro.validation.step.technical.dto.ReportSerializerImpl;

import java.util.Map;
import java.util.Optional;

/**
 * Factory for creating ValidationStepExecutor instances.
 * This factory creates executors for the validation step type.
 */
public class ValidationStepExecutorFactory implements StepExecutorFactory {

   /**
    * Default constructor.
    */
   public ValidationStepExecutorFactory() { /* Default constructor */ }

   @Override
   public String getSupportedStep() {
      return ValidationStepDefinition.TYPE;
   }

   @Override
   public Map<String, Class<? extends Handler>> getRequiredServices(Step step) {
      return Map.of(
            step.getPropertyValue(ValidationStepDefinition.VALIDATION_SERVICE), ValidationHandler.class
      );
   }

   @Override
   public StepExecutor createStepExecutor(Step step, Map<String, Handler> handlers) {
      String serviceName = step.getPropertyValue(ValidationStepDefinition.VALIDATION_SERVICE);
      return new ValidationStepExecutor(
            Optional.ofNullable((ValidationHandler) handlers.get(serviceName))
                  .orElseThrow(() -> new IllegalArgumentException("No validation handler provided for " + serviceName)),
            new ReportSerializerImpl()
      );
   }

}
