/*
 * Copyright 2025 IHE International.
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

package net.ihe.gazelle.serviceregistry.client.technical.job;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.oidc.common.business.accesstoken.AccessTokenService;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.servicemetadata.api.business.ServiceBuilder;
import net.ihe.gazelle.serviceregistry.client.technical.ServiceRegistryMock;
import net.ihe.gazelle.serviceregistry.client.technical.dto.SecuredServiceDTO;
import net.ihe.gazelle.serviceregistry.client.technical.websocket.ServiceRegistrationWebSocketClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static net.ihe.gazelle.serviceregistry.client.technical.ServiceRegistryMock.SUCCESS_OUTCOME;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(value = KeycloakMockResource.class, restrictToAnnotatedClass = true)
class RegistrationJobTest {

   private static final TextSerDes SERDES = new JacksonSerDes();
   private static final Service SERVICE = new ServiceBuilder()
         .setName("Test Service")
         .setVersion("1.0")
         .setInstanceId("abc123")
         .setReplicaId("1")
         .build();

   private ServiceRegistryMock registryMock;

   @Inject
   AccessTokenService accessTokenService;

   @BeforeEach
   void setUp() {
      registryMock = new ServiceRegistryMock();
      registryMock.start();
   }

   @AfterEach
   void tearDown() {
      registryMock.shutdown();
   }

   @Test
   void testJob() throws InterruptedException {
      registryMock.setWebSocketDispatcher(text -> SUCCESS_OUTCOME);

      RegistrationJobMicroprofileConfig config = new RegistrationJobMicroprofileConfig();
      config.registrationEnabled = true;
      config.serviceRegistryUrl = Optional.of(registryMock.getUrl());
      config.keepAliveIntervalSeconds = 1L;

      RegistrationJob job = new RegistrationJob(() -> SERVICE, config, new ServiceRegistrationWebSocketClient(registryMock.getUrl(), accessTokenService));
      job.onStart(null);

      String message = registryMock.pollReceivedMessage(1, TimeUnit.SECONDS);
      SecuredServiceDTO serviceDTO = SERDES.deserialize(message, SecuredServiceDTO.class);
      assertEquals(SERVICE.getName(), serviceDTO.getName());
      assertEquals(SERVICE.getDescription(), serviceDTO.getDescription());
      assertEquals(SERVICE.getVersion(), serviceDTO.getVersion());
      assertEquals(SERVICE.getInstanceId(), serviceDTO.getInstanceId());
      assertEquals(SERVICE.getReplicaId(), serviceDTO.getReplicaId());
      assertEquals(SERVICE.getName(), serviceDTO.getName());
      assertTrue(message.contains("authorization"));

      String keepAlive = registryMock.pollReceivedMessage(config.keepAliveIntervalSeconds + 1, TimeUnit.SECONDS);
      serviceDTO = SERDES.deserialize(keepAlive, SecuredServiceDTO.class);
      assertEquals(SERVICE.getName(), serviceDTO.getName());
      assertEquals(SERVICE.getDescription(), serviceDTO.getDescription());
      assertEquals(SERVICE.getVersion(), serviceDTO.getVersion());
      assertEquals(SERVICE.getInstanceId(), serviceDTO.getInstanceId());
      assertEquals(SERVICE.getReplicaId(), serviceDTO.getReplicaId());
      assertEquals(SERVICE.getName(), serviceDTO.getName());
      assertTrue(message.contains("authorization"));

      job.onStop(null);

      assertEquals("CLOSING", registryMock.pollReceivedMessage(1, TimeUnit.SECONDS),
            "Job must close the websocket on stop");
   }

   @Test
   void testJobDisabled() throws InterruptedException {
      registryMock.setWebSocketDispatcher(text -> SUCCESS_OUTCOME);
      RegistrationJobMicroprofileConfig config = new RegistrationJobMicroprofileConfig();
      config.registrationEnabled = false;
      config.serviceRegistryUrl = Optional.empty();
      config.keepAliveIntervalSeconds = 1L;

      RegistrationJob job = new RegistrationJob(() -> SERVICE, config, null);
      job.onStart(null);

      assertNull(registryMock.pollReceivedMessage(1, TimeUnit.SECONDS),
            "Job must not register the service at startup if disabled");
      assertNull(registryMock.pollReceivedMessage(config.keepAliveIntervalSeconds + 1, TimeUnit.SECONDS),
            "Job must not keep alive the websocket nor register if the registration is disabled");

      job.onStop(null);

      assertNull(registryMock.pollReceivedMessage(1, TimeUnit.SECONDS),
            "Job has nothing to do on stop if disabled");
   }
}
