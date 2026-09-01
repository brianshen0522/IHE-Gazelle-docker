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

package net.ihe.gazelle.maestro.quarkus.factory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import net.ihe.gazelle.maestro.engine.business.StepExecutorProvider;
import net.ihe.gazelle.maestro.spi.technical.HandlerProvider;
import technical.provider.StepExecutorSPIProvider;

/**
 * Factory class responsible for producing service instances required by the Maestro engine,
 * specifically {@link StepExecutorProvider}.
 */
@ApplicationScoped
public class ServicesFactory {

   private final HandlerProvider handlerProvider;

   /**
    * Constructs a {@code ServicesFactory} with the provided {@link HandlerProvider}.
    *
    * @param handlerProvider the {@link HandlerProvider} used to create {@link StepExecutorProvider} instances
    */
   @Inject
   public ServicesFactory(HandlerProvider handlerProvider) {
      this.handlerProvider = handlerProvider;
   }

   /**
    * Produces a {@link StepExecutorProvider} instance configured with the injected {@link HandlerProvider}.
    *
    * @return a new {@link StepExecutorSPIProvider} instance
    */
   @Produces
   @Default
   public StepExecutorProvider getStepRunnerProvider() {
      return new StepExecutorSPIProvider(handlerProvider);
   }
}
