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
import net.ihe.gazelle.maestro.api.business.message.ExecutionFinished;
import net.ihe.gazelle.maestro.api.technical.dto.report.TestReportDTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
      name = "ExecutionFinished",
      description = "Message sent when the execution of a test suite or a test is finished."
)
@JsonTypeName(ExecutionFinishedDTO.TYPE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
      "type",
      "testReport",
      "location",
})
public class ExecutionFinishedDTO extends MessageDTO<ExecutionFinished> {

   @JsonIgnore
   public static final String TYPE = "EXECUTION_FINISHED";

   public ExecutionFinishedDTO() {
      this(new ExecutionFinished());
   }

   public ExecutionFinishedDTO(ExecutionFinished testReport) {
      super(testReport);
   }

   @Schema(
         name = "testReport",
         description = "The test report of the test suite or test execution."
   )
   @JsonProperty("testReport")
   public TestReportDTO getTestReport() {
      return new TestReportDTO(message.getReport());
   }

   public void setTestReport(TestReportDTO testReport) {
      message.setReport(testReport.getBusinessObject());
   }

   @Schema(
         name = "location",
         description = "A link to retrieve the test report."
   )
   @JsonProperty("location")
   public String getLocation() {
      return message.getReportLocation();
   }

   public void setLocation(String location) {
      message.setReportLocation(location);
   }

   @Override
   @JsonIgnore
   public Class<ExecutionFinished> getBusinessObjectClass() {
      return ExecutionFinished.class;
   }
}
