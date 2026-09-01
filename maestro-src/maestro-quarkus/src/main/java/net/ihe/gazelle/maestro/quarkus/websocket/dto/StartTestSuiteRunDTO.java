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
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRun;
import net.ihe.gazelle.maestro.api.technical.dto.testrun.TestSuiteRunDTO;
import net.ihe.gazelle.oidc.common.technical.dto.SecuredMessageDTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
      name = "StartTestSuiteRun",
      description = "Message sent when a new test suite run is started."
)
@JsonTypeName(StartTestSuiteRunDTO.TYPE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
      "type",
      "testSuiteRun",
      "persist"
})
public class StartTestSuiteRunDTO extends MessageDTO<TestSuiteRun> implements SecuredMessageDTO {

   @JsonIgnore
   public static final String TYPE = "START_TEST_SUITE_RUN";

   private boolean persist;
   private final String authorization;

   public StartTestSuiteRunDTO() {
      this(new TestSuiteRun(), "");
   }

   public StartTestSuiteRunDTO(TestSuiteRun testSuiteRun,  String authorization) {
      super(testSuiteRun);
      this.persist = true;
      this.authorization = authorization;
   }

   @Schema(
         name = "testSuiteRun",
         description = "The test suite run that is starting."
   )
   @JsonProperty("testSuiteRun")
   public TestSuiteRunDTO getTestSuiteRun() {
      return new TestSuiteRunDTO(getBusinessObject());
   }

   public void setTestSuiteRun(TestSuiteRunDTO testSuiteRun) {
      this.message = testSuiteRun.getBusinessObject();
   }

   @Schema(
         name = "persist",
         description = "Whether the test suite report should be persisted."
   )
   @JsonProperty("persist")
   public boolean isPersist() {
      return persist;
   }

   public void setPersist(boolean persist) {
      this.persist = persist;
   }

   @Override
   public String getAuthorization() {
      return authorization;
   }

   @Override
   @JsonIgnore
   public Class<TestSuiteRun> getBusinessObjectClass() {
      return TestSuiteRun.class;
   }
}

