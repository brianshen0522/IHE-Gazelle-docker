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

import net.ihe.gazelle.maestro.api.business.MaestroObserver;
import net.ihe.gazelle.maestro.api.business.message.*;
import net.ihe.gazelle.maestro.api.business.testreport.*;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.maestro.api.business.testrun.TestRunValidator;
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRun;
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRunValidator;
import net.ihe.gazelle.maestro.engine.business.context.SessionStore;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import net.ihe.gazelle.security.business.acl.AccessControlListBuilder;
import net.ihe.gazelle.servicemetadata.api.business.MetadataService;
import net.ihe.gazelle.servicemetadata.api.business.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Facade for executing test runs and test suite runs using Maestro.
 * Manages session observers, validation, and event production.
 */
public class MaestroFacade {

   private final TestSuiteRunValidator testSuiteRunValidator = new TestSuiteRunValidator();
   private final TestRunValidator testRunValidator = new TestRunValidator();
   private final SessionStore<MaestroObserver> testRunObserverStore;
   private final SessionStore<MaestroObserver> testSuiteRunObserverStore;
   private final MaestroEventProducer maestroEventProducer;
   private final Service maestroService;

   /**
    * Creates a new {@code MaestroFacade} with the provided session stores, event producer, and metadata service.
    *
    * @param testRunObserverStore      the store for test run observers
    * @param testSuiteRunObserverStore the store for test suite run observers
    * @param maestroEventProducer      the event producer for Maestro commands
    * @param metadataService           the service providing metadata
    */
   public MaestroFacade(SessionStore<MaestroObserver> testRunObserverStore,
                        SessionStore<MaestroObserver> testSuiteRunObserverStore,
                        MaestroEventProducer maestroEventProducer,
                        MetadataService metadataService) {
      this.testRunObserverStore = testRunObserverStore;
      this.testSuiteRunObserverStore = testSuiteRunObserverStore;
      this.maestroEventProducer = maestroEventProducer;
      this.maestroService = metadataService.getMetadata();
   }

   /**
    * Executes a {@link TestSuiteRun} and registers the associated observer.
    *
    * @param testSuiteRun         the test suite run to execute
    * @param testSuiteRunObserver the observer for this test suite run
    */
   public void executeTestSuite(TestSuiteRun testSuiteRun, MaestroObserver testSuiteRunObserver) {
      if (testSuiteRun.getAccessControlList() == null) {
         testSuiteRun.setAccessControlList(new AccessControlList().setPublic(true));
      }
      testSuiteRunValidator.assertValid(testSuiteRun);
      String sessionId = UUID.randomUUID().toString();
      long timeout = testSuiteRun.computeTestSuiteTimeout();
      testSuiteRunObserverStore.addSession(sessionId, testSuiteRunObserver, timeout);
      maestroEventProducer.command(new StartTestSuiteRun(sessionId, testSuiteRun));
   }

   /**
    * Executes a {@link TestRun} and registers the associated observer.
    *
    * @param testRun          the test run to execute
    * @param testRunObservers the observer for this test run
    */
   public void executeTest(TestRun testRun, MaestroObserver testRunObservers) {
      if (testRun.getAccessControlList() == null) {
         testRun.setAccessControlList(new AccessControlList().setPublic(true));
      }
      testRunValidator.assertValid(testRun);
      String sessionId = UUID.randomUUID().toString();
      long timeout = testRun.getTest().computeTestRunTimeout();
      testRunObserverStore.addSession(sessionId, testRunObservers, timeout);
      maestroEventProducer.command(new StartTestRun(sessionId, testRun));
   }

   /**
    * Notifies observers that a {@link StartTestRun} event has occurred.
    *
    * @param startTestRun the start test run event
    */
   public void listen(StartTestRun startTestRun) {
      Optional.ofNullable(testSuiteRunObserverStore.getSession(startTestRun.getSessionId()))
            .ifPresent(observer -> observer.onTestRunStarted(
                  new TestRunStarted(startTestRun.getTestRun().getTest().getId())
            ));
   }

    /**
     * Notifies observers that a {@link StartStepRun} event has occurred.
     *
     * @param startStepRun the start step run event
     */
   public void listen(StartStepRun startStepRun) {
      getObserver(startStepRun.getSessionId())
            .ifPresent(observer -> observer.onStepRunStarted(
                  new StepRunStarted(startStepRun.getTestId(), startStepRun.getStepIndex())
            ));
   }

