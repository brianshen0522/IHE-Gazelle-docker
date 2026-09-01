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

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestRunControllerProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("gzl.service.registry.file.path", this.getClass().getResource("/services.json").getPath());
        overrides.put("gzl.it.port", "34500");
        overrides.put("gzl.service.registry.enabled", "false");
        overrides.put("gzl.service.registry.url", "http://localhost:34500/service-registry");
        overrides.put("gzl.sso.url", "http://localhost:34500");
        overrides.put("gzl.m2m.registration.startup.enabled", "false");
        overrides.put("mp.jwt.verify.issuer", "https://localhost:12345");
        overrides.put("mp.jwt.verify.publickey.location", "http://localhost:12345/public.pem");
        overrides.put("gzl.report.recording.enabled", "true");
        overrides.put("datahouse.indexes.enabled", "false");
        overrides.put("datahouse.url", "http://localhost:34501/datahouse/rest/v1");
        overrides.put("itb.api.key", "1234567890abcdef");
        return overrides;
    }

    @Override
    public Set<Class<?>> getEnabledAlternatives() {
        return QuarkusTestProfile.super.getEnabledAlternatives();
    }

    @Override
    public List<TestResourceEntry> testResources() {
        return List.of(new TestResourceEntry(MaestroExternalServicesResource.class));
    }
}
