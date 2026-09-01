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

package net.ihe.gazelle.maestro.quarkus.websocket.dto;

import com.fasterxml.jackson.annotation.*;
import net.ihe.gazelle.errorhandling.technical.UnexpectedErrorDTO;
import net.ihe.gazelle.maestro.api.business.message.TestRunFinished;
import net.ihe.gazelle.maestro.api.business.testreport.Result;
import net.ihe.gazelle.maestro.api.business.testreport.TestRunReport;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Schema(
      name = "TestRunFinished",
      description = "Message sent when a test run is finished."
)
@JsonTypeName(TestRunFinishedDTO.TYPE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
      "type",
      "testId",
      "dateTime",
      "result",
      "unexpectedErrors"
})
public class TestRunFinishedDTO extends MessageDTO<TestRunReport> {

   @JsonIgnore
   public static final String TYPE = "TEST_RUN_FINISHED";

   public TestRunFinishedDTO() {
      this(new TestRunFinished());
   }

   public TestRunFinishedDTO(TestRunFinished message) {
      super(message.getTestRunReport() != null
            ? message.getTestRunReport()
            : new TestRunReport());
   }

   @Schema(
         name = "testId",
         description = "The unique identifier of the test."
   )
   @JsonProperty("testId")
   public String getTestId() {
      return getBusinessObject().getTest().getId();
   }

   @Schema(
         name = "dateTime",
         description = "The zoned date time of the end of the execution of the test.",
         examples = "2025-01-01T16:30:00.387+01:00"
   )
   @JsonProperty("dateTime")
   public String getDateTime() {
      return ZonedDateTime.ofInstant(getBusinessObject().getDateTime(), ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
   }

   @Schema(
         name = "result",
         description = "The result of the test execution.",
         enumeration = {Result.RESULT_PASSED, Result.RESULT_FAILED, Result.RESULT_UNDEFINED}
   )
   @JsonProperty("result")
   public String getResult() {
      return getBusinessObject().getResult().name();
   }

   @Schema(
         name = "unexpectedErrors",
         description = "A list of unexpected errors that can occur during test execution."
   )
   @JsonProperty("unexpectedErrors")
   public List<UnexpectedErrorDTO> getUnexpectedErrors() {
      return getBusinessObject()
            .getUnexpectedErrors()
            .stream()
            .map(UnexpectedErrorDTO::new)
            .toList();
   }

   @Override
   @JsonIgnore
   public Class<TestRunReport> getBusinessObjectClass() {
      return TestRunReport.class;
   }
}
