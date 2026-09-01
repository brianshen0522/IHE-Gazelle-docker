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

import net.ihe.gazelle.maestro.api.business.message.*;

import java.util.concurrent.ExecutorService;

public class HardWiredEventBroker implements MaestroEventProducer, TestSuiteEventProducer, TestRunEventProducer, StepRunEventProducer {

   private final ExecutorService executorService;

   private MaestroFacade maestro;
   private TestSuiteRunner testSuiteRunner;
   private TestRunner testRunner;
   private StepRunner stepRunner;

   public HardWiredEventBroker(ExecutorService executorService) {
      this.executorService = executorService;
   }

   public void setServices(MaestroFacade maestro, TestSuiteRunner testSuiteRunner, TestRunner testRunner, StepRunner stepRunner) {
      this.maestro = maestro;
      this.testSuiteRunner = testSuiteRunner;
      this.testRunner = testRunner;
      this.stepRunner = stepRunner;
   }

   @Override
   public void command(StartTestSuiteRun startTestSuiteRun) {
      executorService.submit(() -> testSuiteRunner.run(startTestSuiteRun));
   }

    @Override
   public void notify(StepRunFinished stepRunFinished) {
      executorService.submit(() -> testRunner.listen(stepRunFinished));
      executorService.submit(() -> maestro.listen(stepRunFinished));
   }

   @Override
   public void command(StartStepRun startStepRun) {
      executorService.submit(() -> stepRunner.run(startStepRun));
      executorService.submit(() -> maestro.listen(startStepRun));
   }

   @Override
   public void notify(TestRunFinished testRunFinished) {
      executorService.submit(() -> testSuiteRunner.listen(testRunFinished));
      executorService.submit(() -> maestro.listen(testRunFinished));
   }

   @Override
   public void command(StartTestRun startTestRun) {
      executorService.submit(() -> testRunner.run(startTestRun));
      executorService.submit(() -> maestro.listen(startTestRun));
   }

   @Override
   public void notify(TestSuiteRunFinished testSuiteRunFinished) {
      executorService.submit(() -> maestro.listen(testSuiteRunFinished));
   }
}
