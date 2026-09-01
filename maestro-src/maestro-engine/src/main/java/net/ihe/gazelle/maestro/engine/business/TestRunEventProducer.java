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

import net.ihe.gazelle.maestro.api.business.message.StartStepRun;
import net.ihe.gazelle.maestro.api.business.message.TestRunFinished;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;

/**
 * Produces events related to the execution of {@link TestRun} instances.
 */
public interface TestRunEventProducer {

   /**
    * Sends a command to start a {@link Step} execution within a test run.
    *
    * @param startStepRun the event representing the start of a step run
    */
   void command(StartStepRun startStepRun);

   /**
    * Notifies that a {@link TestRun} has finished execution.
    *
    * @param testRunFinished the finished test run event to notify
    */
   void notify(TestRunFinished testRunFinished);

}
