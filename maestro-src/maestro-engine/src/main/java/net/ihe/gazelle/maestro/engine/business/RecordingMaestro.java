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

import net.ihe.gazelle.errorhandling.business.UnexpectedErrorBuilder;
import net.ihe.gazelle.maestro.api.business.Maestro;
import net.ihe.gazelle.maestro.api.business.MaestroObserver;
import net.ihe.gazelle.maestro.api.business.message.*;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRun;

import java.util.concurrent.CompletableFuture;

/**
 * Maestro implementation that records test execution reports when {@code persist} is enabled.
 * Delegates execution to a {@link MaestroFacade} and wraps observers for recording.
 */
public class RecordingMaestro implements Maestro {

   private final MaestroFacade maestro;
   private final TestReportRecordingService testReportRecordingService;

   /**
    * Creates a new {@code RecordingMaestro} instance with the specified facade and recording service.
    *
    * @param maestro                    the {@link MaestroFacade} used for executing tests
    * @param testReportRecordingService the service used to record test reports
    */
   public RecordingMaestro(MaestroFacade maestro, TestReportRecordingService testReportRecordingService) {
      this.maestro = maestro;
      this.testReportRecordingService = testReportRecordingService;
   }

   @Override
   public void executeTestSuite(TestSuiteRun testSuiteRun, boolean persist, MaestroObserver observer) {
      if (persist) {
         observer = new RecordObserver(observer);
      }
      maestro.executeTestSuite(testSuiteRun, observer);
   }

   @Override
   public void executeTest(TestRun testRun, boolean persist, MaestroObserver observer) {
      if (persist) {
         observer = new RecordObserver(observer);
      }
      maestro.executeTest(testRun, observer);
   }

   private class RecordObserver implements MaestroObserver {

      private final MaestroObserver delegate;

      public RecordObserver(MaestroObserver delegate) {
         this.delegate = delegate;
      }

      @Override
      public void onTestRunStarted(TestRunStarted testRunStarted) {
         delegate.onTestRunStarted(testRunStarted);
      }

      @Override
      public void onStepRunStarted(StepRunStarted stepRunStarted) {
         delegate.onStepRunStarted(stepRunStarted);
      }

      @Override
      public CompletableFuture<UserInteractionCompleted> interactWithUser(InteractWithUser interactWithUser) {
         return delegate.interactWithUser(interactWithUser);
      }

      @Override
      public void onStepRunFinished(StepRunFinished stepRunFinished) {
         delegate.onStepRunFinished(stepRunFinished);
      }

      @Override
      public void onTestRunFinished(TestRunFinished testRunFinished) {
         delegate.onTestRunFinished(testRunFinished);
      }

      @Override
      public void onExecutionFinished(ExecutionFinished executionFinished) {
         try {
            String location = testReportRecordingService.recordTestReport(executionFinished.getReport());
            executionFinished.setReportLocation(location);
         } catch (Exception e) {
            executionFinished.getReport().addUnexpectedError(new UnexpectedErrorBuilder().fromThrowable(e).build());
         }
         delegate.onExecutionFinished(executionFinished);
      }
   }
}
