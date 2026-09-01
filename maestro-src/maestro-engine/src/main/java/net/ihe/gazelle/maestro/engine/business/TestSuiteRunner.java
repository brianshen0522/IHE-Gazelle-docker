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

import net.ihe.gazelle.errorhandling.business.UnexpectedErrorBuilder;
import net.ihe.gazelle.maestro.api.business.InvalidTestSuiteException;
import net.ihe.gazelle.maestro.api.business.message.StartTestRun;
import net.ihe.gazelle.maestro.api.business.message.StartTestSuiteRun;
import net.ihe.gazelle.maestro.api.business.message.TestRunFinished;
import net.ihe.gazelle.maestro.api.business.message.TestSuiteRunFinished;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.test.TestReference;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRun;
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRunValidator;
import net.ihe.gazelle.maestro.engine.business.context.SessionStore;
import net.ihe.gazelle.maestro.engine.business.context.TestRunCursor;
import net.ihe.gazelle.maestro.engine.business.context.TestSuiteSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * TestSuiteRunner is responsible for executing {@link TestSuiteRun} instances.
 */
public class TestSuiteRunner {

   private static final Logger LOG = LoggerFactory.getLogger(TestSuiteRunner.class);

   private final SessionStore<TestSuiteSession> sessionStore;
   private final TestSuiteEventProducer testSuiteEventProducer;
   private final TestSuiteRunValidator testSuiteRunValidator = new TestSuiteRunValidator();

   /**
    * Creates a new {@code TestSuiteRunner} with the specified session store and event producer.
    *
    * @param sessionStore the store used to manage {@link TestSuiteSession} instances
    * @param testSuiteEventProducer the producer used to send test suite events
    */
   public TestSuiteRunner(SessionStore<TestSuiteSession> sessionStore, TestSuiteEventProducer testSuiteEventProducer) {
      this.sessionStore = sessionStore;
      this.testSuiteEventProducer = testSuiteEventProducer;
   }

   /**
    * Execute a TestSuite with given inputs.
    *
    * @param startTestSuiteRun the start test suite run command. Must not be null.
    *
    * @throws NullPointerException if startTestSuiteRun is null
    */
   public void run(StartTestSuiteRun startTestSuiteRun) {
      TestSuiteRun testSuiteRun = startTestSuiteRun.getTestSuiteRun();
      try {
         testSuiteRunValidator.validate(testSuiteRun).orThrow(InvalidTestSuiteException::new);

         TestRunCursor testRunCursor = TestRunCursor.from(consolidateTestRuns(testSuiteRun));
         long timeout = testSuiteRun.computeTestSuiteTimeout();
         sessionStore.addSession(startTestSuiteRun.getSessionId(), new TestSuiteSession(testSuiteRun, testRunCursor), timeout);
         testSuiteEventProducer.command(new StartTestRun(startTestSuiteRun.getSessionId(), testRunCursor.next()));
      } catch (Exception e) {
         LOG.atError().setCause(e).log("Error when starting test suite for session {}", startTestSuiteRun.getSessionId());
         sessionStore.removeSession(startTestSuiteRun.getSessionId());
         testSuiteEventProducer.notify(
               new TestSuiteRunFinished()
                       .setSessionId(startTestSuiteRun.getSessionId())
                       .setTestSuiteRun(testSuiteRun)
                       .setUnexpectedErrors(List.of(new UnexpectedErrorBuilder().fromThrowable(e).build())));
      }
   }

   /**
    * Handles a {@link TestRunFinished} event by updating the session state,
    * sending the next test run command, or finishing the test suite.
    *
    * @param testRunFinished the finished test run event to process
    */
   public void listen(TestRunFinished testRunFinished) {
      String sessionId = testRunFinished.getSessionId();
      if (sessionId != null && sessionStore.hasSession(sessionId)) {
         TestSuiteSession testSuiteSession = sessionStore.getSession(sessionId);
         try {
            if (testSuiteSession != null) {
               testSuiteSession.addTestRunReport(testRunFinished.getTestRunReport());
               TestRunCursor testRunCursor = testSuiteSession.getTestRunCursor();
               if (!testRunCursor.hasNext()) {
                  testSuiteEventProducer.notify(
                        new TestSuiteRunFinished(sessionId, testSuiteSession.getTestSuiteRun(), testSuiteSession.getTestRunReports()));
                   sessionStore.removeSession(sessionId);
               } else {
                  testSuiteEventProducer.command(new StartTestRun(sessionId, testRunCursor.next()));
               }
            }
         } catch (Exception e) {
            LOG.atError().setCause(e).log("Error listening testRun finished on session {}", sessionId);
             testSuiteEventProducer.notify(
                     new TestSuiteRunFinished()
                             .setSessionId(sessionId)
                             .setTestSuiteRun(testSuiteSession.getTestSuiteRun())
                             .setTestRunReports(testSuiteSession.getTestRunReports())
                             .setUnexpectedErrors(List.of(new UnexpectedErrorBuilder().fromThrowable(e).build())));
             sessionStore.removeSession(sessionId);
         }
      }
   }

   /**
    * Create an ordered list of TestRuns from the TestSuiteRun definition, and with consolidated inputs.
    *
    * @param testSuiteRun the test suite run
    *
    * @return a list of ordered TestRuns to execute, with propagated inputs
    */
   private List<TestRun> consolidateTestRuns(TestSuiteRun testSuiteRun) {
      List<TestRun> testRuns = new ArrayList<>();
      for (TestReference testRef : testSuiteRun.getTestSuite().getTestReferences()) {
         String testId = testRef.getTestId();
         testRuns.add(new TestRun()
               .setTest(testSuiteRun.getTest(testId))
               .setInputs(consolidateInputs(testSuiteRun, testRef))
               .setAccessControlList(testSuiteRun.getAccessControlList()));
      }
      return testRuns;
   }

   /**
    * Propagate test suite run inputs to test run inputs.
    *
    * @param testSuiteRun the test suite run
    * @param testRef the test reference
    *
    * @return a merged list of properties to be consumed by the concerned test run
    */
   private List<Property> consolidateInputs(TestSuiteRun testSuiteRun, TestReference testRef) {
      List<Property> consolidatedInputs = new ArrayList<>(testSuiteRun.getInputs());
      consolidatedInputs.addAll(testRef.getProperties());
      return consolidatedInputs;
   }
}