    /**
     * Notifies observers that a {@link StepRunFinished} event has occurred.
     *
     * @param stepRunFinished the finished step run event
     */
   public void listen(StepRunFinished stepRunFinished) {
      getObserver(stepRunFinished.getSessionId())
            .ifPresent(observer -> observer.onStepRunFinished(stepRunFinished));
   }

    /**
     * Notifies observers that a {@link TestRunFinished} event has occurred.
     *
     * @param testRunFinished the finished test run event
     */
   public void listen(TestRunFinished testRunFinished) {
      String sessionId = testRunFinished.getSessionId();
      Optional.ofNullable(testSuiteRunObserverStore.getSession(sessionId))
            .ifPresentOrElse(
                  obs -> obs.onTestRunFinished(testRunFinished),
                  () -> Optional.ofNullable(testRunObserverStore.getSession(sessionId))
                        .ifPresent(obs -> obs.onExecutionFinished(asExecutionFinished(testRunFinished)))
            );
   }

    /**
     * Notifies observers that a {@link TestSuiteRunFinished} event has occurred.
     *
     * @param testSuiteRunFinished the finished test suite run event
     */
   public void listen(TestSuiteRunFinished testSuiteRunFinished) {
      Optional.ofNullable(testSuiteRunObserverStore.getSession(testSuiteRunFinished.getSessionId()))
            .ifPresent(observer -> {
               observer.onExecutionFinished(asExecutionFinished(testSuiteRunFinished));
               testSuiteRunObserverStore.removeSession(testSuiteRunFinished.getSessionId());
            });
   }

   private ExecutionFinished asExecutionFinished(TestRunFinished testRunFinished) {
      TestRun testRun = testRunFinished.getTestRun();
      TestReportBuilder testReportBuilder = new TestReportBuilder()
            .setTestService(getTestServiceBuilder())
            .setAccessControlList(new AccessControlListBuilder(testRun.getAccessControlList()))
            .setSystemsUnderTest(Optional.ofNullable(testRun.getSystemsUnderTest())
                  .orElse(Collections.emptyList())
                  .stream()
                  .map(SystemUnderTestBuilder::new)
                  .toList());
      testReportBuilder.setTestSuiteName(testRun.getTest().getName());
      testReportBuilder.addTestRunReport(testRunFinished.getTestRunReport());
      TestReport testReport = testReportBuilder.build();
      return new ExecutionFinished(testRunFinished.getSessionId(), testRunFinished.getTestRun(), testReport);
   }

   private ExecutionFinished asExecutionFinished(TestSuiteRunFinished testSuiteRunFinished) {
      TestReport testReport = buildGenericTestReport(testSuiteRunFinished.getTestSuiteRun())
            .addTestRunReports(testSuiteRunFinished.getTestRunReports())
            .build();
      return new ExecutionFinished(testSuiteRunFinished.getSessionId(), testSuiteRunFinished.getTestSuiteRun(), testReport);
   }

   private Optional<MaestroObserver> getObserver(String sessionId) {
      Optional<MaestroObserver> observer = Optional.ofNullable(testRunObserverStore.getSession(sessionId));
      if (observer.isEmpty()) {
         observer = Optional.ofNullable(testSuiteRunObserverStore.getSession(sessionId));
      }
      return observer;
   }

   private TestReportBuilder buildGenericTestReport(TestSuiteRun testSuiteRun) {
      TestReportBuilder testReportBuilder = new TestReportBuilder()
            .setTestService(getTestServiceBuilder())
            .setAccessControlList(new AccessControlListBuilder(testSuiteRun.getAccessControlList()))
            .setSystemsUnderTest(testSuiteRun.getSystemsUnderTest().stream().map(SystemUnderTestBuilder::new).toList());
      if (testSuiteRun.getTestSuite() != null) {
         testReportBuilder.setTestSuiteName(testSuiteRun.getTestSuite().getName());
      }
      return testReportBuilder;
   }

   private TestServiceBuilder getTestServiceBuilder() {
      return new TestServiceBuilder()
            .setDisclaimer(
                  "This Gazelle execution service is provided for technical compliance purposes only and does not " +
                        "constitute legal, medical, or regulatory advice.")
            .setServiceIdentification(
                  new EntityIdentificationBuilder()
                        .setName(maestroService.getName())
                        .setVersion(maestroService.getVersion())
            );
   }
}
