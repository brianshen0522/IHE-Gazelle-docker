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

package net.ihe.gazelle.simulation;

import com.github.tomakehurst.wiremock.WireMockServer;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.servicemetadata.api.business.Binding;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.servicemetadata.technical.jaxrs.ServiceDTO;
import net.ihe.gazelle.simulation.business.sequence.SimulationChecksumService;
import net.ihe.gazelle.simulation.utils.ResourceRetriever;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WireMockSingleton {

    public static final String SVS_VALUE_SET_ID = "IT value set";

    private static WireMockServer wireMockServer;

    private WireMockSingleton() {
        // Private constructor to enforce Singleton pattern
    }

    public static synchronized WireMockServer startServer(int port) {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(port);
            wireMockServer.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> wireMockServer.stop()));
        }
        return wireMockServer;
    }

    public static synchronized void mockChecksum() throws IOException {
        wireMockServer.stubFor(get("/simulation/v1/sequences/checksum")
                .willReturn(okJson("{\"checksum\": \"" + generateChecksum() + "\"}"))
        );
    }

    public static synchronized void mockSimulationSequence() throws IOException {
        wireMockServer.stubFor(get("/simulation/v1/sequences")
                .willReturn(okJson(getResourceAsString("it/simulation-sequences.json")))
        );
    }

    public static synchronized void mockResolvedSimulationSequence() throws IOException {
        wireMockServer.stubFor(get("/simulation/v1/sequences")
                .willReturn(okJson(getResourceAsString("it/resolved-sequences.json")))
        );
    }

    public static synchronized void mockServiceRegistry() throws IOException {
        String servicesJson = getResourceAsString("it/service-registry.json");
        TextSerDes serDes = new JacksonSerDes();
        List<Service> services = Arrays.stream(serDes.deserialize(servicesJson, ServiceDTO[].class))
                .map(ServiceDTO::getBusinessObject)
                .toList();

        for (Service service : services) {
            for (ProvidedInterface providedInterface : service.getProvidedInterfaces()) {
                for (Binding binding : providedInterface.getBindings()) {
                    if (binding instanceof HttpRestBinding restBinding) {
                        restBinding.setServiceUrl("http://localhost:" + wireMockServer.port());
                    }
                }
            }
        }

        List<ServiceDTO<Service>> serviceDTOList = services.stream()
                .map(ServiceDTO::new)
                .toList();
        String replacedJson = serDes.serializeAsString(serviceDTOList);

        wireMockServer.stubFor(get("/service-registry/services?_limit=all&_offset=0&providedInterface=Simulation+Service+API&status=AVAILABLE%2CUNKNOWN")
                .willReturn(okJson(replacedJson).withHeader("Content-Range", "DeployedServices 1-1/1"))
        );
    }

    public static synchronized void mockSVSSimulator() throws IOException {
        String url = "/SVSSimulator/rest/RetrieveValueSetForSimulator";
        // Return 200 OK with XML if 'id' query param equals "2.16.840.1.113883.2.8.1.2.57"
        wireMockServer.stubFor(get(urlPathMatching(url))
                .withQueryParam("id", equalTo(SVS_VALUE_SET_ID))
                .willReturn(okXml(getResourceAsString("SVSValueSet.xml")))
        );

        // Return 404 Not Found if 'id' is anything else
        wireMockServer.stubFor(get(urlPathMatching(url))
                .atPriority(10) // Lower priority to match only when the above doesn't
                .willReturn(notFound().withHeader("Warning", "Unknown value set"))
        );

        wireMockServer.stubFor(get(urlPathMatching(url))
                .withQueryParam("id", equalTo("invalid"))
                .willReturn(okXml("<Invalid />"))
        );

        wireMockServer.stubFor(get(urlPathMatching(url))
                .withQueryParam("id", equalTo("500"))
                .willReturn(serverError())
        );
    }

    public static WireMockServer getWireMockServer() {
        return wireMockServer;
    }

    public static String getResourceAsString(String resourcePath) throws IOException {
        InputStream inputStream = ResourceRetriever.class.getClassLoader().getResourceAsStream(resourcePath);
        Objects.requireNonNull(inputStream, "Resource not found: " + resourcePath);
        return new String(inputStream.readAllBytes());
    }

    private static synchronized String generateChecksum() throws IOException {
        String sequences = getResourceAsString("it/simulation-sequences.json");
        return SimulationChecksumService.computeChecksum(sequences);
    }
}
