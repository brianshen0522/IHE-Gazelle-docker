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

package net.ihe.gazelle.maestro.engine.business.mock;

import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.engine.business.TestReportRecordingException;
import net.ihe.gazelle.maestro.engine.business.TestReportRecordingService;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class InMemoryTestReportRecordingService implements TestReportRecordingService {

   private final List<TestReport> recordedReports = new CopyOnWriteArrayList<>();
   private final AtomicReference<TestReportRecordingException> nextFailure = new AtomicReference<>();

    @Override
    public String recordTestReport(TestReport report) {
        return recordTestReportWithReferences(report);
    }

   private synchronized String recordTestReportWithReferences(TestReport report) {
      TestReportRecordingException failure = nextFailure.getAndSet(null);
      if (failure != null) {
         throw failure;
      }
      recordedReports.add(report);
      return "report-" + recordedReports.size();
   }

   public List<TestReport> getRecordedReports() {
      return new CopyOnWriteArrayList<>(recordedReports);
   }

   public void failNextWith(TestReportRecordingException exception) {
        nextFailure.set(exception);
    }

   public void clear() {
        recordedReports.clear();
    }
}
