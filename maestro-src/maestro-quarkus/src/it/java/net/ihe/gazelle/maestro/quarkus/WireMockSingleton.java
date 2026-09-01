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

package net.ihe.gazelle.maestro.quarkus;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import net.ihe.gazelle.m2m.client.technical.KeycloakConfigurationProvider;
import net.ihe.gazelle.m2m.client.technical.configuration.SSOConfigurationProvider;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.api.technical.dto.report.TestReportDTO;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.servicemetadata.api.business.Binding;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.servicemetadata.technical.jaxrs.ServiceDTO;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static net.ihe.gazelle.security.mocks.OIDCJWTGenerator.getValidJwtWithGroups;
import static org.awaitility.Awaitility.await;

@Disabled
public class WireMockSingleton {

    private static WireMockServer wireMockServer;

    private static final SSOConfigurationProvider ssoConfigurationProvider = new KeycloakConfigurationProvider("GZL_SERVICE_K8S_ID");

    private WireMockSingleton() {
        // Private constructor to enforce Singleton pattern
    }

    public static WireMockServer getWireMockServer() {
        return wireMockServer;
    }

    public static synchronized WireMockServer startServer(int port) {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(port);
            wireMockServer.start();
        }
        return wireMockServer;
    }

    public static void mockGetProfiles() throws IOException {
        wireMockServer.stubFor(get(urlPathMatching("/matchboxv3/gazelle/validation/v2/profiles"))
                .willReturn(ok().withBody(getResourceAsString("/profiles.json")))
        );
    }

    public static void mockEvsProfiles() throws IOException {
        wireMockServer.stubFor(get(urlPathMatching("/evs/gazelle/validations/profiles"))
                .willReturn(ok().withBody(getResourceAsString("/evs-profiles.json")))
        );
    }

    public static void mockValidate() throws IOException {
        mockValidate("/response.json");
    }

    public static void mockValidate(String resourcePath) throws IOException {
        wireMockServer.stubFor(post(urlPathMatching("/matchboxv3/gazelle/validation/v2/validate"))
                .willReturn(ok().withBody(getResourceAsString(resourcePath)))
        );
    }

    public static void mockEvsValidate() throws IOException {
        wireMockServer.stubFor(post(urlPathMatching("/evs/gazelle/validations"))
                .willReturn(ok()
                        .withStatus(201)
                    .withHeader("Content-Type", "text/plain")
                        .withHeader("Location", "http://localhost:34500/evs/gazelle/validations/1.1.1.1.1.1.4.754")
                        .withBody("http://localhost:34500/evs/gazelle/validations/1.1.1.1.1.1.4.754"))
        );

        wireMockServer.stubFor(get(urlPathMatching("/evs/gazelle/validations/.*/report"))
                .willReturn(ok()
                    .withHeader("Content-Type", "application/json")
                    .withBody(getResourceAsString("/validation-response.xml")))
        );
    }

    public static void mockServiceRegistry() throws IOException {
        String servicesJson = getResourceAsString("/services.json");
        TextSerDes serDes = new JacksonSerDes();
        List<Service> services = Arrays.stream(serDes.deserialize(servicesJson, ServiceDTO[].class))
                .map(ServiceDTO::getBusinessObject)
                .toList();

        for (Service service : services) {
            for (ProvidedInterface providedInterface : service.getProvidedInterfaces()) {
                for (Binding binding : providedInterface.getBindings()) {
                    if (binding instanceof HttpRestBinding restBinding) {
                        restBinding.setServiceUrl(restBinding.getServiceUrl().replace(":port", ":" + wireMockServer.port()));
                    }
                }
            }
        }

        List<ServiceDTO<Service>> serviceDTOList = services.stream()
                .map(ServiceDTO::new)
                .toList();
        String replacedJson = serDes.serializeAsString(serviceDTOList);

        wireMockServer.stubFor(get("/service-registry/services?_limit=all&_offset=0")
                .willReturn(okJson(replacedJson).withHeader("Content-Range", "DeployedServices 1-1/1"))
        );
    }

    public static void mockCallback() {
        wireMockServer.stubFor(post(urlPathMatching("/mock/gazelle/rest")).willReturn(ok()));
    }

    public static void mockKeycloak() {
        String tokenUrl = "/realms/gazelle/protocol/openid-connect/token";
        wireMockServer.stubFor(post(tokenUrl)
                .withRequestBody(containing("client_id=" + ssoConfigurationProvider.getClientId()))
                .withRequestBody(containing("client_secret=" + ssoConfigurationProvider.getClientSecret()))
                .willReturn(ok("{\"access_token\":\"" + getValidJwtWithGroups(List.of("admin")) + "\"}")));
    }

    public static TestReport awaitTestReport() {
        byte[] content = Objects.requireNonNull(await()
                .atMost(3, TimeUnit.SECONDS)
                .until(() -> {
                    List<LoggedRequest> matchingRequests = getMatchingRequests().apply(wireMockServer);
                    return !matchingRequests.isEmpty() ? matchingRequests : null;
                }, Objects::nonNull)).getFirst().getBody();
        return new JacksonSerDes().deserialize(content, TestReportDTO.class).getBusinessObject();
    }

    private static Function<WireMockServer, List<LoggedRequest>> getMatchingRequests() {
        return (WireMockServer wireMockServer) -> wireMockServer.findRequestsMatching(
                postRequestedFor(
                        urlPathMatching("/mock/gazelle/rest")
                ).build()
        ).getRequests();
    }

    public static String getResourceAsString(String resourcePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/it/resources" + resourcePath)));
    }

    public static synchronized void stop() {
        wireMockServer.stop();
        wireMockServer = null;
    }
}
