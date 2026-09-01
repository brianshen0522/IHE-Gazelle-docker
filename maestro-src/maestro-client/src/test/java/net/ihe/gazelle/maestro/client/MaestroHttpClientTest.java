package net.ihe.gazelle.maestro.client;

import net.ihe.gazelle.maestro.api.business.InvalidTestRunException;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MaestroHttpClientTest {

    @Test
    void executeTestAsyncIncludesCallback() throws Exception {
        URI callback = new URI("http://callback.example/report?session=abc");
        RecordingHttpClient recordingClient = new RecordingHttpClient(stubResponse(202, ""));
        MaestroHttpClient client = new MaestroHttpClient(
              new URI("http://maestro"),
              recordingClient,
              MaestroHttpClient.DEFAULT_CONNECT_TIMEOUT_MILLISECONDS,
              MaestroHttpClient.DEFAULT_READ_TIMEOUT_MILLISECONDS
        );

        client.executeTestAsync(simpleTestRun(), true, callback);

        HttpUriRequest recorded = recordingClient.getRecordedRequest();
        assertNotNull(recorded);
        assertEquals("POST", recorded.getMethod());
        String uri = recorded.getURI().toString();
        assertTrue(uri.contains("persist=true"));
        assertTrue(uri.contains("callback=" + URLEncoder.encode(callback.toString(), StandardCharsets.UTF_8)));
    }

    @Test
    void executeTestAsyncRejectsNullCallback() {
        RecordingHttpClient recordingClient = new RecordingHttpClient(stubResponse(202, ""));
        MaestroHttpClient client = new MaestroHttpClient(
              URI.create("http://maestro"),
              recordingClient,
              MaestroHttpClient.DEFAULT_CONNECT_TIMEOUT_MILLISECONDS,
              MaestroHttpClient.DEFAULT_READ_TIMEOUT_MILLISECONDS
        );
        TestRun testRun = simpleTestRun();

        assertThrows(IllegalArgumentException.class, () -> client.executeTestAsync(testRun, true, null));
    }

    @Test
    void executeTestAsyncPropagatesApplicationError() throws Exception {
        RecordingHttpClient recordingClient = new RecordingHttpClient(stubResponse(400, "invalid"));
        MaestroHttpClient client = new MaestroHttpClient(
              new URI("http://maestro"),
              recordingClient,
              MaestroHttpClient.DEFAULT_CONNECT_TIMEOUT_MILLISECONDS,
              MaestroHttpClient.DEFAULT_READ_TIMEOUT_MILLISECONDS
        );
        TestRun testRun = simpleTestRun();
        URI callback = new URI("http://callback");

        assertThrows(InvalidTestRunException.class, () -> client.executeTestAsync(testRun, true, callback));
    }

    private static HttpResponse stubResponse(int statusCode, String body) {
        BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, statusCode, "Reason");
        response.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        return response;
    }

    private static final class RecordingHttpClient implements HttpClient {

        private final HttpResponse response;
        private HttpUriRequest recordedRequest;

        private RecordingHttpClient(HttpResponse response) {
            this.response = response;
        }

        private HttpUriRequest getRecordedRequest() {
            return recordedRequest;
        }

        @Override
        public HttpResponse execute(HttpUriRequest request) throws IOException {
            this.recordedRequest = request;
            return response;
        }

        @Override
        public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException {
            return execute(request);
        }

        @Override
        public HttpResponse execute(HttpHost target, HttpRequest request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        @SuppressWarnings("java:S1874")
        public org.apache.http.conn.ClientConnectionManager getConnectionManager() {
            throw new UnsupportedOperationException();
        }

        @Override
        @SuppressWarnings("java:S1874")
        public org.apache.http.params.HttpParams getParams() {
            throw new UnsupportedOperationException();
        }

    }

    private static TestRun simpleTestRun() {
        AccessControlList acl = new AccessControlList()
              .setOwners(Set.of("owner"))
              .setReaders(Set.of("owner"))
              .setEditors(Set.of("owner"))
              .setPublic(true);
        return new TestRun()
              .setTest(new net.ihe.gazelle.maestro.api.business.test.Test()
                    .setId("test-1")
                    .setName("Test 1"))
              .setAccessControlList(acl);
    }
}
