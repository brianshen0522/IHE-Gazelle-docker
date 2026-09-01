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

package net.ihe.gazelle.maestro.quarkus.factory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import net.ihe.gazelle.maestro.api.business.Maestro;
import net.ihe.gazelle.maestro.api.business.MaestroObserver;
import net.ihe.gazelle.maestro.api.business.SynchronousMaestro;
import net.ihe.gazelle.maestro.engine.business.*;
import net.ihe.gazelle.maestro.engine.business.context.SessionStore;
import net.ihe.gazelle.maestro.engine.business.context.TestRunSession;
import net.ihe.gazelle.maestro.engine.business.context.TestSuiteSession;
import net.ihe.gazelle.maestro.spi.business.StepExecutor;
import net.ihe.gazelle.servicemetadata.api.business.MetadataService;
import org.eclipse.microprofile.config.inject.ConfigProperty;


/**
 * Factory class responsible for creating and providing configured Maestro engine components.
 */
@ApplicationScoped
public class MaestroFactory {

   private final SessionStore<TestSuiteSession> suiteSessionStore;
   private final SessionStore<TestRunSession> testSessionStore;
   private final SessionStore<MaestroObserver> testRunObserverStore;
   private final SessionStore<MaestroObserver> testSuiteRunObserverStore;
   private final MaestroEventProducer maestroEventProducer;
   private final TestSuiteEventProducer testSuiteEventProducer;
   private final TestRunEventProducer testRunEventProducer;
   private final StepRunEventProducer stepRunEventProducer;
   private final StepExecutorProvider stepExecutorProvider;
   private final TestReportRecordingService testReportRecordingService;
   private final MetadataService metadataService;
   private final Boolean reportRecordingEnabled;


   /**
    * Creates a new {@code MaestroFactory} with all required dependencies injected by CDI.
    *
    * @param suiteSessionStore the session store for {@link TestSuiteSession} instances
    * @param testSessionStore the session store for {@link TestRunSession} instances
    * @param testRunObserverStore the session store for {@link MaestroObserver} of test runs
    * @param testSuiteRunObserverStore the session store for {@link MaestroObserver} of test suite runs
    * @param maestroEventProducer the event producer for Maestro commands
    * @param testSuiteTestReportRecordService the event producer for test suite events
    * @param testRunEventProducer the event producer for test run events
    * @param stepRunEventProducer the event producer for step run events
    * @param testReportRecordingService the service responsible for recording test reports
    * @param stepExecutorProvider the provider for {@link StepExecutor} instances
    * @param metadataService the service providing metadata information
    * @param reportRecordingEnabled flag indicating if test report recording is enabled
    */
   @Inject
   public MaestroFactory(@TestSuiteStore SessionStore<TestSuiteSession> suiteSessionStore,
                         @TestRunStore SessionStore<TestRunSession> testSessionStore,
                         @TestRunObserverStore SessionStore<MaestroObserver> testRunObserverStore,
                         @TestSuiteRunObserverStore SessionStore<MaestroObserver> testSuiteRunObserverStore,
                         MaestroEventProducer maestroEventProducer,
                         TestSuiteEventProducer testSuiteTestReportRecordService,
                         TestRunEventProducer testRunEventProducer,
                         StepRunEventProducer stepRunEventProducer,
                         TestReportRecordingService testReportRecordingService,
                         StepExecutorProvider stepExecutorProvider,
                         MetadataService metadataService,
                         @ConfigProperty(name = "gzl.report.recording.enabled") Boolean reportRecordingEnabled) {
      this.suiteSessionStore = suiteSessionStore;
      this.testSessionStore = testSessionStore;
      this.testRunObserverStore = testRunObserverStore;
      this.testSuiteRunObserverStore = testSuiteRunObserverStore;
      this.maestroEventProducer = maestroEventProducer;
      this.testSuiteEventProducer = testSuiteTestReportRecordService;
      this.testRunEventProducer = testRunEventProducer;
      this.stepRunEventProducer = stepRunEventProducer;
      this.testReportRecordingService = testReportRecordingService;
      this.stepExecutorProvider = stepExecutorProvider;
      this.metadataService = metadataService;
      this.reportRecordingEnabled = reportRecordingEnabled;
   }

   /**
    * Produces a {@link SynchronousMaestro} instance configured with the appropriate underlying Maestro.
    *
    * @return a new {@link SynchronousMaestro} instance
    */
   @Produces
   @Default
   public SynchronousMaestro getSynchronousMaestro() {
      return new SynchronousMaestro(getRecordingMaestro());
   }

   /**
    * Produces a {@link Maestro} instance that may be recording or standalone
    * depending on the {@code reportRecordingEnabled} flag.
    *
    * @return a configured {@link Maestro} instance
    */
   @Produces
   @Default
   public Maestro getRecordingMaestro() {
      if(Boolean.TRUE.equals(reportRecordingEnabled)) {
         return new RecordingMaestro(getMaestro(), testReportRecordingService);
      }
      return new StandaloneMaestro(getMaestro());
   }

   /**
    * Produces a {@link MaestroFacade} instance which handles the orchestration of test and suite executions.
    *
    * @return a new {@link MaestroFacade} instance
    */
   @Produces
   @Default
   public MaestroFacade getMaestro() {
      return new MaestroFacade(
            testRunObserverStore,
            testSuiteRunObserverStore,
            maestroEventProducer,
            metadataService
      );
   }

   /**
    * Produces a {@link TestSuiteRunner} instance configured with the test suite session store
    * and the test suite event producer.
    *
    * @return a new {@link TestSuiteRunner} instance
    */
   @Produces
   @Default
   public TestSuiteRunner getTestSuiteRunner() {
      return new TestSuiteRunner(suiteSessionStore, testSuiteEventProducer);
   }

   /**
    * Produces a {@link TestRunner} instance configured with the test run session store
    * and the test run event producer.
    *
    * @return a new {@link TestRunner} instance
    */
   @Produces
   @Default
   public TestRunner getTestRunner() {
      return new TestRunner(testSessionStore, testRunEventProducer);
   }

   /**
    * Produces a {@link StepRunner} instance configured with the {@link StepExecutorProvider}
    * and the step run event producer.
    *
    * @return a new {@link StepRunner} instance
    */
   @Produces
   @Default
   public StepRunner getStepRunner() {
      return new StepRunner(stepExecutorProvider, stepRunEventProducer);
   }

}
