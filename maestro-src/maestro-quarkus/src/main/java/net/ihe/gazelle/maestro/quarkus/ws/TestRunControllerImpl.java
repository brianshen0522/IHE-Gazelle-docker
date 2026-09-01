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

import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import io.quarkus.security.AuthenticationFailedException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.lang.TimeoutRuntimeException;
import net.ihe.gazelle.maestro.api.business.Maestro;
import net.ihe.gazelle.maestro.api.business.SynchronousMaestro;
import net.ihe.gazelle.maestro.api.business.UnsupportedPropertyTypeException;
import net.ihe.gazelle.maestro.api.business.testreport.LocalizedTestReport;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRun;
import net.ihe.gazelle.maestro.api.technical.dto.report.TestReportDTO;
import net.ihe.gazelle.maestro.api.technical.dto.testrun.TestRunDTO;
import net.ihe.gazelle.maestro.api.technical.dto.testrun.TestSuiteRunDTO;
import net.ihe.gazelle.security.business.GazelleIdentity;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Implementation of {@link TestRunController} that handles test run requests.
 */
@RequestScoped
public class TestRunControllerImpl implements TestRunController {

   private static final Logger LOG = LoggerFactory.getLogger(TestRunControllerImpl.class);

   private final GazelleIdentity identity;
   private final SynchronousMaestro synchronousMaestro;
   private final Maestro maestro;

   /**
    * Creates a new instance of {@code TestRunControllerImpl}.
    *
    * @param identity           the authenticated user identity
    * @param synchronousMaestro the synchronous Maestro engine for executing tests
    * @param maestro            the generic Maestro engine for asynchronous test execution
    */
   @Inject
   public TestRunControllerImpl(GazelleIdentity identity, SynchronousMaestro synchronousMaestro, Maestro maestro) {
      this.identity = identity;
      this.synchronousMaestro = synchronousMaestro;
      this.maestro = maestro;
   }

   @Override
   public Response executeTestSuite(TestSuiteRunDTO testSuite, boolean persist, String callback) {
      TestSuiteRun testSuiteRun = testSuite.getBusinessObject();
      if (callback != null) {
         return handleErrors(() -> runTestSuiteAsynchronously(testSuiteRun, persist, callback));
      } else {
         return handleErrors(() -> runTestSuiteSynchronously(testSuiteRun, persist));
      }
   }

   @Override
   public Response executeTest(TestRunDTO testRunDTO, boolean persist, String callback) {
      TestRun testRun = testRunDTO.getBusinessObject();
      if (callback != null) {
         return handleErrors(() -> runTestAsynchronously(testRun, persist, callback));
      } else {
         return handleErrors(() -> runTestSynchronously(testRun, persist));
      }
   }

   private Response handleErrors(Supplier<Response> serviceCall) {
      try {
         if (!identity.isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
         }
         return serviceCall.get();
      } catch (IllegalArgumentException e) {
         LOG.debug("TestRun format Exception", e);
         return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
               .entity("The request contains a testRun with bad format" + e).build();
      } catch (TimeoutRuntimeException e) {
         return Response.status(Response.Status.GATEWAY_TIMEOUT.getStatusCode())
               .entity(SynchronousMaestro.TIMEOUT_MESSAGE).build();
      } catch (Exception e) {
         LOG.error("Unknown error", e);
         Thread.currentThread().interrupt();
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
               .entity("an error occurred during execution: " + e).build();
      }
   }

   private Response runTestSuiteSynchronously(TestSuiteRun testSuiteRun, boolean persist) {
      try {
         LocalizedTestReport report = (LocalizedTestReport) synchronousMaestro.executeTestSuite(testSuiteRun, persist);
         return Response
               .ok(new TestReportDTO(report))
               .header(HttpHeaders.LOCATION, report.getLocation()).build();
      } catch (TimeoutException e) {
         throw new TimeoutRuntimeException(e);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new ExecutionInterruptedException("Interrupted while running test suite", e);
      }
   }

   private Response runTestSuiteAsynchronously(TestSuiteRun testSuiteRun, boolean persist, String callback) {
      maestro.executeTestSuite(testSuiteRun, persist, new ApacheTestReportClient(callback));
      return Response.accepted().build();
   }

   private Response runTestSynchronously(TestRun testRun, boolean persist) {
      try {
         LocalizedTestReport report = (LocalizedTestReport) synchronousMaestro.executeTest(testRun, persist);
         return Response
               .ok(new TestReportDTO(report))
               .header(HttpHeaders.LOCATION, report.getLocation()).build();
      } catch (TimeoutException e) {
         throw new TimeoutRuntimeException(e);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new ExecutionInterruptedException("Interrupted while running test", e);
      }
   }

   private Response runTestAsynchronously(TestRun testRun, boolean persist, String callback) {
      maestro.executeTest(testRun, persist, new ApacheTestReportClient(callback));
      return Response.accepted().build();
   }

   /**
    * This method is automatically triggered by Jakarta Jackson if the received JSON couldn't be deserialized into
    * TestRunDTO object
    *
    * @param exception The exception Jakarta Jackson thrown while deserializing received JSON
    * @return A Bad Request status code with error message from Jackson
    */
   @ServerExceptionMapper
   public Response handleJsonProcessingException(Exception exception) {
      String message;
      if (exception instanceof AuthenticationFailedException) {
         return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      if (exception.getCause() != null && exception.getCause().getCause() != null) {
         if (exception.getCause().getCause() instanceof IllegalArgumentException illegalArgumentException) {
            message = illegalArgumentException.getMessage();
         } else if (exception.getCause()
               .getCause() instanceof UnsupportedPropertyTypeException unsupportedPropertyTypeException) {
            message = unsupportedPropertyTypeException.getMessage();
         } else {
            LOG.error(exception.getMessage(), exception);
            message = "Unable to parse TestSuite structure.";
         }
      } else {
         if (exception instanceof InvalidTypeIdException invalidTypeIdException) {
            message = invalidTypeIdException.getOriginalMessage();
         } else {
            LOG.error(exception.getMessage(), exception);
            message = "Unable to parse TestSuite structure.";
         }
      }

      return Response.status(Response.Status.BAD_REQUEST)
            .entity(message)
            .type(MediaType.APPLICATION_JSON)
            .build();
   }
}
