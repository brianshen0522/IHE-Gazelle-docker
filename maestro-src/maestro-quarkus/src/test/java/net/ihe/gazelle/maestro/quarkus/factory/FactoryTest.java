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

package net.ihe.gazelle.maestro.quarkus.factory;

import net.ihe.gazelle.maestro.api.business.Maestro;
import net.ihe.gazelle.maestro.engine.business.HardWiredEventBroker;
import net.ihe.gazelle.maestro.engine.business.MetadataServiceMock;
import net.ihe.gazelle.maestro.engine.business.RecordingMaestro;
import net.ihe.gazelle.maestro.engine.business.StepExecutorProvider;
import net.ihe.gazelle.maestro.engine.business.mock.InMemoryTestReportRecordingService;
import net.ihe.gazelle.maestro.quarkus.mock.HandlerProviderMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import technical.dao.SessionStoreImpl;
import technical.provider.StepExecutorSPIProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FactoryTest {

    private MaestroFactory maestroFactory;
    private ServicesFactory servicesFactory;

    @BeforeEach
    void setUp() {
        ExecutorService executorService = Executors.newCachedThreadPool();

        servicesFactory = new ServicesFactory(
                new HandlerProviderMock()
        );

        maestroFactory = new MaestroFactory(
                new SessionStoreImpl<>(),
                new SessionStoreImpl<>(),
                new SessionStoreImpl<>(),
                new SessionStoreImpl<>(),
                new HardWiredEventBroker(executorService),
                new HardWiredEventBroker(executorService),
                new HardWiredEventBroker(executorService),
                new HardWiredEventBroker(executorService),
                new InMemoryTestReportRecordingService(),
                servicesFactory.getStepRunnerProvider(),
                new MetadataServiceMock(),
              true
                );

    }

    @Test
    void getMaestroTest() {
        Maestro maestro = maestroFactory.getRecordingMaestro();
        assertNotNull(maestro);
        assertInstanceOf(RecordingMaestro.class, maestro);
    }

    @Test
    void getStepRunnerProviderTest() {
        StepExecutorProvider provider = servicesFactory.getStepRunnerProvider();
        assertNotNull(provider);
        assertInstanceOf(StepExecutorSPIProvider.class, provider);
    }

}
