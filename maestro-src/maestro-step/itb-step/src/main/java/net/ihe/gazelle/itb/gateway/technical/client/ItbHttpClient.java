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

package net.ihe.gazelle.itb.gateway.technical.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ihe.gazelle.itb.gateway.business.ItbClient;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbReporting;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbTestSession;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbSessionCreationInformation;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbSessionStatusInformation;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbStartRequest;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbStartResponse;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbStatusRequest;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Adapter to execute HTTP requests to ITB (Interoperability Test Bed)
 */
public class ItbHttpClient implements ItbClient {

    private static final Logger logger = LoggerFactory.getLogger(ItbHttpClient.class);
    public static final String APPLICATION_JSON = "application/json";

    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseItbUrl;
    private final String itbApiKey;

    /**
     * Creates an ITB REST client.
     *
     * @param baseItbUrl ITB base URL
     * @param itbApiKey ITB API key
     */
    public ItbHttpClient(String baseItbUrl, String itbApiKey) {
        this.baseItbUrl = baseItbUrl;
        this.itbApiKey = itbApiKey;
    }

    @Override
    public String startTest(ItbStartRequest testRequest) {
        ItbStartRequest request = withAsyncOptions(testRequest);
        ItbStartResponse response = sendPOSTRequest(
                baseItbUrl + "/api/rest/tests/start",
                request,
                null,
                ItbStartResponse.class
        );
        ItbSessionCreationInformation sessionInfo = extractFirstCreatedSession(response);
        return sessionInfo.getSession();
    }

    @Override
    public ItbReporting startTestAndWait(ItbStartRequest testRequest, long timeoutMs) {
        ItbStartRequest request = withSyncOptions(testRequest, timeoutMs);
        ItbStartResponse response = sendPOSTRequest(
                baseItbUrl + "/api/rest/tests/start",
                request,
                timeoutMs,
                ItbStartResponse.class
        );
        ItbSessionCreationInformation sessionInfo = extractFirstCreatedSession(response);
        String sessionId = sessionInfo.getSession();
        if (!Boolean.TRUE.equals(sessionInfo.getCompleted())) {
            throw new ItbHttpClientException(
                    "ITB sync start did not complete within maximumWaitTime for session " + sessionId
            );
        }

        ItbSessionStatusInformation status = getSessionStatusInformation(sessionId, true, true, timeoutMs);
        String report = status.getReport();
        if (report == null || report.isBlank()) {
            throw new ItbHttpClientException(
                    "ITB sync response does not contain test report for session " + sessionId
            );
        }
        String logs = status.getLogs() == null || status.getLogs().isEmpty()
                ? ""
                : String.join("\n", status.getLogs());

        String testCaseIdentifier = extractTestCaseIdentifier(testRequest);
        return new ItbReporting(
                null,
                new ItbTestSession(null, testCaseIdentifier, sessionId),
                report
        ).setLogs(logs);
    }

    @Override
    public String getTestLogs(String sessionID) {
        if (sessionID == null) {
            return "";
        }
        ItbSessionStatusInformation sessionStatus = getSessionStatusInformation(sessionID, true, false, null);
        if (sessionStatus.getLogs() == null || sessionStatus.getLogs().isEmpty()) {
            return "";
        }
        return String.join("\n", sessionStatus.getLogs());
    }

