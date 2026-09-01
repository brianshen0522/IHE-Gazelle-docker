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

import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.testreport.StepResult;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.userinteract.stub.UserInteractionHandlerStub;
import net.ihe.gazelle.maestro.spi.business.StepRun;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserInteractionStepExecutorTest {

    @Test
    void user_interaction_step_executor_test() {
        UserInteractionStepExecutor executor = new UserInteractionStepExecutor(new UserInteractionHandlerStub());
        StepRun stepRun = stepRun();

        StepRunReport report = executor.execute(stepRun);
        assertEquals(stepRun.getName(), report.getStepName());
        assertEquals(UserInteractionStepDefinition.TYPE, report.getType());
        assertEquals(StepResult.DONE, report.getResult());
    }

    private Step step() {
        return new Step()
                .setName("Simulation")
                .setType(UserInteractionStepDefinition.TYPE)
                .setProperties(List.of(
                        new StringProperty(UserInteractionStepDefinition.INTERACTION_TITLE, "title"),
                        new StringProperty(UserInteractionStepDefinition.MESSAGE, "message")
                ));
    }

    private StepRun stepRun() {
        return new StepRun(step(),  List.of());
    }
}
