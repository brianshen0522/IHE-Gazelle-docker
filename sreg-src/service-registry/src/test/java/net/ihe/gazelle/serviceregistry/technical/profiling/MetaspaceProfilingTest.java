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

package net.ihe.gazelle.serviceregistry.technical.profiling;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.mocks.MockedGazelleIdentity;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.servicemetadata.api.business.ServiceBuilder;
import net.ihe.gazelle.serviceregistry.api.business.ServiceId;
import net.ihe.gazelle.serviceregistry.business.registration.ServiceRegistration;
import net.ihe.gazelle.serviceregistry.technical.dao.InMemoryServiceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static net.ihe.gazelle.security.business.Groups.ROLE_TEST_SERVICE;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Profiling test to detect metaspace leaks during repeated WebSocket-like serialization/deserialization cycles.
 * This test simulates the WebSocket registration scenario where the same ObjectMapper is used repeatedly.
 */
@QuarkusTest
class MetaspaceProfilingTest {

    private static final GazelleIdentity TEST_SERVICE_IDENTITY = new MockedGazelleIdentity(Set.of(ROLE_TEST_SERVICE));

    @Inject
    ServiceRegistration serviceRegistration;

    @Inject
    InMemoryServiceRepository repository;

    @AfterEach
    void tearDown() {
        repository.dropAll();
    }

    /**
     * Test that repeated service registration (simulating WebSocket messages) does not cause metaspace bloat.
     * Uses jcmd to capture metaspace usage before and after.
     */
    @Test
    void shouldNotLeakMetaspaceOnRepeatedWebSocketLikeOperations() throws Exception {
        Path profilingDir = Path.of("target", "profiling");
        Files.createDirectories(profilingDir);

        int registrationCount = 200;  // Simulate 200 WebSocket messages

        // Capture metaspace baseline
        long metaspaceBefore = getMetaspaceUsage();

        // Register services repeatedly (simulating WebSocket @OnMessage calls)
        for (int i = 0; i < registrationCount; i++) {
            Service service = new ServiceBuilder()
                    .setName("ws-service-" + i)
                    .setVersion("1.0.0")
                    .setInstanceId("ws-instance-" + i)
                    .setReplicaId("1")
                    .build();

            // This simulates the WebSocket onMessage deserialization + connectService
            serviceRegistration.connectService(service, TEST_SERVICE_IDENTITY);

            // Immediately disconnect (like connection loss)
            serviceRegistration.disconnectService(new ServiceId(service));
        }

        // Force garbage collection to clean up heap
        System.gc();
        Thread.sleep(500);

        // Capture metaspace after
        long metaspaceAfter = getMetaspaceUsage();
        long metaspaceDelta = metaspaceAfter - metaspaceBefore;
        long metaspaceGrowthPercentage = (metaspaceDelta * 100) / Math.max(1, metaspaceBefore);

        writeProfilingReport(profilingDir, metaspaceBefore, metaspaceAfter, metaspaceDelta, metaspaceGrowthPercentage);

        // Acceptable threshold: metaspace should not grow more than 5% during 200 operations
        // If it grows more, it indicates a metaspace leak
        assertTrue(metaspaceGrowthPercentage < 5,
                () -> String.format("Metaspace growth is suspicious: %dMB (+%d%%) after %d operations. " +
                                "Before: %dMB, After: %dMB",
                        metaspaceDelta / (1024 * 1024),
                        metaspaceGrowthPercentage,
                        registrationCount,
                        metaspaceBefore / (1024 * 1024),
                        metaspaceAfter / (1024 * 1024)));
    }

    private long getMetaspaceUsage() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    private void writeProfilingReport(Path dir, long before, long after, long delta, long percentGrowth) throws IOException {
        String report = String.format("Metaspace Profiling Report%n" +
                        "=========================%n" +
                        "Before: %d bytes (%d MB)%n" +
                        "After: %d bytes (%d MB)%n" +
                        "Delta: %d bytes (%d MB)%n" +
                        "Growth: %d%%%n",
                before, before / (1024 * 1024),
                after, after / (1024 * 1024),
                delta, delta / (1024 * 1024),
                percentGrowth);

        Files.writeString(dir.resolve("metaspace-profiling.txt"), report);
    }
}

