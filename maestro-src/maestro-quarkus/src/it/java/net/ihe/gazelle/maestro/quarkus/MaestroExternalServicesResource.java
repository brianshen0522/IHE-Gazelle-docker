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

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.io.IOException;
import java.util.Map;

public class MaestroExternalServicesResource implements QuarkusTestResourceLifecycleManager {

    static final int WIREMOCK_PORT = 34500;
    static final int DATAHOUSE_PORT = 34501;

    @Override
    public Map<String, String> start() {
        try {
            WireMockSingleton.startServer(WIREMOCK_PORT);
            WireMockSingleton.mockCallback();
            WireMockSingleton.mockServiceRegistry();
            WireMockSingleton.mockGetProfiles();
            WireMockSingleton.mockEvsProfiles();
            WireMockSingleton.mockValidate();
            WireMockSingleton.mockKeycloak();
            MockDatahouseServer.start(DATAHOUSE_PORT);
            return Map.of(
                    "gzl.it.port", String.valueOf(WIREMOCK_PORT),
                    "gzl.service.registry.url", "http://localhost:" + WIREMOCK_PORT + "/service-registry",
                    "gzl.sso.url", "http://localhost:" + WIREMOCK_PORT,
                    "datahouse.url", "http://localhost:" + DATAHOUSE_PORT + "/datahouse/rest/v1"
            );
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start Maestro integration test resources", e);
        }
    }

    @Override
    public void stop() {
        WireMockSingleton.stop();
        try {
            MockDatahouseServer.stop();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to stop Maestro integration test resources", e);
        }
    }
}
