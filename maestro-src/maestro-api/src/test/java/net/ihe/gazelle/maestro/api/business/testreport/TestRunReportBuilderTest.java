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

package net.ihe.gazelle.maestro.api.business.testreport;

import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestRunReportBuilderTest {

    @Test
    void shouldBuildTestRunReportWithStepRuns() {
        Instant timestamp = Instant.parse("2025-01-01T00:00:00Z");

        StepRunReportBuilder stepRunReportBuilder = new StepRunReportBuilder()
                .setStepName("Validation step")
                .setType("VALIDATION")
                .setResult(StepResult.PASSED)
                .addOutput(new StringProperty("output", "value"));

        TestRunReportBuilder builder = new TestRunReportBuilder()
                .setRunId("run-42")
                .setDateTime(timestamp)
                .setTest(new TestBuilder()
                        .setId("test-1")
                        .setName("Sample test"))
                .addInput(new StringProperty("input", "example"))
                .addOutput(new StringProperty("result", "ok"))
                .addStepRunReport(stepRunReportBuilder)
                .setUrlToTestRun("http://example.test/run-42");

        TestRunReport report = builder.build();

        assertEquals("run-42", report.getRunId());
        assertEquals(timestamp, report.getDateTime());
        assertEquals("test-1", report.getTest().getId());
        assertEquals(1, report.getInputs().size());
        assertEquals("input", report.getInputs().getFirst().getName());
        assertEquals(1, report.getOutputs().size());
        assertEquals(Result.PASSED, report.getResult());

        assertEquals(1, report.getStepRunReports().size());
        StepRunReport stepRunReport = report.getStepRunReports().getFirst();
        assertEquals("Validation step", stepRunReport.getStepName());
        assertEquals(StepResult.PASSED, stepRunReport.getResult());
        assertEquals(1, stepRunReport.getOutputs().size());
        assertNotNull(report.getUrlToTestRun());
    }
}
