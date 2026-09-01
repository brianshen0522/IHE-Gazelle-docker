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

package net.ihe.gazelle.maestro.engine.business.context;

import net.ihe.gazelle.maestro.api.business.testreport.TestRunReport;
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRun;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * TestSuiteSession is a structure used to store report builder from executed TestRun, testSet's id and reference to
 * this testSet.
 */
public class TestSuiteSession {

   private final TestSuiteRun testSuiteRun;
   private final TestRunCursor testRunCursor;
   private final List<TestRunReport> testRunReports;

   /**
    * Creates a new {@code TestSuiteSession} for the specified {@link TestSuiteRun} and {@link TestRunCursor}.
    *
    * @param testSuiteRun the test suite run associated with this session
    * @param testRunCursor the cursor for iterating over the test runs in this suite
    */
   public TestSuiteSession(TestSuiteRun testSuiteRun, TestRunCursor testRunCursor) {
      this.testSuiteRun = testSuiteRun;
      this.testRunCursor = testRunCursor;
      this.testRunReports = new ArrayList<>();
   }

   /**
    * Retrieves the {@link TestSuiteRun} associated with this session.
    *
    * @return the test suite run
    */
   public TestSuiteRun getTestSuiteRun() {
      return testSuiteRun;
   }

   /**
    * Returns a copy of the list of executed test run reports.
    *
    * @return a list of {@link TestRunReport} instances
    */
   public List<TestRunReport> getTestRunReports() {
      return new ArrayList<>(testRunReports);
   }

   /**
    * Adds a {@link TestRunReport} to the list of executed test run reports.
    *
    * @param report the test run report to add
    */
   public void addTestRunReport(TestRunReport report) {
      testRunReports.add(report);
   }

   /**
    * Returns the {@link TestRunCursor} for iterating over the test runs in this suite.
    *
    * @return the test run cursor
    */
   public TestRunCursor getTestRunCursor() {
      return testRunCursor;
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof TestSuiteSession store)) {
         return false;
      }
      return Objects.equals(testSuiteRun, store.testSuiteRun)
            && Objects.equals(testRunReports, store.testRunReports)
            && Objects.equals(testRunCursor, store.testRunCursor);
   }

   @Override
   public int hashCode() {
      return Objects.hash(testSuiteRun, testRunReports, testRunCursor);
   }
}

