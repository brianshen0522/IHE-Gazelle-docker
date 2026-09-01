package net.ihe.gazelle.validation.gateway.app.itmock;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class MockEvsServiceResource implements QuarkusTestResourceLifecycleManager {

    private static final DockerImageName SERVICE_REGISTRY_IMAGE = image("testcontainers.image.service-registry");
    private static final int SERVICE_REGISTRY_PORT = 8088;

    private static final String PROFILES_PATH = "/validator/rest/validation/v2/profiles";
    private static final String PROFILES_RESOURCE = "/v2/get-profiles-response.json";
    private static final String MAESTRO_TEST_RUN_PATH = "/v1/test/run";
    private static final String MAESTRO_TEST_REPORT = "/maestro/test-report.json";
    private static final String DATAHOUSE_ITEM_PATH = "/items/123";
    private static final String DATAHOUSE_ITEM = "/datahouse/item-report.json";
    private static final int KEYCLOAK_MOCK_PORT = 12345;
    private static final String KEYCLOAK_MOCK_HOST = "host.testcontainers.internal";
    private static final String MODE_ARG = "mode";
    private static final String DOWN_SERVICE_REGISTRY_URL = "http://localhost:1/service-registry";
    private static final String DOWN_MAESTRO_URL = "http://localhost:1";
    private static final String DOWN_DATAHOUSE_URL = "http://localhost:1";
    private static final String SERVICE_NOT_FOUND_REPORT = "/evs/error/maestro/service-not-found-test-report.json";
    private static final String MISSING_REPORT_LOCATION_TEST_REPORT =
          "/evs/error/maestro/missing-validation-report-test-report.json";
    private static final String INVALID_VALIDATION_REPORT_ITEM =
          "/evs/error/datahouse/invalid-validation-report-item.json";
    private static final String SERVICE_REGISTRY_TEMPLATE =
          "/itmock/service-registry/evs-services-template.json";

    private Mode mode = Mode.NORMAL;

    public MockEvsServiceResource() {
    }

    protected MockEvsServiceResource(String fixedMode) {
        this.mode = Mode.from(fixedMode);
    }

    private WireMockServer wireMockServer;
    private GenericContainer<?> serviceRegistryContainer;
    private Path servicesFile;

    @Override
    public void init(Map<String, String> initArgs) {
        if (initArgs == null) {
            return;
        }
        String configuredMode = initArgs.get(MODE_ARG);
        if (configuredMode != null && !configuredMode.isBlank()) {
            mode = Mode.from(configuredMode);
        }
    }

    protected final void setMode(String fixedMode) {
        this.mode = Mode.from(fixedMode);
    }

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        Testcontainers.exposeHostPorts(wireMockServer.port(), KEYCLOAK_MOCK_PORT);

        wireMockServer.stubFor(
              get(urlPathEqualTo(PROFILES_PATH))
                    .willReturn(aResponse()
                          .withStatus(200)
                          .withHeader("Content-Type", "application/json")
                          .withBody(readResource(PROFILES_RESOURCE)))
        );

        stubMaestroTestRun();
        stubDatahouseItem();

        wireMockServer.stubFor(
              get(urlPathEqualTo("/items"))
                    .withQueryParam("callerAlias.toolObjectId", matching(".+"))
                    .willReturn(aResponse()
                          .withStatus(200)
                          .withHeader("Content-Type", "application/json")
                          .withHeader("Content-Range", "item 0-0/1")
                          .withBody(readResource("/datahouse/search-items-response.json"))));
        wireMockServer.stubFor(
              put(urlPathEqualTo("/index-registry/"))
                    .willReturn(aResponse()
                          .withStatus(200)
                          .withHeader("Content-Type", "application/json")
                          .withBody("{\"status\":\"OK\"}")));

        String keycloakContainerBase = "http://" + KEYCLOAK_MOCK_HOST + ":" + KEYCLOAK_MOCK_PORT;
        servicesFile = writeServicesFile(wireMockServer.port());
        serviceRegistryContainer = new GenericContainer<>(SERVICE_REGISTRY_IMAGE)
              .withExposedPorts(SERVICE_REGISTRY_PORT)
              .withEnv("GZL_SERVICE_REGISTRY_FILE_PATH", "/opt/service-registry/services.json")
              .withEnv("GZL_SSO_URL", keycloakContainerBase)
              .withEnv("GZL_SSO_REALM", "gazelle")
              .withEnv("GZL_SSO_ADMIN_USER", "admin")
              .withEnv("GZL_SSO_ADMIN_PASSWORD", "admin")
              .withEnv("GZL_M2M_CLIENT_SECRET", "secret")
              .withEnv("GZL_M2M_REGISTRATION_STARTUP_ENABLED", "false")
              .withEnv("MP_JWT_VERIFY_ISSUER", "https://localhost:12345")
              .withEnv("MP_JWT_VERIFY_PUBLICKEY_LOCATION",
                    keycloakContainerBase + "/realms/gazelle/protocol/openid-connect/public.pem")
              .withCopyFileToContainer(MountableFile.forHostPath(servicesFile),
                    "/opt/service-registry/services.json")
              .waitingFor(Wait.forListeningPort());
        serviceRegistryContainer.start();

        return Map.ofEntries(
              Map.entry("validation.service.url", wireMockServer.baseUrl() + "/validator/rest"),
              Map.entry("maestro.base-url", mode == Mode.MAESTRO_DOWN ? DOWN_MAESTRO_URL : wireMockServer.baseUrl()),
              Map.entry("maestro.m2m.k8s-id-variable-name", "TEST_K8S_ID"),
              Map.entry("datahouse.url", mode == Mode.DATAHOUSE_DOWN ? DOWN_DATAHOUSE_URL : wireMockServer.baseUrl()),
              Map.entry("datahouse.m2m.k8s-id-variable-name", "TEST_K8S_ID"),
              Map.entry("datahouse.indexes.enabled", "false"),
              Map.entry("gzl.sso.url", "http://localhost:12345"),
              Map.entry("gzl.sso.realm", "gazelle"),
              Map.entry("gzl.sso.admin.user", "admin"),
              Map.entry("gzl.sso.admin.password", "admin"),
              Map.entry("gzl.m2m.client.secret", "secret"),
              Map.entry("gzl.jwt.verify.audience", "http://localhost"),
              Map.entry("gzl.service.name", "validation-gateway-evs-api"),
              Map.entry("gzl.k8s.id", "validation-gateway-evs-api-12345"),
              Map.entry("gzl.service.registry.url",
                    mode == Mode.SERVICE_REGISTRY_DOWN
                          ? DOWN_SERVICE_REGISTRY_URL
                          : "http://localhost:" + serviceRegistryContainer.getMappedPort(SERVICE_REGISTRY_PORT)
                                + "/service-registry"),
              Map.entry("gzl.service.registry.enabled", "false")
        );
    }

    @Override
    public void stop() {
        if (serviceRegistryContainer != null) {
            serviceRegistryContainer.stop();
        }
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
        if (servicesFile != null) {
            try {
                Files.deleteIfExists(servicesFile);
            } catch (IOException ignored) {
                // Best-effort cleanup for temp services file.
            }
        }
    }

    private String readResource(String path) {
        try (InputStream inputStream = MockEvsServiceResource.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing resource: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read resource: " + path, e);
        }
    }

    private Path writeServicesFile(int port) {
        String content = readResource(SERVICE_REGISTRY_TEMPLATE)
              .replace("{{validatorPort}}", String.valueOf(port));
        try {
            Path file = Files.createTempFile("service-registry", ".json");
            Files.writeString(file, content, StandardCharsets.UTF_8);
            return file;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create services.json for service registry", e);
        }
    }

    private void stubMaestroTestRun() {
        String body = switch (mode) {
            case SERVICE_NOT_FOUND -> readResource(SERVICE_NOT_FOUND_REPORT);
            case MISSING_VALIDATION_REPORT_IN_TEST_REPORT -> readResource(MISSING_REPORT_LOCATION_TEST_REPORT);
            default -> readResource(MAESTRO_TEST_REPORT);
        };
        wireMockServer.stubFor(
              post(urlPathEqualTo(MAESTRO_TEST_RUN_PATH))
                    .willReturn(aResponse()
                          .withStatus(200)
                          .withHeader("Content-Type", "application/json")
                          .withBody(body))
        );
    }

    private void stubDatahouseItem() {
        String itemBody = mode == Mode.INVALID_VALIDATION_REPORT_JSON
              ? readResource(INVALID_VALIDATION_REPORT_ITEM)
              : readResource(DATAHOUSE_ITEM);
        wireMockServer.stubFor(
              get(urlPathEqualTo(DATAHOUSE_ITEM_PATH))
                    .willReturn(aResponse()
                          .withStatus(200)
                          .withHeader("Content-Type", "application/json")
                          .withBody(itemBody))
        );
    }

    private enum Mode {
        NORMAL,
        SERVICE_NOT_FOUND,
        SERVICE_REGISTRY_DOWN,
        DATAHOUSE_DOWN,
        MAESTRO_DOWN,
        MISSING_VALIDATION_REPORT_IN_TEST_REPORT,
        INVALID_VALIDATION_REPORT_JSON;

        static Mode from(String value) {
            if (value == null || value.isBlank()) {
                return NORMAL;
            }
            return Mode.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        }
    }

    private static DockerImageName image(String property) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required system property: " + property);
        }
        return DockerImageName.parse(value);
    }
}
