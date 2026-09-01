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

package net.ihe.gazelle.maestro.itb.step.technical.factory;

import net.ihe.gazelle.itb.gateway.technical.dao.ItbSessionStoreImpl;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.itb.step.business.ItbHandler;
import net.ihe.gazelle.maestro.itb.step.business.ItbStepDefinition;
import net.ihe.gazelle.maestro.itb.step.business.ItbStepExecutor;
import net.ihe.gazelle.maestro.spi.business.Handler;
import net.ihe.gazelle.maestro.spi.business.StepExecutor;
import net.ihe.gazelle.maestro.spi.technical.StepExecutorFactory;

import java.util.Map;
import java.util.Optional;

/**
 * Factory class for creating step executors that handle ITB (Integration Test Bed) steps.
 */
public class ItbStepExecutorFactory implements StepExecutorFactory {

   /**
    * Default constructor.
    */
   public ItbStepExecutorFactory() {
      // Empty
   }

   @Override
   public String getSupportedStep() {
      return ItbStepDefinition.TYPE;
   }

   @Override
   public Map<String, Class<? extends Handler>> getRequiredServices(Step step) {
      return Map.of(
            ItbHandler.ITB_SERVICE_NAME, ItbHandler.class
      );
   }

   @Override
   public StepExecutor createStepExecutor(Step step, Map<String, Handler> handlers) {
      return new ItbStepExecutor(
            Optional.ofNullable((ItbHandler) handlers.get(ItbHandler.ITB_SERVICE_NAME))
                  .orElseThrow(() -> new IllegalArgumentException("No ITB handler provided")),
            new ItbSessionStoreImpl()
      );
   }

}
