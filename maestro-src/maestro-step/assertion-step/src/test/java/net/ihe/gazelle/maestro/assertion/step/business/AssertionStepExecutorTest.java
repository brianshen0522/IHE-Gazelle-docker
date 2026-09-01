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

package net.ihe.gazelle.maestro.assertion.step.business;

import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.testreport.StepResult;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.testreport.validator.StepRunReportValidator;
import net.ihe.gazelle.maestro.assertion.step.business.contains.AssertContainsStepDefinition;
import net.ihe.gazelle.maestro.assertion.step.business.contains.AssertContainsStepExecutor;
import net.ihe.gazelle.maestro.assertion.step.business.equals.AssertEqualsStepDefinition;
import net.ihe.gazelle.maestro.assertion.step.business.equals.AssertEqualsStepExecutor;
import net.ihe.gazelle.maestro.spi.business.StepRun;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AssertionStepExecutorTest {

    @Test
    void execute_assert_equals_passed() {
        AssertEqualsStepExecutor runner = new AssertEqualsStepExecutor();
        Step step = buildStep(AssertEqualsStepDefinition.TYPE);
        Property expected = new StringProperty(AssertEqualsStepDefinition.EXPECTED, "Expected");
        Property actual = new StringProperty(AssertEqualsStepDefinition.ACTUAL, "Expected");
        step.setProperties(List.of(expected, actual));
        StepRun stepRun = new StepRun(step, List.of());

        StepRunReport report = runner.execute(stepRun);

        assertEquals(step.getName(), report.getStepName());
        assertEquals(AssertEqualsStepDefinition.TYPE, report.getType());
        assertEquals(StepResult.PASSED, report.getResult());
        assertDoesNotThrow(() -> new StepRunReportValidator().validate(report));
    }

    @Test
    void execute_assert_equals_failed() {
        AssertEqualsStepExecutor runner = new AssertEqualsStepExecutor();
        Step step = buildStep(AssertEqualsStepDefinition.TYPE);
        Property expected = new StringProperty(AssertEqualsStepDefinition.EXPECTED, "Expected");
        Property actual = new StringProperty(AssertEqualsStepDefinition.ACTUAL, "Actual");
        step.setProperties(List.of(expected, actual));
        StepRun stepRun = new StepRun(step, List.of());

        StepRunReport report = runner.execute(stepRun);

        assertEquals(step.getName(), report.getStepName());
        assertEquals(AssertEqualsStepDefinition.TYPE, report.getType());
        assertEquals(StepResult.FAILED, report.getResult());
        assertDoesNotThrow(() -> new StepRunReportValidator().validate(report));
    }

    @Test
    void execute_assert_contains_passed() {
        AssertContainsStepExecutor runner = new AssertContainsStepExecutor();
        Step step = buildStep(AssertContainsStepDefinition.TYPE);
        Property expected = new StringProperty(AssertEqualsStepDefinition.EXPECTED, "Expected");
        Property actual = new StringProperty(AssertEqualsStepDefinition.ACTUAL, "Expected");
        step.setProperties(List.of(expected, actual));
        StepRun stepRun = new StepRun(step, List.of());

        StepRunReport report = runner.execute(stepRun);

        assertEquals(step.getName(), report.getStepName());
        assertEquals(AssertContainsStepDefinition.TYPE, report.getType());
        assertEquals(StepResult.PASSED, report.getResult());
        assertDoesNotThrow(() -> new StepRunReportValidator().validate(report));
    }

    @Test
    void execute_assert_contains_failed() {
        AssertContainsStepExecutor runner = new AssertContainsStepExecutor();
        Step step = buildStep(AssertContainsStepDefinition.TYPE);
        Property expected = new StringProperty(AssertEqualsStepDefinition.EXPECTED, "Expected");
        Property actual = new StringProperty(AssertEqualsStepDefinition.ACTUAL, "Actual");
        step.setProperties(List.of(expected, actual));
        StepRun stepRun = new StepRun(step, List.of());

        StepRunReport report = runner.execute(stepRun);

        assertEquals(step.getName(), report.getStepName());
        assertEquals(AssertContainsStepDefinition.TYPE, report.getType());
        assertEquals(StepResult.FAILED, report.getResult());
        assertDoesNotThrow(() -> new StepRunReportValidator().validate(report));
    }

    private static Step buildStep(String type) {
        return new Step()
                .setName("step")
                .setType(type);
    }
}
