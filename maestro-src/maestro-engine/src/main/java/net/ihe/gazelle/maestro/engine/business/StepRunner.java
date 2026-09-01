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
import net.ihe.gazelle.maestro.api.business.message.StartStepRun;
import net.ihe.gazelle.maestro.api.business.message.StepRunFinished;
import net.ihe.gazelle.maestro.api.business.property.MissingPropertyException;
import net.ihe.gazelle.maestro.api.business.testreport.StepResult;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReportBuilder;
import net.ihe.gazelle.maestro.api.business.testreport.validator.StepRunReportValidator;
import net.ihe.gazelle.maestro.spi.business.StepExecutor;
import net.ihe.gazelle.maestro.spi.business.StepRun;
import org.slf4j.Logger;

/**
 * A stepRunnerJob receive a message from producer to execute Step, launch execution and send a result to Maestro Listener
 */
public class StepRunner {

   private static final Logger logger = org.slf4j.LoggerFactory.getLogger(StepRunner.class);

   private final StepExecutorProvider stepExecutorProvider;
   private final StepRunEventProducer stepRunEventProducer;
   private final StepRunReportValidator stepRunReportValidator = new StepRunReportValidator();

   /**
    * Creates a new {@code StepRunner} with the specified executor provider and event producer.
    *
    * @param stepExecutorProvider the provider used to obtain {@link StepExecutor} instances for executing steps
    * @param stepRunEventProducer the producer used to notify step run events
    */
   public StepRunner(StepExecutorProvider stepExecutorProvider, StepRunEventProducer stepRunEventProducer) {
      this.stepExecutorProvider = stepExecutorProvider;
      this.stepRunEventProducer = stepRunEventProducer;
   }

   /**
    * Execute Step with a stepRunner corresponding to the step
    *
    * @param startStepRun Message containing a step to run
    */
   public void run(StartStepRun startStepRun) {
      StepRun stepRun = startStepRun.getStepRun();
      try {
         StepExecutor executor = stepExecutorProvider.getExecutor(startStepRun.getSessionId(), stepRun);
         StepRunReport report = executor.execute(stepRun);
         stepRunReportValidator.validate(report).orThrow(IllegalStateException::new);
         stepRunEventProducer.notify(
               new StepRunFinished(startStepRun.getSessionId(), startStepRun.getTestId(), startStepRun.getStepIndex(),
                     stepRun, report)
         );
      } catch (UnknownStepException | MissingPropertyException | ClassCastException e) {
         // User error, due to bad test definition
         logger.atDebug().setCause(e).log("Unable to run step");
         stepRunEventProducer.notify(
               new StepRunFinished(startStepRun.getSessionId(), startStepRun.getTestId(), startStepRun.getStepIndex(),
                     stepRun, buildErrorReport(stepRun, e))
         );
      } catch (Exception e) {
         // Unexpected error
         logger.atError().setCause(e).log("Unexpected error while running step");
         stepRunEventProducer.notify(
               new StepRunFinished(startStepRun.getSessionId(), startStepRun.getTestId(), startStepRun.getStepIndex(),
                     stepRun, buildErrorReport(stepRun, e))
         );
      }
   }

   private static StepRunReport buildErrorReport(StepRun stepRun, Throwable e) {
      return new StepRunReportBuilder()
            .setStepName(stepRun.getName())
            .setType(stepRun.getType())
            .setResult(StepResult.UNDEFINED)
            .addUnexpectedError(new UnexpectedErrorBuilder().fromThrowable(e))
            .build();
   }

}
