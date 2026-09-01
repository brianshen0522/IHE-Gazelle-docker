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

package net.ihe.gazelle.maestro.quarkus.ws;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.maestro.api.technical.dto.report.TestReportDTO;
import net.ihe.gazelle.maestro.api.technical.dto.testrun.TestRunDTO;
import net.ihe.gazelle.maestro.api.technical.dto.testrun.TestSuiteRunDTO;
import net.ihe.gazelle.oidc.rest.business.ProtectedResource;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import static net.ihe.gazelle.maestro.quarkus.ws.Constants.*;

/**
 * REST API interface for executing Gazelle tests and test suites.
 * <p>
 * This API allows clients to trigger synchronous or asynchronous test execution using the Maestro engine.
 * If a {@code callback} query parameter is provided, the test run will be executed asynchronously, and the
 * results will be sent to the specified callback URI. Otherwise, the test run will be executed synchronously,
 * and the results will be returned directly in the response.
 */
@Path("/v1")
public interface TestRunController {

    /**
     * Name of the interface.
     */
    String INTERFACE_NAME = "Test Run API";

    /**
     * Version of the interface.
     */
    String INTERFACE_VERSION = "1.0";

    /**
     * Execute a Gazelle test suite.
     *
     * @param testSuiteRun the test suite to execute
     * @param persist      whether to persist the test report
     * @param callback     optional callback URL for asynchronous execution
     * @return a {@link Response} containing the {@link TestReportDTO} or HTTP status depending on execution mode
     */
    @POST
    @Path("/test-suite/run")
    @ProtectedResource
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = INTERFACE_NAME)
    @SecurityRequirement(name = "Keycloak")
    @Operation(
            summary = "Run Gazelle test suite",
            description = """
                       This API is used to execute a Gazelle test suite with the maestro engine.
                       If the callback query parameter is not set, this will run synchronously and directly send the Test report as a response.
                       If the callback query parameter is set, this will run asynchronously and will send the Test report to the specified callback URI later on.
                    """
    )
    @APIResponse(
            responseCode = "200",
            description = "Test suite has been executed, Contains the Test report with all detailed steps and results.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = TestReportDTO.class)
            )
    )
    @APIResponse(
            responseCode = "202",
            description = "Test run accepted"
    )
    @APIResponse(
            responseCode = "400",
            description = "The test run is malformed"
    )
    @APIResponse(
          responseCode = "401",
          description = "Unauthorized to execute a test suite run"
    )
    @APIResponse(
            responseCode = "500",
            description = "Unexpected Error"
    )
    @APIResponse(
            responseCode = "504",
            description = "Synchronous run has timed out"
    )
    Response executeTestSuite(
            @RequestBody(
                    description = "The test suite to run.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TestSuiteRunDTO.class),
                            examples = {
                                    @ExampleObject(name = "Simple example", value = TEST_SUITE_RUN),
                                    @ExampleObject(name = "Example with two validations", value = TEST_SUITE_TWO_VALIDATIONS),
                                    @ExampleObject(name = "Example with different type of tests", value = TEST_SUITE_MULTIPLE_TESTS)
                            }
                    )
            ) TestSuiteRunDTO testSuiteRun,
            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "persist",
                    description = "Choose whether to persist the test report or not"
            ) @QueryParam("persist") @DefaultValue("true") boolean persist,
            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "callback",
                    description = "Callback URL for asynchronous test run. For such callback to be secured, the client should generate a unique and strong sessionId included into the callback URL.",
                    example = "http://localhost/gazelle/report?session=10"
            ) @QueryParam("callback") String callback
    );

    /**
     * Execute a test.
     *
     * @param testRun  the test to execute
     * @param persist  whether to persist the test report
     * @param callback optional callback URL for asynchronous execution
     * @return a {@link Response} containing the {@link TestReportDTO} or HTTP status depending on execution mode
     */
    @POST
    @Path("/test/run")
    @ProtectedResource
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = INTERFACE_NAME)
    @SecurityRequirement(name = "Keycloak")
    @Operation(
            summary = "Run Gazelle test",
            description = """
                       This API is used to execute a Gazelle test suite with the maestro engine.
                       If the callback query parameter is not set, this will run synchronously and directly send the Test report as a response.
                       If the callback query parameter is set, this will run asynchronously and will send the Test report to the specified callback URI later on.
                    """
    )
    @APIResponse(
            responseCode = "200",
            description = "Test has been executed, Contains the Test report with all detailed steps and results.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = TestReportDTO.class)
            )
    )
    @APIResponse(
            responseCode = "202",
            description = "Test run accepted"
    )
    @APIResponse(
            responseCode = "400",
            description = "The test run is malformed"
    )
    @APIResponse(
          responseCode = "401",
          description = "Unauthorized to execute a test run"
    )
    @APIResponse(
            responseCode = "500",
            description = "Unexpected Error"
    )
    @APIResponse(
            responseCode = "504",
            description = "Synchronous run has timed out"
    )
    Response executeTest(
            @RequestBody(
                    description = "The test to run.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TestRunDTO.class),
                            examples = {
                                    @ExampleObject(name = "Simple assert test run", value = TEST_RUN_ASSERT),
                                    @ExampleObject(name = "Simple validation test run", value = TEST_RUN_VALIDATION),
                            })
            ) TestRunDTO testRun,
            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "persist",
                    description = "Choose whether to persist the test report or not"
            ) @QueryParam("persist") @DefaultValue("true") boolean persist,
            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "callback",
                    description = "Callback URL for asynchronous test run. For such callback to be secured, the client should generate a unique and strong sessionId included into the callback URL.",
                    example = "http://localhost/gazelle/report?session=10"
            ) @QueryParam("callback") String callback
    );

}
