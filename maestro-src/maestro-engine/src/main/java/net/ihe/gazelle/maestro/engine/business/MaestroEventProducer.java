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

package net.ihe.gazelle.maestro.engine.business;

import net.ihe.gazelle.maestro.api.business.message.StartTestRun;
import net.ihe.gazelle.maestro.api.business.message.StartTestSuiteRun;

/**
 * Produces events to trigger the execution of test runs or test suite runs in Maestro.
 */
public interface MaestroEventProducer {

   /**
    * Sends a command to start a {@link StartTestSuiteRun}.
    *
    * @param startTestSuiteRun the event representing the start of a test suite run
    */
   void command(final StartTestSuiteRun startTestSuiteRun);

   /**
    * Sends a command to start a {@link StartTestRun}.
    *
    * @param startTestRun the event representing the start of a test run
    */
   void command(final StartTestRun startTestRun);

}
