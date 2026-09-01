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

package net.ihe.gazelle.maestro.quarkus.websocket;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;
import net.ihe.gazelle.maestro.api.business.message.*;
import net.ihe.gazelle.maestro.quarkus.utils.ObjectFactory;
import net.ihe.gazelle.maestro.quarkus.websocket.dto.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActivateRequestContext
class WebSocketSessionTest {

    @TestHTTPResource("/test-server")
    URI uri;

    private Session session;

    @BeforeAll
    void openConnection() throws Exception {
        session = ContainerProvider
                .getWebSocketContainer()
                .connectToServer(TestWebSocketClient.class, uri);
    }

    @BeforeEach
    void resetReceiver() {
        TestWebSocketServer.clear();
    }

    @AfterAll
    void closeConnection() throws IOException {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    @Test
    void should_send_test_run_started_message() throws InterruptedException {
        WebSocketSession ws = new WebSocketSession(session);
        ws.onTestRunStarted(new TestRunStarted("testId"));

        String messageString = TestWebSocketServer.pollLastMessage();
        assertNotNull(messageString);
        assertThat(
             messageString,
             containsString(TestRunStartedDTO.TYPE)
        );
    }

    @Test
    void should_send_step_run_started_message() throws InterruptedException {
        WebSocketSession ws = new WebSocketSession(session);
        ws.onStepRunStarted(new StepRunStarted("testId", 3));

        String messageString = TestWebSocketServer.pollLastMessage();
        assertNotNull(messageString);
        assertThat(
              messageString,
              containsString(StepRunStartedDTO.TYPE)
        );
    }

    @Test
    void should_send_user_interaction_message() throws InterruptedException {
        WebSocketSession ws = new WebSocketSession(session);
        InteractWithUser interactWithUser = new InteractWithUser()
                .setInteractionTitle("title")
                .setMessage("message");
        ws.interactWithUser(interactWithUser);
        ws.completeInteraction(new UserInteractionCompleted());

        String messageString = TestWebSocketServer.pollLastMessage();
        assertNotNull(messageString);
        assertThat(
              messageString,
              containsString(InteractWithUserDTO.TYPE)
        );
    }

    @Test
    void should_send_step_run_finished_message() throws InterruptedException {
        WebSocketSession ws = new WebSocketSession(session);
        ws.onStepRunFinished(new StepRunFinished(
                session.getId(),
                "testId", 3,
                ObjectFactory.createStepRun(),
                ObjectFactory.createStepRunReport())
        );

        String messageString = TestWebSocketServer.pollLastMessage();
        assertThat(
              messageString,
              containsString(StepRunFinishedDTO.TYPE)
        );
    }

    @Test
    void should_send_test_run_finished_message() throws InterruptedException {
        WebSocketSession ws = new WebSocketSession(session);
        ws.onTestRunFinished(new TestRunFinished(
                session.getId(),
                ObjectFactory.createTestRun(),
                ObjectFactory.createTestRunReport()));

        String messageString = TestWebSocketServer.pollLastMessage();
        assertThat(
              messageString,
              containsString(TestRunFinishedDTO.TYPE)
        );
    }

    @Test
    void should_send_execution_finished_message() throws InterruptedException, DeploymentException, IOException {
        Session internal = ContainerProvider
                .getWebSocketContainer()
                .connectToServer(TestWebSocketClient.class, uri);
        WebSocketSession ws = new WebSocketSession(internal);
        ws.onExecutionFinished(new ExecutionFinished(
                session.getId(),
                ObjectFactory.createTestRun(),
                ObjectFactory.createTestReport()
        ));

        String messageString = TestWebSocketServer.pollLastMessage();
        assertThat(
              messageString,
              containsString(ExecutionFinishedDTO.TYPE)
        );
        assertFalse(internal.isOpen());
    }
}
