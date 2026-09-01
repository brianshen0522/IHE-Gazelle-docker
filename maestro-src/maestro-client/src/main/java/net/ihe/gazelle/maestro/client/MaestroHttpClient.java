package net.ihe.gazelle.maestro.client;

import net.ihe.gazelle.lang.UnexpectedInternalErrorException;
import net.ihe.gazelle.m2m.client.technical.filter.apache.ApacheM2MHttpClientBuilder;
import net.ihe.gazelle.maestro.api.business.InvalidTestRunException;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.maestro.api.business.testrun.TestSuiteRun;
import net.ihe.gazelle.maestro.api.technical.dto.report.TestReportDTO;
import net.ihe.gazelle.maestro.api.technical.dto.testrun.TestRunDTO;
import net.ihe.gazelle.maestro.api.technical.dto.testrun.TestSuiteRunDTO;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A concrete implementation of the {@link MaestroClient} interface that provides functionality for interacting
 * with the Maestro service via HTTP. This client supports running tests and test suites both synchronously and
 * asynchronously, and provides methods for handling technical errors that may occur during HTTP communication.
 */
public final class MaestroHttpClient implements MaestroClient {

   /**
    * The endpoint URL for triggering the execution of a single test run.
    */
   public static final String TEST_RUN_ENDPOINT = "/v1/test/run";

   /**
    * Endpoint for triggering the execution of a test suite.
    */
   public static final String TEST_SUITE_RUN_ENDPOINT = "/v1/test-suite/run";

   /**
    * Represents the content type header value used for HTTP requests.
    */
   public static final String CONTENT_TYPE = "application/json";

   /**
    * The default timeout value in milliseconds for establishing an HTTP connection.
    */
   public static final int DEFAULT_CONNECT_TIMEOUT_MILLISECONDS = 10000;

   /**
    * The default read timeout value for HTTP requests, measured in milliseconds.
    */
   public static final int DEFAULT_READ_TIMEOUT_MILLISECONDS = 120000;

   private static final TextSerDes serDes = new JacksonSerDes();

   private final URI baseUri;
   private final HttpClient client;
   private final int msConnectTimeout;
   private final int msReadTimeout;

   /**
    * Constructs a new {@code MaestroHttpClient} instance using the specified base URI and Kubernetes ID variable name.
    * This constructor applies default values for connection and read timeouts.
    *
    * @param baseUri the base URI for the HTTP client; must not be null
    * @param k8sIdVariableName the Kubernetes ID variable name to use for access token retrieval; must not be null or blank
    */
   public MaestroHttpClient(URI baseUri, String k8sIdVariableName) {
      this(baseUri, k8sIdVariableName, DEFAULT_CONNECT_TIMEOUT_MILLISECONDS, DEFAULT_READ_TIMEOUT_MILLISECONDS);
   }

   /**
    * Constructs a new {@code MaestroHttpClient} instance with the specified parameters.
    * This constructor allows customization of connection and read timeouts.
    *
    * @param baseUri the base URI for the HTTP client; must not be null
    * @param k8sIdVariableName the Kubernetes ID variable name to use for access token retrieval; must not be null or blank
    * @param msConnectTimeout the connection timeout in milliseconds; a non-negative value
    * @param msReadTimeout the read timeout in milliseconds; a non-negative value
    */
   public MaestroHttpClient(URI baseUri, String k8sIdVariableName, int msConnectTimeout, int msReadTimeout) {
      this(
            requireBaseUri(baseUri),
            buildM2mHttpClient(requireK8sId(k8sIdVariableName), msConnectTimeout, msReadTimeout),
            msConnectTimeout,
            msReadTimeout
      );
   }

   MaestroHttpClient(URI baseUri, HttpClient client, int msConnectTimeout, int msReadTimeout) {
      if (baseUri == null) {
         throw new IllegalArgumentException("baseUri must not be null");
      }
      this.baseUri = baseUri;
      this.client = Objects.requireNonNull(client, "client must not be null");
      this.msConnectTimeout = msConnectTimeout;
      this.msReadTimeout = msReadTimeout;
   }


   @Override
   public TestReport executeTest(TestRun testRun, boolean persist) {
      return handleTechnicalErrors(() -> doExecuteTest(testRun, persist));
   }

   @Override
   public TestReport executeTestSuite(TestSuiteRun testSuiteRun, boolean persist) {
      return handleTechnicalErrors(() -> doExecuteTestSuite(testSuiteRun, persist));
   }

   @Override
   public void executeTestAsync(TestRun testRun, boolean persist, URI callback) {
      handleTechnicalErrors(() -> {
         doExecuteTestAsync(testRun, persist, callback);
         return null;
      });
   }

   @Override
   public void executeTestSuiteAsync(TestSuiteRun testSuiteRun, boolean persist, URI callback) {
      handleTechnicalErrors(() -> {
         doExecuteTestSuiteAsync(testSuiteRun, persist, callback);
         return null;
      });
   }

   private TestReport doExecuteTest(TestRun testRun, boolean persist) {
      String url = buildUrl(TEST_RUN_ENDPOINT, persist);
      TestRunDTO dto = new TestRunDTO(testRun);
      String payload = serDes.serializeAsString(dto);
      return executePost(url, payload);
   }

   private static URI requireBaseUri(URI baseUri) {
      if (baseUri == null) {
         throw new IllegalArgumentException("baseUri must not be null");
      }
      return baseUri;
   }

