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
import net.ihe.gazelle.maestro.api.business.message.StepRunFinished;
import net.ihe.gazelle.maestro.api.business.testreport.StepResult;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Schema(
      name = "StepRunFinished",
      description = "Message sent when a step is finished."
)
@JsonTypeName(StepRunFinishedDTO.TYPE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
      "type",
      "testId",
      "stepIndex",
      "dateTime",
      "result",
      "unexpectedErrors"
})
public class StepRunFinishedDTO extends MessageDTO<StepRunFinished> {

   @JsonIgnore
   public static final String TYPE = "STEP_RUN_FINISHED";

   public StepRunFinishedDTO() {
      this(new StepRunFinished());
   }

   public StepRunFinishedDTO(StepRunFinished message) {
      super(message);
   }

   @Schema(
         name = "testId",
         description = "The unique identifier of the test that own this step."
   )
   @JsonProperty("testId")
   public String getTestRunId() {
      return message.getTestId();
   }

   @Schema(
         name = "stepIndex",
         description = "The index of the step.",
         type = SchemaType.INTEGER
   )
   @JsonProperty("stepIndex")
   public int getStepIndex() {
      return message.getStepIndex();
   }

   @Schema(
         name = "dateTime",
         description = "The zoned date time of the end of the execution of the step.",
         examples = "2025-01-01T16:30:00.387+01:00"
   )
   @JsonProperty("dateTime")
   public String getDateTime() {
      return ZonedDateTime.ofInstant(message.getStepRunReport().getDateTime(), ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
   }

   @Schema(
         name = "result",
         description = "The result of the step.",
         enumeration = {StepResult.RESULT_DONE, StepResult.RESULT_PASSED, StepResult.RESULT_FAILED, StepResult.RESULT_UNDEFINED}
   )
   @JsonProperty("result")
   public String getResult() {
      return message.getStepRunReport().getResult().name();
   }

   @Schema(
         name = "unexpectedErrors",
         description = "A list of unexpected errors that can occur during step execution."
   )
   @JsonProperty("unexpectedErrors")
   public List<UnexpectedErrorDTO> getUnexpectedErrors() {
      return message.getStepRunReport()
            .getUnexpectedErrors()
            .stream()
            .map(UnexpectedErrorDTO::new)
            .toList();
   }

   @Override
   @JsonIgnore
   public Class<StepRunFinished> getBusinessObjectClass() {
      return StepRunFinished.class;
   }
}
