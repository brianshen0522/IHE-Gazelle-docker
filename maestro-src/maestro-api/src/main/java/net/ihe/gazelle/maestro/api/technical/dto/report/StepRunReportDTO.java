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

package net.ihe.gazelle.maestro.api.technical.dto.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.errorhandling.technical.UnexpectedErrorDTO;
import net.ihe.gazelle.maestro.api.business.testreport.StepResult;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.technical.dto.property.PropertyDTO;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import net.ihe.gazelle.modelmarshaller.technical.dto.SubTypingDTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Schema(
      name = "StepRunReport",
      description = "A report containing the result of a test step execution."
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
      "stepName",
      "type",
      "dateTime",
      "result",
      "outputs",
      "unexpectedErrors"
})
public class StepRunReportDTO implements DTO<StepRunReport> {

   @JsonIgnore
   private final StepRunReport stepRunReport;

   public StepRunReportDTO() {
      this(new StepRunReport());
   }

   public StepRunReportDTO(StepRunReport stepRunReport) {
      this.stepRunReport = stepRunReport;
   }

   @Override
   @JsonIgnore
   public StepRunReport getBusinessObject() {
      return stepRunReport;
   }

   @Schema(
         name = "stepName",
         description = "The name of the step that was executed.",
         examples = "ITI-90 Simulation step",
         required = true
   )
   @JsonProperty(value = "stepName")
   public String getStepName() {
      return stepRunReport.getStepName();
   }

   public void setStepName(String stepName) {
      stepRunReport.setStepName(stepName);
   }

   @Schema(
         name = "type",
         description = "The type of the step that was executed.",
         examples = {"SIMULATION", "VALIDATION"},
         required = true
   )
   @JsonProperty(value = "type")
   public String getType() {
      return stepRunReport.getType();
   }

   public void setType(String type) {
      stepRunReport.setType(type);
   }

   @Schema(
         name = "dateTime",
         description = "The zoned date time of the step execution.",
         examples = "2025-01-01T16:30:00.387+01:00",
         required = true
   )
   @JsonProperty(value = "dateTime")
   public String getDateTime() {
      return ZonedDateTime.ofInstant(getBusinessObject().getDateTime(), ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
   }

   public void setDateTime(String dateTime) {
      getBusinessObject().setDateTime(
            Instant.from(ZonedDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
      );
   }

   @Schema(
         name = "result",
         description = "The result of the test step.",
         enumeration = {StepResult.RESULT_DONE, StepResult.RESULT_PASSED, StepResult.RESULT_FAILED, StepResult.RESULT_UNDEFINED},
         required = true
   )
   @JsonProperty(value = "result")
   public String getResult() {
      return stepRunReport.getResult().name();
   }

   public void setResult(String result) {
      stepRunReport.setResult(StepResult.valueOf(result));
   }

   @Schema(
         name = "outputs",
         description = "The outputs of the step execution."
   )
   @SuppressWarnings("rawtypes")
   @JsonProperty(value = "outputs")
   public List<PropertyDTO> getOutputs() {
      return stepRunReport.getOutputs().stream()
            .map(SubTypingDTO::fromBusinessObject)
            .map(PropertyDTO.class::cast)
            .toList();
   }

   @SuppressWarnings("rawtypes")
   public void setOutputs(List<PropertyDTO> outputs) {
      stepRunReport.setOutputs(
            outputs.stream()
                  .map(PropertyDTO::getBusinessObject)
                  .toList()
      );
   }

   @Schema(
         name = "unexpectedErrors",
         description = "The errors that can happen during step execution."
   )
   @JsonProperty(value = "unexpectedErrors")
   public List<UnexpectedErrorDTO> getUnexpectedErrors() {
      return stepRunReport
            .getUnexpectedErrors()
            .stream()
            .map(UnexpectedErrorDTO::new)
            .toList();
   }

   public void setUnexpectedErrors(List<UnexpectedErrorDTO> unexpectedErrors) {
      stepRunReport.setUnexpectedErrors(
            unexpectedErrors.stream()
                  .map(UnexpectedErrorDTO::getBusinessObject)
                  .toList()
      );
   }
}
