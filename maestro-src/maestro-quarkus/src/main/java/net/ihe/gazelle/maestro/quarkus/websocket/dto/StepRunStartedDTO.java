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
import net.ihe.gazelle.maestro.api.business.message.StepRunStarted;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Schema(
      name = "StepRunStarted",
      description = "Message sent when a step is started."
)
@JsonTypeName(StepRunStartedDTO.TYPE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
      "type",
      "testId",
      "stepIndex",
      "dateTime"
})
public class StepRunStartedDTO extends MessageDTO<StepRunStarted> {

   @JsonIgnore
   public static final String TYPE = "STEP_RUN_STARTED";

   public StepRunStartedDTO() {
      this(new StepRunStarted());
   }

   public StepRunStartedDTO(StepRunStarted message) {
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
   public int getStepName() {
      return message.getStepIndex();
   }

   @Schema(
         name = "dateTime",
         description = "The zoned date time of the beginning of the execution of the step.",
         examples = "2025-01-01T16:30:00.387+01:00"
   )
   @JsonProperty("dateTime")
   public String getDateTime() {
      return ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
   }

   @Override
   @JsonIgnore
   public Class<StepRunStarted> getBusinessObjectClass() {
      return StepRunStarted.class;
   }
}
