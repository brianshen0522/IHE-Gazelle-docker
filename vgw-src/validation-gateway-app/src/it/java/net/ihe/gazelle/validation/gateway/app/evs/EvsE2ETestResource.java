package net.ihe.gazelle.validation.gateway.app.evs;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.slf4j.Logger;
import net.ihe.gazelle.security.mocks.OIDCJWTGenerator;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class EvsE2ETestResource implements QuarkusTestResourceLifecycleManager {

    private static final Logger logger = LoggerFactory.getLogger(EvsE2ETestResource.class);

    private static final DockerImageName MAESTRO_IMAGE = image("testcontainers.image.maestro");
    private static final DockerImageName DATAHOUSE_IMAGE = image("testcontainers.image.datahouse");
    private static final DockerImageName SERVICE_REGISTRY_IMAGE = image("testcontainers.image.service-registry");
    private static final DockerImageName MONGO_IMAGE = image("testcontainers.image.mongo");

    private static final int APP_PORT = 8082;
    private static final int MAESTRO_PORT = 8150;
    private static final int SERVICE_REGISTRY_PORT = 8088;

    private static final String PROFILES_PATH = "/validator/rest/validation/v2/profiles";
    private static final String VALIDATE_PATH = "/validator/rest/validation/v2/validate";
    private static final String PROFILES_RESOURCE = "/v2/get-profiles-response.json";
    private static final String VALIDATE_RESOURCE = "/v2/validate-response.json";
    private static final String TEST_REALM = "gazelle";
    private static final String ROOT_TEST_BED_URL = "http://localhost";
    private static final String TEST_SSO_ADMIN_USER = "gazelle-clients-admin";
    private static final String TEST_SSO_ADMIN_PASSWORD = "gazelle";
    private static final String TEST_CLIENT_SECRET = "CPT";
    private static final String DATAHOUSE_K8S_ID = "gazelle-datahouse-798445-12456";
    private static final String MAESTRO_K8S_ID = "gazelle-maestro-798445-12456";
    private static final String EVS_GATEWAY_K8S_ID = "gazelle-evs-gateway-798445-12456";
    private static final String KEYCLOAK_BASE_URL = "http://localhost:12345";
    private static final String KEYCLOAK_ISSUER = "https://localhost:12345";
    private static final int KEYCLOAK_MOCK_PORT = 12345;
    private static final String KEYCLOAK_MOCK_HOST = "host.testcontainers.internal";

    private WireMockServer wireMockServer;
    private Network network;
    private MongoDBContainer mongoContainer;
    private GenericContainer<?> datahouseContainer;
    private GenericContainer<?> maestroContainer;
    private GenericContainer<?> serviceRegistryContainer;
    private Path servicesFile;
    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().bindAddress("0.0.0.0"));
        wireMockServer.start();
        Testcontainers.exposeHostPorts(wireMockServer.port(), KEYCLOAK_MOCK_PORT);

        String dockerHostIp = resolveDockerHostIp();
        String keycloakContainerBase = "http://" + KEYCLOAK_MOCK_HOST + ":" + KEYCLOAK_MOCK_PORT;
        stubValidationService();
        servicesFile = writeServicesFile(wireMockServer.port());

        network = Network.newNetwork();

        serviceRegistryContainer = new GenericContainer<>(SERVICE_REGISTRY_IMAGE)
              .withNetwork(network)
              .withNetworkAliases("service-registry")
              .withExposedPorts(SERVICE_REGISTRY_PORT)
              .withEnv("GZL_SERVICE_REGISTRY_FILE_PATH", "/opt/service-registry/services.json")
              .withEnv("GZL_SSO_URL", keycloakContainerBase)
              .withEnv("GZL_SSO_REALM", TEST_REALM)
              .withEnv("GZL_SSO_ADMIN_USER", TEST_SSO_ADMIN_USER)
              .withEnv("GZL_SSO_ADMIN_PASSWORD", TEST_SSO_ADMIN_PASSWORD)
              .withEnv("GZL_M2M_CLIENT_SECRET", TEST_CLIENT_SECRET)
              .withEnv("GZL_M2M_REGISTRATION_STARTUP_ENABLED", "true")
              .withEnv("MP_JWT_VERIFY_ISSUER", KEYCLOAK_ISSUER)
              .withEnv("MP_JWT_VERIFY_PUBLICKEY_LOCATION",
                    keycloakContainerBase + "/realms/" + TEST_REALM + "/protocol/openid-connect/public.pem")
              .withEnv("GZL_SERVICE_NAME", "service-registry")
              .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("tc.service-registry")))

              .withCopyFileToContainer(MountableFile.forHostPath(servicesFile),
                    "/opt/service-registry/services.json")
              .waitingFor(Wait.forListeningPort()
                    .withStartupTimeout(Duration.ofMinutes(2)));
        serviceRegistryContainer.start();

        mongoContainer = new MongoDBContainer(MONGO_IMAGE)
              .withNetwork(network)
              .withNetworkAliases("mongo");
        mongoContainer.start();

        int mongoPort = mongoContainer.getMappedPort(27017);
        logger.info("MongoDB started at {}:{}", dockerHostIp, mongoPort);
        datahouseContainer = new GenericContainer<>(DATAHOUSE_IMAGE)
              .withNetwork(network)
              .withNetworkAliases("datahouse")
              .withExposedPorts(APP_PORT)
              .withEnv("MONGODB_CONNECTION_STRING", "mongodb://mongo:27017")
              .withEnv("MONGODB_DATABASE", "datahouse")
              .withCopyFileToContainer(
                    MountableFile.forClasspathResource("datahouse/index_configs.json"),
                    "/opt/datahouse/index_configs.json")
              .withEnv("MP_JWT_VERIFY_ISSUER", KEYCLOAK_ISSUER)
              .withEnv("MP_JWT_VERIFY_PUBLICKEY_LOCATION",
                    keycloakContainerBase + "/realms/" + TEST_REALM + "/protocol/openid-connect/public.pem")
              .withEnv("GZL_SSO_URL", keycloakContainerBase)
              .withEnv("GZL_SSO_REALM", TEST_REALM)
              .withEnv("GZL_SSO_ADMIN_USER", TEST_SSO_ADMIN_USER)
              .withEnv("GZL_SSO_ADMIN_PASSWORD", TEST_SSO_ADMIN_PASSWORD)
              .withEnv("GZL_JWT_VERIFY_AUDIENCE", ROOT_TEST_BED_URL)
              .withEnv("GZL_SERVICE_NAME", "datahouse")
              .withEnv("GZL_K8S_ID", DATAHOUSE_K8S_ID)
              .withEnv("GZL_DATAHOUSE_K8S_ID", DATAHOUSE_K8S_ID)
              .withEnv("TEST_K8S_ID", DATAHOUSE_K8S_ID)
              .withEnv("GZL_M2M_CLIENT_SECRET", TEST_CLIENT_SECRET)
              .withEnv("GZL_M2M_REGISTRATION_STARTUP_ENABLED", "true")
              .withEnv("GZL_SERVICE_REGISTRY_ENABLED", "false")
              .withEnv("GZL_EVS_GATEWAY_K8S_ID", EVS_GATEWAY_K8S_ID)
              .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("tc.datahouse")))
              .waitingFor(Wait.forListeningPort()
                    .withStartupTimeout(Duration.ofMinutes(2)));
        datahouseContainer.start();

        int datahousePort = datahouseContainer.getMappedPort(APP_PORT);
        logger.info("Datahouse started at {}:{}", dockerHostIp, datahousePort);
        int registryPort = serviceRegistryContainer.getMappedPort(SERVICE_REGISTRY_PORT);
        logger.info("Service Registry started at {}:{}", dockerHostIp, registryPort);
        maestroContainer = new GenericContainer<>(MAESTRO_IMAGE)
              .withNetwork(network)
              .withNetworkAliases("maestro")
              .withExposedPorts(MAESTRO_PORT)
              .withEnv("DATAHOUSE_URL", "http://datahouse:" + APP_PORT + "/datahouse/rest/v1")
              .withEnv("GZL_SERVICE_REGISTRY_URL", "http://service-registry:" + SERVICE_REGISTRY_PORT + "/service-registry")
              .withEnv("GZL_SSO_URL", keycloakContainerBase)
              .withEnv("GZL_SSO_REALM", TEST_REALM)
              .withEnv("GZL_SSO_ADMIN_USER", TEST_SSO_ADMIN_USER)
              .withEnv("GZL_SSO_ADMIN_PASSWORD", TEST_SSO_ADMIN_PASSWORD)
              .withEnv("MP_JWT_VERIFY_ISSUER", KEYCLOAK_ISSUER)
              .withEnv("MP_JWT_VERIFY_PUBLICKEY_LOCATION",
                    keycloakContainerBase + "/realms/" + TEST_REALM + "/protocol/openid-connect/public.pem")
              .withEnv("GZL_JWT_VERIFY_AUDIENCE", ROOT_TEST_BED_URL)
              .withEnv("GZL_SERVICE_NAME", "maestro")
              .withEnv("GZL_K8S_ID", MAESTRO_K8S_ID)
              .withEnv("GZL_MAESTRO_K8S_ID", MAESTRO_K8S_ID)
              .withEnv("TEST_K8S_ID", MAESTRO_K8S_ID)
              .withEnv("GZL_M2M_CLIENT_SECRET", TEST_CLIENT_SECRET)
              .withEnv("GZL_M2M_REGISTRATION_STARTUP_ENABLED", "true")
              .withEnv("GZL_SERVICE_REGISTRY_ENABLED", "true")
              .withEnv("GZL_EVS_GATEWAY_K8S_ID", EVS_GATEWAY_K8S_ID)
              .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("tc.maestro")))
              .waitingFor(Wait.forHttp("/maestro/health/ready")
                    .forPort(MAESTRO_PORT)
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(3)));
        maestroContainer.start();
        int maestroPort = maestroContainer.getMappedPort(MAESTRO_PORT);
        logger.info("Maestro started at {}:{}", dockerHostIp, maestroPort);

        String hostBaseUrl = "http://" + dockerHostIp;
        return Map.ofEntries(
              Map.entry("gzl.service.registry.url",
                    hostBaseUrl + ":" + registryPort + "/service-registry"),
              Map.entry("gzl.service.registry.enabled", "false"),
              Map.entry("maestro.base-url", hostBaseUrl + ":" + maestroContainer.getMappedPort(MAESTRO_PORT) + "/maestro"),
              Map.entry("maestro.m2m.enabled", "false"),
              Map.entry("maestro.auth.token", OIDCJWTGenerator.getValidJwtWithGroups(List.of("role:sut_operator"))),
              Map.entry("maestro.m2m.k8s-id-variable-name", "TEST_K8S_ID"),
              Map.entry("datahouse.url",
                    hostBaseUrl + ":" + datahousePort + "/datahouse/rest/v1"),
              Map.entry("datahouse.m2m.k8s-id-variable-name", "TEST_K8S_ID"),
              Map.entry("gzl.sso.url", KEYCLOAK_BASE_URL),
              Map.entry("gzl.sso.realm", TEST_REALM),
              Map.entry("gzl.sso.admin.user", TEST_SSO_ADMIN_USER),
              Map.entry("gzl.sso.admin.password", TEST_SSO_ADMIN_PASSWORD),
              Map.entry("gzl.k8s.id", "validation-gateway-evs-api-12345"),
              Map.entry("gzl.service.name", "validation-gateway-evs-api"),
              Map.entry("gzl.m2m.client.secret", TEST_CLIENT_SECRET),
              Map.entry("gzl.m2m.registration.startup.enabled", "false"),
              Map.entry("gzl.jwt.verify.audience", ROOT_TEST_BED_URL)
        );
    }

    @Override
    public void stop() {
        if (maestroContainer != null) {
            maestroContainer.stop();
        }
        if (datahouseContainer != null) {
            datahouseContainer.stop();
        }
        if (mongoContainer != null) {
            mongoContainer.stop();
        }
        if (serviceRegistryContainer != null) {
            serviceRegistryContainer.stop();
        }
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
        if (network != null) {
            network.close();
        }
        if (servicesFile != null) {
            try {
                Files.deleteIfExists(servicesFile);
            } catch (IOException ignored) {
                // Best-effort cleanup for temp services file.
            }
        }
    }

    private void stubValidationService() {
        wireMockServer.stubFor(
              get(urlPathEqualTo(PROFILES_PATH))
                    .willReturn(aResponse()
                          .withStatus(200)
                          .withHeader("Content-Type", "application/json")
                          .withBody(readResource(PROFILES_RESOURCE)))
        );
        wireMockServer.stubFor(
              post(urlPathEqualTo(VALIDATE_PATH))
                    .willReturn(aResponse()
                          .withStatus(200)
                          .withHeader("Content-Type", "application/json")
                          .withBody(readResource(VALIDATE_RESOURCE)))
        );
    }

    private Path writeServicesFile(int port) {
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
              """.formatted(KEYCLOAK_MOCK_HOST, port);
        try {
            Path file = Files.createTempFile("service-registry", ".json");
            Files.writeString(file, content, StandardCharsets.UTF_8);
            return file;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create services.json for service registry", e);
        }
    }

    private String readResource(String path) {
        try (InputStream inputStream = EvsE2ETestResource.class.getResourceAsStream(path)) {
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
