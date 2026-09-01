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
import net.ihe.gazelle.maestro.api.business.message.StartTestRun;
import net.ihe.gazelle.maestro.api.business.message.StepRunFinished;
import net.ihe.gazelle.maestro.api.business.message.TestRunFinished;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.test.Test;
import net.ihe.gazelle.maestro.api.business.testreport.StepResult;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.maestro.engine.business.context.TestRunSession;
import net.ihe.gazelle.maestro.spi.business.StepRun;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import org.junit.jupiter.api.BeforeEach;
import technical.dao.SessionStoreImpl;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class TestRunnerTest {

   private SessionStoreImpl<TestRunSession> sessionStore;
   private RecordingTestRunEventProducer eventProducer;
   private TestRunner testRunner;
   private Step firstStep;
   private TestRun testRun;

   @BeforeEach
   void setUp() {
      sessionStore = new SessionStoreImpl<>();
      eventProducer = new RecordingTestRunEventProducer();
      testRunner = new TestRunner(sessionStore, eventProducer);

      firstStep = new Step().setName("First").setType("SIM").addOutputMapping("payload", "firstPayload");
      Step secondStep = new Step().setName("Second").setType("SIM");

      Test test = new Test()
            .setId("test-1")
            .setName("Test 1")
            .setSteps(List.of(firstStep, secondStep));

      testRun = new TestRun()
            .setTest(test)
            .setInputs(List.of(new StringProperty("input", "value")))
            .setAccessControlList(new AccessControlList().setPublic(true));
   }

   @org.junit.jupiter.api.Test
   void runStoresSessionAndStartsFirstStep() {
      testRunner.run(new StartTestRun("session-1", testRun));

      assertThat(sessionStore.hasSession("session-1"), is(true));
      assertThat(eventProducer.getCommands(), hasSize(1));

      StartStepRun command = eventProducer.getCommands().getFirst();
      assertThat(command.getStepRun().hasProperty("input"), is(true));
      assertThat(command.getTestId(), equalTo(testRun.getTest().getId()));
   }

   @org.junit.jupiter.api.Test
   void listenAddsOutputsAndTriggersNextStep() {
      testRunner.run(new StartTestRun("session-ctx", testRun));
      StartStepRun firstCommand = eventProducer.getCommands().getFirst();

      StepRunReport report = new StepRunReport()
            .setStepName("First")
            .setType("SIM")
            .setResult(StepResult.PASSED)
            .setOutputs(List.of(new StringProperty("payload", "abc")));

      testRunner.listen(
            new StepRunFinished("session-ctx", testRun.getTest().getId(), 0, firstCommand.getStepRun(), report)
      );

      assertThat(sessionStore.getSession("session-ctx").getContext(), hasItem(
            allOf(hasProperty("name", equalTo("firstPayload")), hasProperty("value", equalTo("abc")))
      ));
      assertThat(eventProducer.getCommands(), hasSize(2));
      StartStepRun secondCommand = eventProducer.getCommands().get(1);
      assertThat(secondCommand.getStepRun().hasProperty("firstPayload"), is(true));
   }

   @org.junit.jupiter.api.Test
   void listenOnLastStepBuildsTestRunReportAndClearsSession() {
      testRunner.run(new StartTestRun("session-last", testRun));
      StartStepRun firstCommand = eventProducer.getCommands().getFirst();

      StepRunReport firstReport = new StepRunReport()
            .setStepName("First")
            .setType("SIM")
            .setResult(StepResult.PASSED)
            .setOutputs(List.of(new StringProperty("payload", "abc")));

      testRunner.listen(
            new StepRunFinished("session-last", testRun.getTest().getId(), 0, firstCommand.getStepRun(), firstReport)
      );

      StartStepRun secondCommand = eventProducer.getCommands().get(1);
      StepRunReport secondReport = new StepRunReport()
            .setStepName("Second")
            .setType("SIM")
            .setResult(StepResult.PASSED);

      testRunner.listen(
            new StepRunFinished("session-last", testRun.getTest().getId(), 1, secondCommand.getStepRun(), secondReport)
      );

      assertThat(sessionStore.hasSession("session-last"), is(false));
      assertThat(eventProducer.getNotifications(), hasSize(1));
      TestRunFinished notification = eventProducer.getNotifications().getFirst();
      assertThat(notification.getTestRunReport().getStepRunReports(), hasSize(2));
   }

    @org.junit.jupiter.api.Test
    void invalidTestRunShouldAddUnexpectedError() {
        TestRun invalid = new TestRun().setTest(new Test().setId("invalid"));
        testRunner.run(new StartTestRun("session-last", invalid));

        assertThat(eventProducer.getNotifications(), hasSize(1));
        TestRunFinished notification = eventProducer.getNotifications().getFirst();
        assertThat(notification.getTestRunReport().getUnexpectedErrors(), is(not(empty())));
    }

   @org.junit.jupiter.api.Test
   void listenWithoutSessionDoesNothing() {
      StepRunReport report = new StepRunReport().setStepName("Any").setType("SIM");
      testRunner.listen(new StepRunFinished("missing", "test", 5,
            new StepRun(firstStep, new ArrayList<>()), report));
      assertThat(eventProducer.getCommands(), empty());
      assertThat(eventProducer.getNotifications(), empty());
   }

   private static final class RecordingTestRunEventProducer implements TestRunEventProducer {

      private final List<StartStepRun> commands = new ArrayList<>();
      private final List<TestRunFinished> notifications = new ArrayList<>();

      @Override
      public void command(StartStepRun startStepRun) {
         commands.add(startStepRun);
      }

      @Override
      public void notify(TestRunFinished testRunFinished) {
         notifications.add(testRunFinished);
      }

      List<StartStepRun> getCommands() {
         return commands;
      }

      List<TestRunFinished> getNotifications() {
         return notifications;
      }
   }
}
