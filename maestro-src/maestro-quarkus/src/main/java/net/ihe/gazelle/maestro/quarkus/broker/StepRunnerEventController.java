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

package net.ihe.gazelle.maestro.quarkus.broker;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import net.ihe.gazelle.maestro.api.business.message.Message;
import net.ihe.gazelle.maestro.api.business.message.StartStepRun;
import net.ihe.gazelle.maestro.api.business.message.StepRunFinished;
import net.ihe.gazelle.maestro.engine.business.StepRunEventProducer;
import net.ihe.gazelle.maestro.engine.business.StepRunner;

/**
 * StepRunnerEventController is responsible for producing and handling step-related events.
 * It delegates the execution of steps to {@link StepRunner} and fires {@link StepRunFinished}
 * events asynchronously.
 */
@RequestScoped
public class StepRunnerEventController implements StepRunEventProducer {

   private final StepRunner stepRunner;
   private final Event<Message> messageEvent;

   /**
    * Creates a new {@code StepRunnerEventController} with the specified {@link StepRunner} and event dispatcher.
    *
    * @param stepRunner   the {@link StepRunner} used to execute steps
    * @param messageEvent the CDI event used to fire step events asynchronously
    */
   @Inject
   public StepRunnerEventController(StepRunner stepRunner, Event<Message> messageEvent) {
      this.stepRunner = stepRunner;
      this.messageEvent = messageEvent;
   }

   /**
    * Handles asynchronously a {@link StartStepRun} event by delegating execution to {@link StepRunner}.
    *
    * @param startStepRun the start step run event
    */
   public void onStartStepRun(@ObservesAsync StartStepRun startStepRun) {
      stepRunner.run(startStepRun);
   }

   @Override
   public void notify(StepRunFinished stepRunFinished) {
      messageEvent.fireAsync(stepRunFinished);
   }
}
