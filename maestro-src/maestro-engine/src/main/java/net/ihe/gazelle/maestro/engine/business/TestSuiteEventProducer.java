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

package net.ihe.gazelle.maestro.engine.business;

import net.ihe.gazelle.maestro.api.business.message.StartTestRun;
import net.ihe.gazelle.maestro.api.business.message.TestSuiteRunFinished;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRun;

/**
 * Produces events related to the execution of {@link TestSuiteRun} instances,
 * including commands to start test runs and notifications when a test suite run has finished.
 */
public interface TestSuiteEventProducer {

   /**
    * Sends a command to start a {@link TestRun} within a test suite run.
    *
    * @param startTestRun the event representing the start of a test run
    */
   void command(StartTestRun startTestRun);

   /**
    * Notifies that a {@link TestSuiteRun} has finished execution.
    *
    * @param testSuiteRunFinished the finished test suite run event to notify
    */
   void notify(TestSuiteRunFinished testSuiteRunFinished);

}
