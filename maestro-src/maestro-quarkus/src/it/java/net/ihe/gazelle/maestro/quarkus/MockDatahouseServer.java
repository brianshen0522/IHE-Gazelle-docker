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

package net.ihe.gazelle.maestro.quarkus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockDatahouseServer {

    private static MockWebServer server;
    private static final ObjectMapper mapper = new ObjectMapper();

    // heuristics patterns
    private static final Pattern filenamePattern = Pattern.compile("filename=\"");
    private static final Pattern contentDispPattern = Pattern.compile("Content-Disposition:", Pattern.CASE_INSENSITIVE);

    private static final AtomicBoolean started = new AtomicBoolean(false);

    private MockDatahouseServer() {
        // utility holder
    }

    public static synchronized void start(int port) throws IOException {
        if (started.compareAndSet(false, true)) {
            server = new MockWebServer();
            server.start(port);
            server.setDispatcher(createDispatcher());
        }
    }

    public static synchronized void stop() throws IOException {
        if (started.compareAndSet(true, false)) {
            server.shutdown();
            server = null;
        }
    }

    public static String baseUrl() {
        return server.url("/").toString();
    }

    private static Dispatcher createDispatcher() {
        return new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();
                String method = request.getMethod();

                // items endpoint: keep simple created response
                if ("/datahouse/rest/v1/items".equals(path) && "POST".equalsIgnoreCase(method)) {
                    return new MockResponse()
                            .setResponseCode(201)
                            .addHeader("Location", server.url("/datahouse/rest/v1/items/1234567890"))
                            .setBody("");
                }

                // attachments endpoint: dynamic
                if ("/datahouse/rest/v1/items/attachments".equals(path) && "POST".equalsIgnoreCase(method)) {
                    String body = request.getBody() != null ? request.getBody().readUtf8() : "";
                    int count = countAttachments(body);

                    List<String> ids = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        ids.add("attachment-" + i); // deterministic ids for easy assertions
                    }

                    String responseJson;
                    try {
                        responseJson = mapper.writeValueAsString(ids);
                    } catch (Exception e) {
                        responseJson = "[]";
                    }

                    return new MockResponse()
                            .setResponseCode(200)
                            .addHeader("Content-Type", "application/json")
                            .setBody(responseJson);
                }

                // fallback: 404
                return new MockResponse().setResponseCode(404);
            }
        };
    }

    private static int countAttachments(String body) {
        if (body == null || body.isEmpty()) return 0;

        // 1) try JSON parse
        try {
            JsonNode node = mapper.readTree(body);
            if (node.isArray()) {
                return node.size();
            } else if (node.has("attachments") && node.get("attachments").isArray()) {
                return node.get("attachments").size();
            }
        } catch (IOException ignored) {
            // not JSON -> continue heuristics
        }

        // 2) multipart heuristics
        Matcher m = filenamePattern.matcher(body);
        int filenameCount = 0;
        while (m.find()) filenameCount++;
        if (filenameCount > 0) return filenameCount;

        Matcher m2 = contentDispPattern.matcher(body);
        int dispCount = 0;
        while (m2.find()) dispCount++;
        if (dispCount > 0) return dispCount;

        // fallback: try to detect simple comma-separated tokens in the body (best-effort)
        // e.g. "a,b,c" => 3
        if (body.contains(",") && !body.contains("{")) {
            return (int) body.chars().filter(ch -> ch == ',').count() + 1;
        }

        return 0;
    }

    // convenience for tests
    public static String attachmentsEndpointUrl() {
        return server.url("/datahouse/rest/v1/items/attachments").toString();
    }

    public static String itemsEndpointUrl() {
        return server.url("/datahouse/rest/v1/items").toString();
    }
}
