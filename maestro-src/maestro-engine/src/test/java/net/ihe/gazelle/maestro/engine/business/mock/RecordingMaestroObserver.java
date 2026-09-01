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

import net.ihe.gazelle.maestro.api.business.MaestroObserver;
import net.ihe.gazelle.maestro.api.business.message.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class RecordingMaestroObserver implements MaestroObserver {

   private final List<TestRunStarted> testRunStartedEvents = new CopyOnWriteArrayList<>();
   private final List<StepRunStarted> stepRunStartedEvents = new CopyOnWriteArrayList<>();
   private final List<StepRunFinished> stepRunFinishedEvents = new CopyOnWriteArrayList<>();
   private final List<TestRunFinished> testRunFinishedEvents = new CopyOnWriteArrayList<>();
   private final CompletableFuture<ExecutionFinished> future = new CompletableFuture<>();

   @Override
   public void onTestRunStarted(TestRunStarted testRunStarted) {
      testRunStartedEvents.add(testRunStarted);
   }

   @Override
   public void onStepRunStarted(StepRunStarted stepRunStarted) {
      stepRunStartedEvents.add(stepRunStarted);
   }

   @Override
   public CompletableFuture<UserInteractionCompleted> interactWithUser(InteractWithUser interactWithUser) {
      return CompletableFuture.completedFuture(new UserInteractionCompleted("observer"));
   }

   @Override
   public void onStepRunFinished(StepRunFinished stepRunFinished) {
      stepRunFinishedEvents.add(stepRunFinished);
   }

   @Override
   public void onTestRunFinished(TestRunFinished testRunFinished) {
      testRunFinishedEvents.add(testRunFinished);
   }

    @Override
    public void onExecutionFinished(ExecutionFinished executionFinished) {
        future.complete(executionFinished);
    }

   public ExecutionFinished awaitSuiteFinished(Duration timeout) {
      try {
         return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
      } catch (Exception e) {
         throw new AssertionError("Test suite did not finish in time", e);
      }
   }

   public List<StepRunFinished> getStepRunFinishedEvents() {
      return new CopyOnWriteArrayList<>(stepRunFinishedEvents);
   }

   public List<TestRunFinished> getTestRunFinishedEvents() {
      return new CopyOnWriteArrayList<>(testRunFinishedEvents);
   }

   public List<TestRunStarted> getTestRunStartedEvents() {
      return new CopyOnWriteArrayList<>(testRunStartedEvents);
   }

   public List<StepRunStarted> getStepRunStartedEvents() {
      return new CopyOnWriteArrayList<>(stepRunStartedEvents);
   }
}
