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

import net.ihe.gazelle.maestro.api.business.testrun.TestRun;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Tracks iteration over the ordered list of {@link TestRun}.
 */
public final class TestRunCursor {

   private final List<TestRun> tests = new ArrayList<>();
   private int index = -1;

   /**
    * Creates a new {@code TestRunCursor} for the given ordered list of test runs.
    *
    * @param orderedTestRuns the ordered list of test runs to iterate
    */
   private TestRunCursor(List<TestRun> orderedTestRuns) {
      tests.addAll(orderedTestRuns);
   }

   /**
    * Creates a {@code TestRunCursor} from the given list of ordered test runs.
    *
    * @param orderedTestRuns the ordered list of test runs
    * @return a new {@code TestRunCursor} instance
    */
   public static TestRunCursor from(List<TestRun> orderedTestRuns) {
      return new TestRunCursor(orderedTestRuns);
   }

   /**
    * Indicates whether there is a next test run available.
    *
    * @return {@code true} if a next test run exists, {@code false} otherwise
    */
   public synchronized boolean hasNext() {
      return index < tests.size() - 1;
   }

   /**
    * Advances to the next test run and returns it.
    *
    * @return the next {@link TestRun}
    * @throws NoSuchElementException if there are no more test runs
    */
   public synchronized TestRun next() {
      if (!hasNext()) {
         throw new NoSuchElementException("No more test runs to execute");
      }
      return tests.get(++index);
   }
}
