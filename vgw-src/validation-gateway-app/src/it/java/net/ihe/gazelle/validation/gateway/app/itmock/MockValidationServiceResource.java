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
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class MockValidationServiceResource implements QuarkusTestResourceLifecycleManager {

    private static final DockerImageName SERVICE_REGISTRY_IMAGE = image("testcontainers.image.service-registry");
    private static final int SERVICE_REGISTRY_PORT = 8088;

    private static final String PROFILES_PATH = "/validator/rest/validation/v2/profiles";
    private static final String PROFILES_RESOURCE = "/v2/get-profiles-response.json";
    private static final String MBV_PATH = "/mbv/ModelBasedValidationWSService/ModelBasedValidationWS";
    private static final String MBV_RESOURCE = "/model-based/get-list-validators-response.xml";
    private static final String SERVICE_REGISTRY_TEMPLATE =
          "/itmock/service-registry/validation-services-template.json";
    private static final int KEYCLOAK_MOCK_PORT = 12345;
    private static final String KEYCLOAK_MOCK_HOST = "host.testcontainers.internal";

    private WireMockServer wireMockServer;
    private GenericContainer<?> serviceRegistryContainer;
    private Path servicesFile;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        Testcontainers.exposeHostPorts(wireMockServer.port(), KEYCLOAK_MOCK_PORT);

        wireMockServer.stubFor(
                get(urlEqualTo(PROFILES_PATH))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(readResource(PROFILES_RESOURCE)))
        );
        wireMockServer.stubFor(
                com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo(MBV_PATH))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "text/xml; charset=utf-8")
                                .withBody(readResource(MBV_RESOURCE)))
        );

        servicesFile = writeServicesFile(wireMockServer.port());
        String keycloakContainerBase = "http://" + KEYCLOAK_MOCK_HOST + ":" + KEYCLOAK_MOCK_PORT;
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

        return Map.of(
                "validation.service.url", wireMockServer.baseUrl() + "/validator/rest",
                "validation.service.mb.url",
                wireMockServer.baseUrl() + "/mbv/ModelBasedValidationWSService/ModelBasedValidationWS?wsdl",
                "gzl.service.registry.url",
                "http://localhost:" + serviceRegistryContainer.getMappedPort(SERVICE_REGISTRY_PORT)
                      + "/service-registry",
                "gzl.service.registry.enabled",
                "false"
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
        try (InputStream inputStream = MockValidationServiceResource.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing resource: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read resource: " + path, e);
        }
    }

    private Path writeServicesFile(int port) {
        String portValue = String.valueOf(port);
        String content = readResource(SERVICE_REGISTRY_TEMPLATE)
              .replace("{{validatorPort}}", portValue)
              .replace("{{mbvPort}}", portValue);
        try {
            Path file = Files.createTempFile("service-registry", ".json");
            Files.writeString(file, content, StandardCharsets.UTF_8);
            return file;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create services.json for service registry", e);
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
