package net.ihe.gazelle.validation.gateway.app.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class GatewayE2ETestResource implements QuarkusTestResourceLifecycleManager {

    private static final DockerImageName SERVICE_REGISTRY_IMAGE = image("testcontainers.image.service-registry");
    private static final int SERVICE_REGISTRY_PORT = 8088;

    private static final String PROFILES_PATH = "/validator/rest/validation/v2/profiles";
    private static final String PROFILES_RESOURCE = "/v2/get-profiles-response.json";

    private WireMockServer wireMockServer;
    private GenericContainer<?> serviceRegistryContainer;
    private Path servicesFile;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().bindAddress("0.0.0.0"));
        wireMockServer.start();
        Testcontainers.exposeHostPorts(wireMockServer.port(), 12345);
        wireMockServer.stubFor(
              get(urlPathEqualTo(PROFILES_PATH))
                    .willReturn(aResponse()
                          .withStatus(200)
                          .withHeader("Content-Type", "application/json")
                          .withBody(readResource(PROFILES_RESOURCE)))
        );

        String dockerHostIp = resolveDockerHostIp();
        String keycloakContainerBase = "http://host.testcontainers.internal:12345";
        servicesFile = writeServicesFile(dockerHostIp, wireMockServer.port());

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
              .waitingFor(Wait.forHttp("/service-registry")
                    .forStatusCodeMatching(status -> status < 500)
                    .withStartupTimeout(Duration.ofMinutes(2)));
        serviceRegistryContainer.start();

        return Map.ofEntries(
              Map.entry("gzl.service.registry.url",
                    "http://localhost:" + serviceRegistryContainer.getMappedPort(SERVICE_REGISTRY_PORT) + "/service-registry"),
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

    private Path writeServicesFile(String hostIp, int port) {
        String content = """
              [
                {
                  "name": "mock-validation-service",
                  "version": "1.0.0",
                  "instanceId": "mock-1",
                  "replicaId": "001",
                  "description": "Mock validation service",
                  "providedInterfaces": [
                    {
                      "interfaceName": "Validation Service API",
                      "interfaceVersion": "2.0.0",
                      "bindings": [
                        {
                          "@type": "REST",
                          "serviceUrl": "http://%s:%d/validator/rest"
                        }
                      ]
                    }
                  ]
                }
              ]
              """.formatted(hostIp, port);
        try {
            Path file = Files.createTempFile("service-registry", ".json");
            Files.writeString(file, content, StandardCharsets.UTF_8);
            return file;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create services.json for service registry", e);
        }
    }

    private String readResource(String path) {
        try (InputStream inputStream = GatewayE2ETestResource.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing resource: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read resource: " + path, e);
        }
    }

    private String resolveDockerHostIp() {
        String ip = DockerClientFactory.instance().dockerHostIpAddress();
        if (!"localhost".equals(ip) && !"127.0.0.1".equals(ip)) {
            return ip;
        }
        try {
            NetworkInterface dockerInterface = NetworkInterface.getByName("docker0");
            if (dockerInterface == null) {
                return ip;
            }
            for (InetAddress address : java.util.Collections.list(dockerInterface.getInetAddresses())) {
                if (address instanceof Inet4Address) {
                    return address.getHostAddress();
                }
            }
        } catch (Exception ignored) {
            // Best-effort fallback to docker host IP.
        }
        return ip;
    }

    private static DockerImageName image(String property) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required system property: " + property);
        }
        return DockerImageName.parse(value);
    }
}
