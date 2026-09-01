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

import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.userinteract.UserInteractionHandler;
import net.ihe.gazelle.maestro.api.business.userinteract.stub.UserInteractionHandlerStub;
import net.ihe.gazelle.maestro.spi.business.Handler;
import net.ihe.gazelle.maestro.spi.business.StepExecutor;
import net.ihe.gazelle.maestro.userinteraction.step.business.UserInteractionStepDefinition;
import net.ihe.gazelle.maestro.userinteraction.step.business.UserInteractionStepExecutor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserInteractionStepExecutorFactoryTest {

    private final UserInteractionStepExecutorFactory factory = new UserInteractionStepExecutorFactory();

    @Test
    void stepTypeTest() {
        assertEquals(UserInteractionStepDefinition.TYPE, factory.getSupportedStep());
    }

    @Test
    void getRequiredServicesOfUserInteractionStep() {
        Step userInteractionStep = getUserInteractionStep();
        assertEquals(1, factory.getRequiredServices(userInteractionStep).size());
    }

    @Test
    void createStepExecutorTest() {
        Step userInteractionStep = getUserInteractionStep();
        UserInteractionHandler handler = new UserInteractionHandlerStub();
        Map<String, Handler> emptyHandlers = Map.of();
        assertThrows(IllegalArgumentException.class, () -> factory.createStepExecutor(userInteractionStep, emptyHandlers));

        StepExecutor executor = factory.createStepExecutor(userInteractionStep, Map.of(
                UserInteractionHandler.SERVICE_NAME,
                handler)
        );

        assertInstanceOf(UserInteractionStepExecutor.class, executor);
    }

    private Step getUserInteractionStep() {
        return new Step()
                .setName("simulationStep")
                .setType(UserInteractionStepDefinition.TYPE)
                .setProperties(List.of(
                        new StringProperty(UserInteractionStepDefinition.INTERACTION_TITLE, "title"),
                        new StringProperty(UserInteractionStepDefinition.MESSAGE, "message")
                ));
    }
}
