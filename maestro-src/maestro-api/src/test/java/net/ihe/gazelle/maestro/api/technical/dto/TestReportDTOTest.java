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

package net.ihe.gazelle.maestro.api.technical.dto;

import net.ihe.gazelle.errorhandling.business.UnexpectedError;
import net.ihe.gazelle.errorhandling.business.UnexpectedErrorValidator;
import net.ihe.gazelle.errorhandling.technical.UnexpectedErrorDTO;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.testreport.*;
import net.ihe.gazelle.maestro.api.business.testreport.validator.SystemUnderTestValidator;
import net.ihe.gazelle.maestro.api.business.testreport.validator.TestReportValidator;
import net.ihe.gazelle.maestro.api.technical.dto.report.StepRunReportDTO;
import net.ihe.gazelle.maestro.api.technical.dto.report.SystemUnderTestDTO;
import net.ihe.gazelle.maestro.api.technical.dto.report.TestReportDTO;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestReportDTOTest {

   private static final TextSerDes SER_DES = new JacksonSerDes();

   private final TestReportValidator testReportValidator = new TestReportValidator();
   private final SystemUnderTestValidator systemUnderTestValidator = new SystemUnderTestValidator();
   private final UnexpectedErrorValidator unexpectedErrorValidator = new UnexpectedErrorValidator();

   @Test
   void shouldSerializeAndDeserializeCompleteReport() {
      TestReport source = buildCompleteReport();

      String json = assertDoesNotThrow(() -> SER_DES.serializeAsString(new TestReportDTO(source)));
      TestReportDTO dto = assertDoesNotThrow(() -> SER_DES.deserialize(json, TestReportDTO.class));
      TestReport restored = dto.getBusinessObject();

      testReportValidator.assertValid(restored);
      assertEquals(source.getUuid(), restored.getUuid());
      assertEquals(source.getTestRunReports().size(), restored.getTestRunReports().size());
      assertEquals(source.getUnexpectedErrors().size(), restored.getUnexpectedErrors().size());
   }

   @Test
   void shouldRoundTripSystemUnderTestDto() {
      SystemUnderTest source = new SystemUnderTest()
            .setSystemIdentification(new EntityIdentification("sut").setVersion("1.0"))
            .addHostName("sut.local")
            .addIpAddress("192.0.2.10")
            .addMacAddress("aa:bb:cc:dd:ee:ff");

      String json = assertDoesNotThrow(() -> SER_DES.serializeAsString(new SystemUnderTestDTO(source)));
      SystemUnderTest restored = assertDoesNotThrow(() -> SER_DES.deserialize(json, SystemUnderTest.class));

      systemUnderTestValidator.assertValid(restored);
      assertEquals(source, restored);
   }

   @Test
   void shouldRoundTripStepRunOutputProperties() {
      StepRunReport stepRunReport = new StepRunReport()
            .setType("validation")
            .setResult(StepResult.PASSED)
            .addOutput(new ByteArrayProperty("document", "content".getBytes(StandardCharsets.UTF_8)));

      String json = assertDoesNotThrow(() -> SER_DES.serializeAsString(new StepRunReportDTO(stepRunReport)));
      StepRunReportDTO dto = assertDoesNotThrow(() -> SER_DES.deserialize(json, StepRunReportDTO.class));
      StepRunReport restored = dto.getBusinessObject();

      assertEquals(1, restored.getOutputs().size());
      ByteArrayProperty property = (ByteArrayProperty) restored.getOutputs().getFirst();
      assertEquals("document", property.getName());
      assertEquals("content", new String(property.getValue(), StandardCharsets.UTF_8));
   }

   @Test
   void shouldRoundTripUnexpectedErrorHierarchy() {
      UnexpectedError source = new UnexpectedError()
            .setName("root")
            .setMessage("root message")
            .setCause(new UnexpectedError()
                  .setName("cause")
                  .setMessage("cause message"));

      String json = assertDoesNotThrow(() -> SER_DES.serializeAsString(new UnexpectedErrorDTO(source)));
      UnexpectedError restored = assertDoesNotThrow(() -> SER_DES.deserialize(json, UnexpectedError.class));

      unexpectedErrorValidator.assertValid(restored);
      unexpectedErrorValidator.assertValid(restored.getCause());
      assertEquals("cause message", restored.getCause().getMessage());
   }

   private TestReport buildCompleteReport() {
      Instant baseTime = Instant.now().minusSeconds(300);

      StepRunReport stepRunReport = new StepRunReport()
            .setStepName("Validate input")
            .setType("validation")
            .setResult(StepResult.PASSED)
            .addOutput(new StringProperty("log", "ok"));

      TestRunReport testRunReport = new TestRunReport()
            .setRunId("run-1")
            .setDateTime(baseTime.plusSeconds(60))
            .setTest(new net.ihe.gazelle.maestro.api.business.testreport.Test()
                  .setId("test-1")
                  .setName("Sample test")
                  .setVersion("1.0"))
            .addInput(new StringProperty("payload", "value"))
            .addOutput(new ByteArrayProperty("document", "content".getBytes(StandardCharsets.UTF_8)))
            .addStepRunReport(stepRunReport)
            .setUrlToTestRun("http://example.com/run/1")
            .addUnexpectedError(new UnexpectedError().setName("run-warning").setMessage("step warning"));
      testRunReport.computeResult();

      TestReport testReport = new TestReport()
            .setUuid(UUID.randomUUID().toString())
            .setDateTime(baseTime.plusSeconds(120))
            .setTestSuiteName("suite")
            .setNote("note")
            .setUrlToTestSuiteResult("http://example.com/report")
            .setTestService(new TestService()
                  .setServiceIdentification(new EntityIdentification("service").setVersion("1.0"))
                  .setDisclaimer("disclaimer"))
            .addSystemUnderTest(new SystemUnderTest()
                  .setSystemIdentification(new EntityIdentification("sut-1")))
            .addTestRunReport(testRunReport)
            .addUnexpectedError(new UnexpectedError().setName("root-warning").setMessage("global warning"));

      testReport.computeResult();
      testReport.computeCounters();
      return testReport;
   }
}
