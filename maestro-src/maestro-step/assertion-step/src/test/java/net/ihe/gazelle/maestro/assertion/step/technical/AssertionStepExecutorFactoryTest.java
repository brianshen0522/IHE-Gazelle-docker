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

package net.ihe.gazelle.maestro.assertion.step.technical;

import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.assertion.step.business.contains.AssertContainsStepDefinition;
import net.ihe.gazelle.maestro.assertion.step.business.contains.AssertContainsStepExecutor;
import net.ihe.gazelle.maestro.assertion.step.business.equals.AssertEqualsStepDefinition;
import net.ihe.gazelle.maestro.assertion.step.business.equals.AssertEqualsStepExecutor;
import net.ihe.gazelle.maestro.assertion.step.technical.factory.AssertContainsStepExecutorFactory;
import net.ihe.gazelle.maestro.assertion.step.technical.factory.AssertEqualsStepExecutorFactory;
import net.ihe.gazelle.maestro.spi.business.StepExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AssertionStepExecutorFactoryTest {

    private AssertContainsStepExecutorFactory assertContainsStepExecutorFactory;
    private AssertEqualsStepExecutorFactory assertEqualsStepExecutorFactory;

    @BeforeEach
    void setUp() {
        assertContainsStepExecutorFactory = new AssertContainsStepExecutorFactory();
        assertEqualsStepExecutorFactory = new AssertEqualsStepExecutorFactory();
    }

    @Test
    void createAssertContainsExecutorTest() {
        String name = "test";
        Property expected = new StringProperty(AssertContainsStepDefinition.EXPECTED, "value");
        Property actual = new StringProperty(AssertContainsStepDefinition.ACTUAL, "value");
        Step assertionStep = new Step()
                .setName(name)
                .setProperties(List.of(expected, actual));
        StepExecutor stepExecutor = assertContainsStepExecutorFactory.createStepExecutor(assertionStep, Map.of());
        assertTrue(assertContainsStepExecutorFactory.getRequiredServices(assertionStep).isEmpty());
        assertNotNull(assertionStep);
        assertEquals(name, assertionStep.getName());
        assertInstanceOf(AssertContainsStepExecutor.class, stepExecutor);
        List<Property> propertiesRes = assertionStep.getProperties();
        assertEquals("value", propertiesRes.getFirst().getValue());
        assertEquals("value", propertiesRes.get(1).getValue());
    }

    @Test
    void createAssertEqualsExecutorTest() {
        String name = "test";
        Property expected = new StringProperty(AssertEqualsStepDefinition.EXPECTED, "value");
        Property actual = new StringProperty(AssertEqualsStepDefinition.ACTUAL, "value");
        Step assertionStep = new Step()
                .setName(name)
                .setProperties(List.of(expected, actual));
        StepExecutor stepExecutor = assertEqualsStepExecutorFactory.createStepExecutor(assertionStep, Map.of());
        assertTrue(assertEqualsStepExecutorFactory.getRequiredServices(assertionStep).isEmpty());
        assertNotNull(assertionStep);
        assertEquals(name, assertionStep.getName());
        assertInstanceOf(AssertEqualsStepExecutor.class, stepExecutor);
        List<Property> propertiesRes = assertionStep.getProperties();
        assertEquals("value", propertiesRes.getFirst().getValue());
        assertEquals("value", propertiesRes.get(1).getValue());
    }

    @Test
    void getSupportedStepTest() {
        assertEquals(AssertContainsStepDefinition.TYPE, assertContainsStepExecutorFactory.getSupportedStep());
        assertEquals(AssertEqualsStepDefinition.TYPE, assertEqualsStepExecutorFactory.getSupportedStep());
    }

}
