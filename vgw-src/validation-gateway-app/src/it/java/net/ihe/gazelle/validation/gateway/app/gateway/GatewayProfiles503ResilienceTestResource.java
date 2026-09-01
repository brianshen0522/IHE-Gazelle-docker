package net.ihe.gazelle.validation.gateway.app.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class GatewayProfiles503ResilienceTestResource implements QuarkusTestResourceLifecycleManager {
   private static final String PROFILES_PATH = "/validator/rest/validation/v2/profiles";
   private static final String PROFILES_RESOURCE = "/v2/get-profiles-response.json";
   private static final String MBV_PATH = "/mbv/ModelBasedValidationWSService/ModelBasedValidationWS";
   private static final String MBV_RESOURCE = "/model-based/get-list-validators-response.xml";

   private static volatile WireMockServer sharedWireMockServer;

   @Override
   public Map<String, String> start() {
      WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
      wireMockServer.start();
      sharedWireMockServer = wireMockServer;
      configureHealthyProfilesResponse();

      return Map.of(
            "validation.service.url",
            wireMockServer.baseUrl() + "/validator/rest",
            "validation.service.mb.url",
            wireMockServer.baseUrl() + "/mbv/ModelBasedValidationWSService/ModelBasedValidationWS?wsdl",
            "gzl.service.registry.url",
            "http://localhost:0/service-registry",
            "gzl.service.registry.enabled",
            "false"
      );
   }

   @Override
   public void stop() {
      if (sharedWireMockServer != null) {
         sharedWireMockServer.stop();
         sharedWireMockServer = null;
      }
   }

   static void configureHealthyProfilesResponse() {
      WireMockServer wireMockServer = requireWireMockServer();
      wireMockServer.resetAll();
      stubModelBasedProfiles(wireMockServer);
      wireMockServer.stubFor(
            get(urlPathEqualTo(PROFILES_PATH))
                  .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readResource(PROFILES_RESOURCE)))
      );
   }

   static void configureDelayed503ProfilesResponse(int delayMillis) {
      WireMockServer wireMockServer = requireWireMockServer();
      wireMockServer.resetAll();
      stubModelBasedProfiles(wireMockServer);
      wireMockServer.stubFor(
            get(urlPathEqualTo(PROFILES_PATH))
                  .willReturn(aResponse()
                        .withFixedDelay(delayMillis)
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"service unavailable\"}"))
      );
   }

   static void resetRequestJournal() {
      requireWireMockServer().resetRequests();
   }

   static int countProfileRequests() {
      return (int) requireWireMockServer().getAllServeEvents().stream()
            .filter(event -> PROFILES_PATH.equals(stripQuery(event.getRequest().getUrl())))
            .count();
   }

   private static WireMockServer requireWireMockServer() {
      if (sharedWireMockServer == null) {
         throw new IllegalStateException("WireMock server not started");
      }
      return sharedWireMockServer;
   }

   private static void stubModelBasedProfiles(WireMockServer wireMockServer) {
      wireMockServer.stubFor(
            post(urlEqualTo(MBV_PATH))
                  .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml; charset=utf-8")
                        .withBody(readResource(MBV_RESOURCE)))
      );
   }

   private static String stripQuery(String url) {
      int queryIndex = url.indexOf('?');
      return queryIndex >= 0 ? url.substring(0, queryIndex) : url;
   }

   private static String readResource(String path) {
      try (InputStream inputStream = GatewayProfiles503ResilienceTestResource.class.getResourceAsStream(path)) {
         if (inputStream == null) {
            throw new IllegalStateException("Missing resource: " + path);
         }
         return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      } catch (IOException e) {
         throw new IllegalStateException("Failed to read resource: " + path, e);
      }
   }

}
