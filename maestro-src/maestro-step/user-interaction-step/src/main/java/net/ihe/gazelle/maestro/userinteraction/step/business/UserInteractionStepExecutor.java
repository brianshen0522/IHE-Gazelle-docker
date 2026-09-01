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

package net.ihe.gazelle.maestro.userinteraction.step.business;

import net.ihe.gazelle.maestro.api.business.testreport.StepResult;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReportBuilder;
import net.ihe.gazelle.maestro.api.business.userinteract.UserInteractionHandler;
import net.ihe.gazelle.maestro.spi.business.StepExecutor;
import net.ihe.gazelle.maestro.spi.business.StepRun;

/**
 * Executor for user interaction steps.
 * This executor displays messages to users and waits for their acknowledgment.
 */
public class UserInteractionStepExecutor implements StepExecutor {

    private final UserInteractionHandler userInteractionHandler;

    /**
     * Constructor for UserInteractionStepExecutor.
     * @param userInteractionHandler the handler to manage user interactions
     */
    public UserInteractionStepExecutor(UserInteractionHandler userInteractionHandler) {
        this.userInteractionHandler = userInteractionHandler;
    }

    @Override
    public StepRunReport execute(StepRun stepRun) {
        String interactionTitle = stepRun.getPropertyValue(UserInteractionStepDefinition.INTERACTION_TITLE);
        String message = stepRun.getPropertyValue(UserInteractionStepDefinition.MESSAGE);

        userInteractionHandler.displayMessage(interactionTitle, message, stepRun.getTimeout());
        return buildStepRunReport(stepRun);
    }

    private StepRunReport buildStepRunReport(StepRun stepRun) {
        return new StepRunReportBuilder()
                .setStepName(stepRun.getName())
                .setType(UserInteractionStepDefinition.TYPE)
                .setResult(StepResult.DONE)
                .build();
    }
}
