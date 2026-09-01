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
import net.ihe.gazelle.maestro.engine.business.TestSuiteEventProducer;
import net.ihe.gazelle.maestro.engine.business.TestSuiteRunner;

/**
 * TestSuiteRunnerEventController is responsible for producing and handling test suite events.
 * It delegates the execution of test suites to {@link TestSuiteRunner} and listens for test run completion events.
 */
@RequestScoped
public class TestSuiteRunnerEventController implements TestSuiteEventProducer {

   private final TestSuiteRunner testSuiteRunner;
   private final Event<Message> messageEvent;

   /**
    * Creates a new {@code TestSuiteRunnerEventController} with the specified {@link TestSuiteRunner} and event dispatcher.
    *
    * @param testSuiteRunner the {@link TestSuiteRunner} used to execute test suites
    * @param messageEvent the CDI event used to fire test suite events asynchronously
    */
   @Inject
   public TestSuiteRunnerEventController(TestSuiteRunner testSuiteRunner, Event<Message> messageEvent) {
      this.testSuiteRunner = testSuiteRunner;
      this.messageEvent = messageEvent;
   }

   /**
    * Handles asynchronously a {@link StartTestSuiteRun} event by delegating execution to {@link TestSuiteRunner}.
    *
    * @param startTestSuiteRun the start test suite run event
    */
   public void onStartTestSuiteRun(@ObservesAsync StartTestSuiteRun startTestSuiteRun) {
      testSuiteRunner.run(startTestSuiteRun);
   }

   /**
    * Handles asynchronously a {@link TestRunFinished} event by delegating to {@link TestSuiteRunner} for processing.
    *
    * @param testRunFinished the test run finished event
    */
   public void onTestRunFinished(@ObservesAsync TestRunFinished testRunFinished) {
      testSuiteRunner.listen(testRunFinished);
   }

   @Override
   public void command(StartTestRun startTestRun) {
      messageEvent.fireAsync(startTestRun);
   }

   @Override
   public void notify(TestSuiteRunFinished testSuiteRunFinished) {
      messageEvent.fireAsync(testSuiteRunFinished);
   }

}
