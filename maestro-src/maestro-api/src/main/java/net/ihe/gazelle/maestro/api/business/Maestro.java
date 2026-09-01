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

package net.ihe.gazelle.maestro.api.business;

import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRun;

/**
 * Maestro execution engine. It is used to run automated tests.
 */
public interface Maestro {

   /**
    * Executes a test suite.
    *
    * @param testSuiteRun the test suite to execute
    * @param persist      whether to persist the results
    * @param observer     the observer that will receive execution updates
    */
   void executeTestSuite(TestSuiteRun testSuiteRun, boolean persist, MaestroObserver observer);

   /**
    * Executes a single test.
    *
    * @param testRun  the test to execute
    * @param persist  whether to persist the results
    * @param observer the observer that will receive execution updates
    */
   void executeTest(TestRun testRun, boolean persist, MaestroObserver observer);

}