    @Override
    public byte[] requestPDFReport(String sessionID, String testCaseName) {
        if (sessionID != null) {
            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(baseItbUrl + "/api/rest/tests/report/" + sessionID))
                        .header("ITB_API_KEY", itbApiKey)
                        .header("Accept", "application/pdf")
                        .GET()
                        .build();
                try {
                    HttpResponse<byte[]> response = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
                    return response.body();
                } catch (IOException e) {
                    throw new ItbHttpClientException("Error request interrupted requesting PDF report. Try again.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ItbHttpClientException("Error request interrupted requesting PDF report. Try again.");
                }
            }
        }
        return new byte[0];
    }

    private ItbStartRequest withAsyncOptions(ItbStartRequest request) {
        return new ItbStartRequest(requireRequest(request))
                .setWaitForCompletion(null)
                .setMaximumWaitTime(null);
    }

    private ItbStartRequest withSyncOptions(ItbStartRequest request, long timeoutMs) {
        if (timeoutMs <= 0) {
            throw new ItbHttpClientException("Sync timeout must be greater than 0");
        }
        return new ItbStartRequest(requireRequest(request))
                .setWaitForCompletion(true)
                .setMaximumWaitTime(timeoutMs);
    }

    private ItbStartRequest requireRequest(ItbStartRequest request) {
        if (request == null) {
            throw new ItbHttpClientException("ITB start request cannot be null");
        }
        return request;
    }

    private ItbSessionStatusInformation getSessionStatusInformation(
            String sessionId,
            boolean withLogs,
            boolean withReports,
            Long timeoutMs
    ) {
        ItbStatusRequest statusRequest = new ItbStatusRequest()
                .setSession(List.of(sessionId))
                .setWithLogs(withLogs)
                .setWithReports(withReports);

        ItbStatusResponse statusResponse = sendPOSTRequest(
                baseItbUrl + "/api/rest/tests/status",
                statusRequest,
                timeoutMs,
                ItbStatusResponse.class
        );

        if (statusResponse.getSessions() == null || statusResponse.getSessions().isEmpty()) {
            throw new ItbHttpClientException(
                    "ITB status response does not contain sessions for session " + sessionId
            );
        }

        ItbSessionStatusInformation sessionStatus = statusResponse.getSessions().getFirst();
        if (sessionStatus.getSession() == null || sessionStatus.getSession().isBlank()) {
            throw new ItbHttpClientException("ITB status response does not contain valid session identifier");
        }
        return sessionStatus;
    }

    private ItbSessionCreationInformation extractFirstCreatedSession(ItbStartResponse response) {
        if (response.getCreatedSessions() == null || response.getCreatedSessions().isEmpty()) {
            throw new ItbHttpClientException("ITB response does not contain createdSessions.");
        }
        ItbSessionCreationInformation sessionInfo = response.getCreatedSessions().getFirst();
        if (sessionInfo.getSession() == null || sessionInfo.getSession().isBlank()) {
            throw new ItbHttpClientException("ITB response does not contain a valid session identifier.");
        }
        return sessionInfo;
    }

    private <T> T sendPOSTRequest(String uri, Object payload, Long timeoutMs, Class<T> responseType) {
        String body;
        try {
            body = mapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new ItbHttpClientException("Error parsing request body : " + e);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Sending ITB request payload: {}", body);
        }

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("ITB_API_KEY", itbApiKey)
                    .header("Content-Type", ItbHttpClient.APPLICATION_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(body));
            if (timeoutMs != null && timeoutMs > 0) {
                requestBuilder.timeout(Duration.ofMillis(timeoutMs));
            }
            HttpRequest req = requestBuilder.build();
            try {
                HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
                String responseBody = response.body();
                if (response.statusCode() >= 400) {
                    throw new ItbHttpClientException(
                            "ITB request error (status " + response.statusCode() + "): " + extractErrorMessage(responseBody)
                    );
                }
                return mapper.readValue(responseBody, responseType);
            } catch (IOException e) {
                logger.error("Error sending POST request", e);
                throw new ItbHttpClientException("Error request interrupted. Try again.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ItbHttpClientException("Error request interrupted. Try again.");
            }
        }
    }

    private String extractErrorMessage(String responseBody) {
        try {
            JsonNode content = mapper.readTree(responseBody);
            return content.path("error_description").asText(
                    content.path("message").asText(responseBody)
            );
        } catch (JsonProcessingException e) {
            return responseBody;
        }
    }

    private String extractTestCaseIdentifier(ItbStartRequest testRequest) {
        if (testRequest.getTestCase() == null || testRequest.getTestCase().isEmpty()) {
            return null;
        }
        return testRequest.getTestCase().getFirst();
    }
}
