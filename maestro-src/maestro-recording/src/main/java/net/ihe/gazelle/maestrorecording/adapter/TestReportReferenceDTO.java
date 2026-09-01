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

package net.ihe.gazelle.maestrorecording.adapter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.maestro.api.business.testreport.Result;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
      "testReportId",
      "result",
      "testSuiteName"
})
class TestReportReferenceDTO {

    private String testReportId;
    private Result result;
    private String testSuiteName;

    @JsonProperty("testReportId")
    String getTestReportId() {
        return testReportId;
    }

    void setTestReportId(String testReportId) {
        this.testReportId = testReportId;
    }

    @JsonProperty("result")
    Result getResult() {
        return result;
    }

    void setResult(Result result) {
        this.result = result;
    }

    @JsonProperty("testSuiteName")
    String getTestSuiteName() {
        return testSuiteName;
    }

    void setTestSuiteName(String testSuiteName) {
        this.testSuiteName = testSuiteName;
    }
}
