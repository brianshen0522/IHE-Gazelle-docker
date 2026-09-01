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

package net.ihe.gazelle.itb.gateway.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import net.ihe.gazelle.itb.gateway.business.ItbSessionStore;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbReporting;
import net.ihe.gazelle.itb.gateway.technical.client.ItbHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

abstract class ItbWireMockITSupport {

    protected static final String API_KEY = "test-api-key";

    protected WireMockServer wireMock;
    protected ItbHttpClient itbHttpClient;

    @BeforeEach
    void setUpWireMock() {
        wireMock = new WireMockServer(options().dynamicPort());
        wireMock.start();
        itbHttpClient = new ItbHttpClient(wireMock.baseUrl(), API_KEY);
    }

    @AfterEach
    void tearDownWireMock() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }

    protected void stubStartSuccess(String sessionId) {
        ItbWireMockStubs.stubStartSuccess(wireMock, sessionId);
    }

    protected void stubStartSyncSuccess(String sessionId) {
        ItbWireMockStubs.stubStartSyncSuccess(wireMock, sessionId);
    }

    protected void stubStatusLogs(String sessionId, String... logs) {
        ItbWireMockStubs.stubStatusLogs(wireMock, sessionId, logs);
    }

    protected void stubStatusReport(String sessionId, String report) {
        ItbWireMockStubs.stubStatusReport(wireMock, sessionId, report);
    }

    protected void stubStatusReportAndLogs(String sessionId, String report, String... logs) {
        ItbWireMockStubs.stubStatusReportAndLogs(wireMock, sessionId, report, logs);
    }

    protected void stubPdfReportSuccess(String sessionId, byte[] pdfContent) {
        ItbWireMockStubs.stubPdfReportSuccess(wireMock, sessionId, pdfContent);
    }

    protected ItbSessionStore inMemorySessionStore() {
        return new LocalItbSessionStore();
    }

    private static class LocalItbSessionStore implements ItbSessionStore {

        private final Map<String, CompletableFuture<ItbReporting>> futures = new ConcurrentHashMap<>();

        @Override
        public CompletableFuture<ItbReporting> get(String sessionId) {
            return futures.get(sessionId);
        }

        @Override
        public void add(String sessionId, CompletableFuture<ItbReporting> future) {
            futures.put(sessionId, future);
        }

        @Override
        public void remove(String sessionId) {
            futures.remove(sessionId);
        }
    }
}
