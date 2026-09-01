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

package net.ihe.gazelle.maestro.api.business.testreport.validator;

import net.ihe.gazelle.framework.modelvalidator.business.ObjectResult;
import net.ihe.gazelle.maestro.api.business.testreport.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestReportValidatorTest {

    private final TestReportValidator validator = new TestReportValidator();

    @Test
    void shouldValidateCompleteTestReport() {
        TestReport testReport = buildValidTestReport();

        ObjectResult result = validator.validate(testReport);

        assertTrue(result.isValid(), () -> "Unexpected failures: " + result);
        validator.assertValid(testReport);
    }

    @Test
    void shouldRejectTestReportWhenCountersMismatch() {
        TestReport testReport = buildValidTestReport();
        testReport.setTestCounters(new TestCounters().setTotal(99));

        ObjectResult result = validator.validate(testReport);

        assertFalse(result.isValid());
    }

    private TestReport buildValidTestReport() {
        StepRunReport stepRunReport = new StepRunReport()
                .setStepName("Validate input")
                .setType("validation")
                .setResult(StepResult.PASSED);

        TestRunReport testRunReport = new TestRunReport()
                .setRunId("run-1")
                .setDateTime(Instant.now().minusSeconds(1))
                .setTest(new net.ihe.gazelle.maestro.api.business.testreport.Test().setId("test-id"))
                .addStepRunReport(stepRunReport)
                .setUrlToTestRun("http://example.com");
        testRunReport.computeResult();

        TestReport testReport = new TestReport()
                .setUuid(UUID.randomUUID().toString())
                .setDateTime(Instant.now().minusSeconds(1))
                .setTestSuiteName("suite")
                .setNote("note")
                .setUrlToTestSuiteResult("http://example.com/report")
                .setTestService(new TestService()
                        .setServiceIdentification(new EntityIdentification("service").setVersion("1.0"))
                        .setDisclaimer("disclaimer"))
                .addSystemUnderTest(new SystemUnderTest()
                        .setSystemIdentification(new EntityIdentification("sut")))
                .addTestRunReport(testRunReport);
        testReport.computeResult();
        testReport.computeCounters();
        return testReport;
    }
}
