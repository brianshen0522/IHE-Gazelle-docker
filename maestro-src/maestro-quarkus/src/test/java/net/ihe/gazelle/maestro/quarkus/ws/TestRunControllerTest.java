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
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.maestro.api.business.SynchronousMaestro;
import net.ihe.gazelle.maestro.api.business.UnsupportedPropertyTypeException;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.test.Test;
import net.ihe.gazelle.maestro.api.business.test.TestReference;
import net.ihe.gazelle.maestro.api.business.test.TestSuite;
import net.ihe.gazelle.maestro.api.business.testreport.LocalizedTestReport;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRun;
import net.ihe.gazelle.maestro.api.technical.dto.report.TestReportDTO;
import net.ihe.gazelle.maestro.api.technical.dto.testrun.TestRunDTO;
import net.ihe.gazelle.maestro.api.technical.dto.testrun.TestSuiteRunDTO;
import net.ihe.gazelle.maestro.quarkus.mock.IdentityMock;
import net.ihe.gazelle.maestro.quarkus.utils.ObjectFactory;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class TestRunControllerTest {

   private static WireMockServer wireMockServer;
   private static int mockPort;
   private static final ScheduledExecutorService SCHEDULED_EXECUTOR = new ScheduledThreadPoolExecutor(5);

   private MaestroMockForREST maestroMock;
   private TestRunControllerImpl controller;

   @BeforeAll
   static void setup() {
      wireMockServer = new WireMockServer(0); // use random port
      wireMockServer.start();
      mockPort = wireMockServer.port();
   }

   @AfterAll
   static void tearDown() {
      wireMockServer.stop();
   }

   @BeforeEach
   void setUp() {
      maestroMock = new MaestroMockForREST(SCHEDULED_EXECUTOR);
      controller = new TestRunControllerImpl(new IdentityMock(Set.of("role:gazelle_admin")), new SynchronousMaestro(maestroMock), maestroMock);
   }

   @org.junit.jupiter.api.Test
   void testTestSuiteRunSync() {
      TestReport testReport = ObjectFactory.createTestReport();
      maestroMock.setTestReportSupplier(() -> testReport);

      try (Response response = controller.executeTestSuite(getTestSuiteRunDTO(), false, null)) {
         assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
         TestReportDTO entity = (TestReportDTO) response.getEntity();
         assertNotNull(entity);
         assertEquals(testReport.getTestSuiteName(), entity.getTestSuiteName());
      }
   }

   @org.junit.jupiter.api.Test
   void testTestRunSync() {
      TestReport testReport = ObjectFactory.createTestReport();
      maestroMock.setTestReportSupplier(() -> testReport);

      try (Response response = controller.executeTest(getTestRunDTO(), false, null)) {
         assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
         TestReportDTO entity = (TestReportDTO) response.getEntity();
         assertNotNull(entity);
         assertEquals(testReport.getTestSuiteName(), entity.getTestSuiteName());
      }
   }

   @org.junit.jupiter.api.Test
   void testTestRunSyncPersist() {
      TestReport testReport = ObjectFactory.createTestReport();
      maestroMock.setTestReportSupplier(() -> testReport);

      try (Response response = controller.executeTest(getTestRunDTO(), true, null)) {
         assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
         TestReportDTO entity = (TestReportDTO) response.getEntity();
         assertNotNull(entity);
         LocalizedTestReport localizedTestReport = (LocalizedTestReport) entity.getBusinessObject();
         assertNotNull(localizedTestReport.getLocation());
         assertTrue(localizedTestReport.getLocation().startsWith("http://localhost/datahouse/items/"));
      }
   }

   @org.junit.jupiter.api.Test
   void testTestSuiteRunAsync() {
      maestroMock.setTestReportSupplier(ObjectFactory::createTestReport);

      String callbackUrl = "http://localhost:" + mockPort + "/callback/testTestSuiteRunAsync";
      wireMockServer.stubFor(
            post(urlEqualTo("/callback/testTestSuiteRunAsync"))
                  .willReturn(WireMock.ok())
      );

      try (Response response = controller.executeTestSuite(getTestSuiteRunDTO(), false, callbackUrl)) {
         assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
         assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
         Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAsserted(
               () -> wireMockServer.verify(
                     1,
                     postRequestedFor(urlEqualTo("/callback/testTestSuiteRunAsync"))
                           .withoutHeader("Location")
                           .withRequestBody(containing("\"result\":\"PASSED\""))
               )
         );
      }
   }

   @org.junit.jupiter.api.Test
   void testTestRunAsync() {
      maestroMock.setTestReportSupplier(ObjectFactory::createTestReport);

      String callbackUrl = "http://localhost:" + mockPort + "/callback/testTestRunAsync";
      wireMockServer.stubFor(
            post(urlEqualTo("/callback/testTestRunAsync"))
                  .willReturn(WireMock.ok())
      );

      try (Response response = controller.executeTest(getTestRunDTO(), false, callbackUrl)) {
         assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
         assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
         Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAsserted(
               () -> wireMockServer.verify(
                     1,
                     postRequestedFor(urlEqualTo("/callback/testTestRunAsync"))
                           .withoutHeader("Location")
                           .withRequestBody(containing("\"result\":\"PASSED\""))
               )
         );
      }
   }

   @org.junit.jupiter.api.Test
   void testTestSuiteRunAsyncPersist() {
      maestroMock.setTestReportSupplier(ObjectFactory::createTestReport);

      String callbackUrl = "http://localhost:" + mockPort + "/callback/testTestSuiteRunAsyncPersist";
      wireMockServer.stubFor(
            post(urlEqualTo("/callback/testTestSuiteRunAsyncPersist"))
                  .willReturn(WireMock.ok())
      );

      try (Response response = controller.executeTestSuite(getTestSuiteRunDTO(), true, callbackUrl)) {
         assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
         Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAsserted(
               () -> wireMockServer.verify(
                     1,
                     postRequestedFor(urlEqualTo("/callback/testTestSuiteRunAsyncPersist"))
                           .withHeader("Location", containing("http://localhost/datahouse/items/"))
                           .withRequestBody(containing("\"result\":\"PASSED\""))
               )
         );
      }
   }

   @org.junit.jupiter.api.Test
   void testTestRunAsyncPersist() {
      maestroMock.setTestReportSupplier(ObjectFactory::createTestReport);

      String callbackUrl = "http://localhost:" + mockPort + "/callback/testTestRunAsyncPersist";
      wireMockServer.stubFor(
            post(urlEqualTo("/callback/testTestRunAsyncPersist"))
                  .willReturn(WireMock.ok())
      );

      try (Response response = controller.executeTest(getTestRunDTO(), true, callbackUrl)) {
         assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
         Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAsserted(
               () -> wireMockServer.verify(
                     1,
                     postRequestedFor(urlEqualTo("/callback/testTestRunAsyncPersist"))
                           .withHeader("Location", containing("http://localhost/datahouse/items/"))
                           .withRequestBody(containing("\"result\":\"PASSED\""))
               )
         );
      }
   }

   @org.junit.jupiter.api.Test
   void testInvalidTestSuiteRunAsync() {
      maestroMock.setErrorSupplier(() -> new IllegalArgumentException("invalid"));

      String callbackUrl = "http://localhost:" + mockPort + "/callback/testInvalidTestSuiteRunAsync";
      wireMockServer.stubFor(
            post(urlEqualTo("/callback/testTestSuiteRunAsyncPersist"))
                  .willReturn(WireMock.ok())
      );

      try (Response response = controller.executeTestSuite(getTestSuiteRunDTO(), false, callbackUrl)) {
         assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
         assertTrue(response.getEntity().toString().contains("bad format"));
         // Verify that no call back was made
         Awaitility.await().during(3, TimeUnit.SECONDS).untilAsserted(
               () -> wireMockServer.verify(
                     0,
                     postRequestedFor(urlEqualTo("/callback/testInvalidTestSuiteRunAsync"))
               )
         );
      }
   }

   @org.junit.jupiter.api.Test
   void testInvalidTestRunAsync() {
      maestroMock.setErrorSupplier(() -> new IllegalArgumentException("invalid"));

      String callbackUrl = "http://localhost:" + mockPort + "/callback/testInvalidTestRunAsync";
      wireMockServer.stubFor(
            post(urlEqualTo("/callback/testInvalidTestRunAsync"))
                  .willReturn(WireMock.ok())
      );

      try (Response response = controller.executeTest(getTestRunDTO(), false, callbackUrl)) {
         assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
         assertTrue(response.getEntity().toString().contains("bad format"));
         // Verify that no call back was made
         Awaitility.await().during(3, TimeUnit.SECONDS).untilAsserted(
               () -> wireMockServer.verify(
                     0,
                     postRequestedFor(urlEqualTo("/callback/testInvalidTestRunAsync"))
               )
         );
      }
   }

   @org.junit.jupiter.api.Test
   void testTestSuiteRunTimeout() {
      maestroMock.setTestReportSupplier(() -> {
         // Simulate a long-running test to trigger a timeout in synchronousMaestro
         LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
         return null;
      });

      try (Response response = controller.executeTestSuite(getTestSuiteRunDTO(), false, null)) {
         assertEquals(Response.Status.GATEWAY_TIMEOUT.getStatusCode(), response.getStatus());
         assertEquals(SynchronousMaestro.TIMEOUT_MESSAGE, response.getEntity());
      }
   }

   @org.junit.jupiter.api.Test
   void testTestRunTimeout() {
      maestroMock.setTestReportSupplier(() -> {
         // Simulate a long-running test to trigger a timeout in synchronousMaestro
         LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(4));
         return null;
      });

      try (Response response = controller.executeTest(getTestRunDTO(), false, null)) {
         assertEquals(Response.Status.GATEWAY_TIMEOUT.getStatusCode(), response.getStatus());
         assertEquals(SynchronousMaestro.TIMEOUT_MESSAGE, response.getEntity());
      }
   }

   @org.junit.jupiter.api.Test
   void testTestSuiteRunInternalServerError() {
      maestroMock.setErrorSupplier(() -> new RuntimeException("boom boom"));

      try (Response response = controller.executeTestSuite(getTestSuiteRunDTO(), false, null)) {
         assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
         assertTrue(Thread.currentThread().isInterrupted());
         assertTrue(response.getEntity().toString().contains("an error occurred"));
      }
   }

   @org.junit.jupiter.api.Test
   void testTestRunInternalServerError() {
      maestroMock.setErrorSupplier(() -> new RuntimeException("boom"));

      try (Response response = controller.executeTest(getTestRunDTO(), false, null)) {
         assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
         assertTrue(Thread.currentThread().isInterrupted());
         assertTrue(response.getEntity().toString().contains("an error occurred"));
      }
   }

   @org.junit.jupiter.api.Test
   void handleJsonProcessingExceptionWithIllegalArgumentCauseReturnsMessage() {
      IllegalArgumentException illegalArgumentException = new IllegalArgumentException("bad input");
      Exception wrapper = new RuntimeException("wrapper", illegalArgumentException);
      Exception root = new Exception("root", wrapper);

      try (Response response = controller.handleJsonProcessingException(root)) {
         assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
         assertEquals("bad input", response.getEntity());
      }
   }

   @org.junit.jupiter.api.Test
   void handleJsonProcessingExceptionWithUnsupportedPropertyTypeReturnsMessage() {
      UnsupportedPropertyTypeException unsupportedPropertyTypeException = new UnsupportedPropertyTypeException(
            "unsupported");
      Exception wrapper = new RuntimeException("wrapper", unsupportedPropertyTypeException);
      Exception root = new Exception("root", wrapper);

      try (Response response = controller.handleJsonProcessingException(root)) {
         assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
         assertEquals("unsupported", response.getEntity());
      }
   }

   @org.junit.jupiter.api.Test
   void handleJsonProcessingExceptionWithInvalidTypeIdReturnsOriginalMessage() {
      InvalidTypeIdException invalidTypeIdException = InvalidTypeIdException.from(null, "original message",
            TypeFactory.defaultInstance().constructType(Object.class), "type");

      try (Response response = controller.handleJsonProcessingException(invalidTypeIdException)) {
         assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
         assertEquals("original message", response.getEntity());
      }
   }

   @org.junit.jupiter.api.Test
   void handleJsonProcessingExceptionWithUnknownExceptionReturnsGenericMessage() {
      Exception exception = new Exception("unknown");

      try (Response response = controller.handleJsonProcessingException(exception)) {
         assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
         assertEquals("Unable to parse TestSuite structure.", response.getEntity());
      }
   }



   private TestRunDTO getTestRunDTO() {
      TestRun testRun = new TestRun()
            .setTest(
                  new Test().setId("T1").setName("Test 1").addStep(
                        new Step().setName("A step").setType("SOME_STEP_TYPE").setTimeout(10L)
                  )
            )
            .setAccessControlList(new AccessControlList().setPublic(true));
      return new TestRunDTO(testRun);
   }

   private TestSuiteRunDTO getTestSuiteRunDTO() {
      TestSuiteRun testSuiteRun = new TestSuiteRun()
            .setTestSuite(
                  new TestSuite()
                        .setId("TS1")
                        .setName("Test Suite 1")
                        .setTestReferences(List.of(
                              new TestReference().setTestId("T1"),
                              new TestReference().setTestId("T2")
                        ))
            )
            .setTests(List.of(
                  new Test().setId("T1").setName("Test 1").addStep(
                        new Step().setName("A step").setType("SOME_STEP_TYPE").setTimeout(10L)
                  ),
                  new Test().setId("T2").setName("Test 2").addStep(
                        new Step().setName("A step").setType("SOME_STEP_TYPE").setTimeout(10L)
                  )
            ))
            .setAccessControlList(new AccessControlList().setPublic(true));
      return new TestSuiteRunDTO(testSuiteRun);
   }
}