   private static String requireK8sId(String k8sIdVariableName) {
      if (k8sIdVariableName == null || k8sIdVariableName.isBlank()) {
         throw new IllegalArgumentException("k8sIdVariableName must not be null or blank");
      }
      return k8sIdVariableName;
   }


   private TestReport doExecuteTestSuite(TestSuiteRun testSuiteRun, boolean persist) {
      String url = buildUrl(TEST_SUITE_RUN_ENDPOINT, persist);
      TestSuiteRunDTO dto = new TestSuiteRunDTO(testSuiteRun);
      String payload = serDes.serializeAsString(dto);
      return executePost(url, payload);
   }

   private void doExecuteTestAsync(TestRun testRun, boolean persist, URI callback) {
      String url = buildUrl(TEST_RUN_ENDPOINT, persist) + buildCallbackParameter(callback);
      TestRunDTO dto = new TestRunDTO(testRun);
      String payload = serDes.serializeAsString(dto);
      executePostAsync(url, payload);
   }

   private void doExecuteTestSuiteAsync(TestSuiteRun testSuiteRun, boolean persist, URI callback) {
      String url = buildUrl(TEST_SUITE_RUN_ENDPOINT, persist) + buildCallbackParameter(callback);
      TestSuiteRunDTO dto = new TestSuiteRunDTO(testSuiteRun);
      String payload = serDes.serializeAsString(dto);
      executePostAsync(url, payload);
   }

   private TestReport executePost(String url, String payload) {
      try {
         HttpPost httpPost = createHttpPost(url, payload);
         HttpResponse response = client.execute(httpPost);
         String body = readBody(response);
         return handleApplicationError(response, body);
      } catch (IOException e) {
         throw new MaestroConnectionException("Unable to execute request to Maestro at " + url, e);
      }
   }

   private void executePostAsync(String url, String payload) {
      try {
         HttpPost httpPost = createHttpPost(url, payload);
         HttpResponse response = client.execute(httpPost);
         String body = readBody(response);
         handleAsyncApplicationError(response, body);
      } catch (IOException e) {
         throw new MaestroConnectionException("Unable to execute request to Maestro at " + url, e);
      }
   }

   private TestReport handleApplicationError(HttpResponse response, String body) {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 200) {
         return serDes.deserialize(body, TestReportDTO.class).getBusinessObject();
      }
      return handleError(body, statusCode);
   }

   private TestReport handleError(String body, int statusCode) {
      if (statusCode == 400) {
         throw new InvalidTestRunException(body != null ? body : "Invalid test run");
      }
      if (statusCode == 401) {
         throw new MaestroHttpError(statusCode, body);
      }
      if (statusCode >= 400) {
         throw new MaestroHttpError(statusCode, body);
      }
      throw new UnexpectedInternalErrorException("Unexpected response from Maestro: " + statusCode);
   }

   private void handleAsyncApplicationError(HttpResponse response, String body) {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 200 || statusCode == 202) {
         return;
      }
      handleError(body, statusCode);
   }

   private HttpPost createHttpPost(String url, String payload) {
      HttpPost httpPost = new HttpPost(url);
      RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(msConnectTimeout)
            .setSocketTimeout(msReadTimeout)
            .build();
      httpPost.setConfig(requestConfig);
      StringEntity input = new StringEntity(payload, StandardCharsets.UTF_8);
      input.setContentType(CONTENT_TYPE);
      httpPost.addHeader("Content-type", CONTENT_TYPE);
      httpPost.setEntity(input);
      return httpPost;
   }

   private String buildUrl(String endpoint, boolean persist) {
      return baseUri.toString() + endpoint + "?persist=" + persist;
   }

   private String buildCallbackParameter(URI callback) {
      validateCallback(callback);
      return "&callback=" + URLEncoder.encode(callback.toString(), StandardCharsets.UTF_8);
   }

   private void validateCallback(URI callback) {
      if (callback == null) {
         throw new IllegalArgumentException("callback must not be null");
      }
      String value = callback.toString();
      if (value == null || value.isBlank()) {
         throw new IllegalArgumentException("callback must not be blank");
      }
   }

   private <T> T handleTechnicalErrors(ExceptionalSupplier<T> operation) {
      try {
         return operation.run();
      } catch (MaestroConnectionException e) {
         if (e.getCause() instanceof SocketTimeoutException) {
            throw new MaestroTimeoutException(
                  String.format("Maestro did not respond before timeout (connectTimeout=%d, readTimeout=%d).",
                        msConnectTimeout, msReadTimeout),
                  e);
         }
         throw e;
      }
   }

   private static String readBody(HttpResponse response) throws IOException {
      if (response.getEntity() == null) {
         return "";
      }
      try (InputStream inputStream = response.getEntity().getContent()) {
         return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      }
   }

   private static CloseableHttpClient buildM2mHttpClient(String k8sIdVariableName,
                                                         int msConnectTimeout,
                                                         int msReadTimeout) {
      RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(msConnectTimeout)
            .setSocketTimeout(msReadTimeout)
            .build();
      return ApacheM2MHttpClientBuilder.createWithAccessToken(k8sIdVariableName)
            .setDefaultRequestConfig(requestConfig)
            .build();
   }

   @FunctionalInterface
   private interface ExceptionalSupplier<T> {
      T run();
   }
}
