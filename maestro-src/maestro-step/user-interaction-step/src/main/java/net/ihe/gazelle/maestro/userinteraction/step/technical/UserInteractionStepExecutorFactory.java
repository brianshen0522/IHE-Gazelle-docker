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

package net.ihe.gazelle.maestro.userinteraction.step.technical;

import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.userinteract.UserInteractionHandler;
import net.ihe.gazelle.maestro.spi.business.Handler;
import net.ihe.gazelle.maestro.spi.business.StepExecutor;
import net.ihe.gazelle.maestro.spi.technical.StepExecutorFactory;
import net.ihe.gazelle.maestro.userinteraction.step.business.UserInteractionStepDefinition;
import net.ihe.gazelle.maestro.userinteraction.step.business.UserInteractionStepExecutor;

import java.util.Map;
import java.util.Optional;

/**
 * Factory for creating UserInteractionStepExecutor instances.
 * This factory creates executors for the user interaction step type.
 */
public class UserInteractionStepExecutorFactory implements StepExecutorFactory {

    /**
     * Default constructor.
     */
    public UserInteractionStepExecutorFactory() { /* Default constructor */ }

    @Override
    public String getSupportedStep() {
        return UserInteractionStepDefinition.TYPE;
    }

    @Override
    public Map<String, Class<? extends Handler>> getRequiredServices(Step step) {
        return Map.of(
                UserInteractionHandler.SERVICE_NAME, UserInteractionHandler.class
        );
    }

    @Override
    public StepExecutor createStepExecutor(Step step, Map<String, Handler> handlers) {
        return new UserInteractionStepExecutor(
                Optional.ofNullable((UserInteractionHandler) handlers.get(UserInteractionHandler.SERVICE_NAME))
                        .orElseThrow(() -> new IllegalArgumentException("No user interaction handler provided"))
        );
    }
}
