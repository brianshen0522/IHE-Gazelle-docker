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

package net.ihe.gazelle.maestro.quarkus.ws;

import net.ihe.gazelle.maestro.api.business.Maestro;
import net.ihe.gazelle.maestro.api.business.MaestroObserver;
import net.ihe.gazelle.maestro.api.business.message.ExecutionFinished;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRun;

import java.io.Serializable;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class MaestroMockForREST implements Maestro {

   private final ScheduledExecutorService scheduledExecutor;
   private Supplier<TestReport> testReportSupplier = null;
   private Supplier<RuntimeException> errorSupplier = null;

   public MaestroMockForREST(ScheduledExecutorService scheduledExecutor) {
      this.scheduledExecutor = scheduledExecutor;
   }

   public MaestroMockForREST setTestReportSupplier(Supplier<TestReport> testReportSupplier) {
      this.testReportSupplier = testReportSupplier;
      return this;
   }

   public MaestroMockForREST setErrorSupplier(Supplier<RuntimeException> errorSupplier) {
      this.errorSupplier = errorSupplier;
      return this;
   }

   @Override
   public void executeTestSuite(TestSuiteRun testSuiteRun, boolean persist, MaestroObserver observer) {
      if(errorSupplier != null) {
         throw errorSupplier.get();
      }
      scheduledExecutor.schedule(
            () -> pushExecutionFinished(testSuiteRun, persist, observer),
            1, java.util.concurrent.TimeUnit.SECONDS
      );
   }

   @Override
   public void executeTest(TestRun testRun, boolean persist, MaestroObserver observer) {
      if(errorSupplier != null) {
         throw errorSupplier.get();
      }
      scheduledExecutor.schedule(
            () -> pushExecutionFinished(testRun, persist, observer),
            1, java.util.concurrent.TimeUnit.SECONDS
      );
   }

   private void pushExecutionFinished(Serializable run, boolean persist, MaestroObserver observer) {
      observer.onExecutionFinished(
            new ExecutionFinished()
                  .setSessionId(UUID.randomUUID().toString())
                  .setRun(run)
                  .setReport(testReportSupplier.get())
                  .setReportLocation(persist ? "http://localhost/datahouse/items/" + getRandomId() : null)
      );
   }

   public String getRandomId() {
      byte[] bytes = new byte[8]; // 8 bytes = 16 hex characters
      ThreadLocalRandom.current().nextBytes(bytes);
      return HexFormat.of().formatHex(bytes);
   }

}
