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
import net.ihe.gazelle.maestro.api.business.message.*;
import net.ihe.gazelle.maestro.engine.business.TestRunEventProducer;
import net.ihe.gazelle.maestro.engine.business.TestRunner;

/**
 * This class is the event broker that listen to all kind of messages used in Maestro application It will trigger
 * application code depending on which event is fired.
 */
@RequestScoped
public class TestRunnerEventController implements TestRunEventProducer {

   private final TestRunner testRunner;
   private final Event<Message> messageEvent;

   /**
    * Creates a new {@code TestRunnerEventController} with the specified {@link TestRunner} and event dispatcher.
    *
    * @param testRunner the {@link TestRunner} used to execute test runs
    * @param messageEvent the CDI event used to fire test events asynchronously
    */
   @Inject
   public TestRunnerEventController(TestRunner testRunner, Event<Message> messageEvent) {
      this.testRunner = testRunner;
      this.messageEvent = messageEvent;
   }

   /**
    * Handles asynchronously a {@link StartTestRun} event by delegating execution to {@link TestRunner}.
    *
    * @param startTestRun the start test run event
    */
   public void onStartTestRun(@ObservesAsync StartTestRun startTestRun) {
      testRunner.run(startTestRun);
   }

   /**
    * Handles asynchronously a {@link StepRunFinished} event by delegating to {@link TestRunner} for processing.
    *
    * @param stepRunFinished the step run finished event
    */
   public void onStepRunFinished(@ObservesAsync StepRunFinished stepRunFinished) {
      testRunner.listen(stepRunFinished);
   }

   @Override
   public void command(StartStepRun startStepRun) {
      messageEvent.fireAsync(startStepRun);
   }

   @Override
   public void notify(TestRunFinished testRunFinished) {
      messageEvent.fireAsync(testRunFinished);
   }

}
