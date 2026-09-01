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

package net.ihe.gazelle.serviceregistry.business.registration;

import com.google.common.collect.TreeMultimap;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.AuthzImpl;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.mocks.MockedGazelleIdentity;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.ServiceId;
import net.ihe.gazelle.serviceregistry.technical.dao.InMemoryServiceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static net.ihe.gazelle.security.business.Groups.ROLE_ADMIN;
import static net.ihe.gazelle.security.business.Groups.ROLE_TEST_SERVICE;
import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.*;
import static org.junit.jupiter.api.Assertions.*;

class ServiceRegistrationTest {

   private InMemoryServiceRepository serviceRegistrationDAO;
   private final MockedGazelleIdentity mockedIdentity = new MockedGazelleIdentity(Set.of(ROLE_ADMIN));

   @BeforeEach
   void setUp() {
      serviceRegistrationDAO = new InMemoryServiceRepository();
   }

   @AfterEach
   void tearDown() {
      serviceRegistrationDAO.dropAll();
   }

   @Test
   void testRegisterMinimalService() {
      ServiceRegistration serviceRegistration = getServiceRegistration();
      Service service = new Service()
            .setName("minimalValidService")
            .setVersion("1.0")
            .setInstanceId("ab123")
            .setReplicaId("c45");
      assertDoesNotThrow(() -> serviceRegistration.register(service));
      DeployedService registeredService = serviceRegistration.getService(new ServiceId(service), mockedIdentity);
      assertEquals("minimalValidService", registeredService.getName());
      assertEquals("1.0", registeredService.getVersion());
      assertEquals("ab123", registeredService.getInstanceId());
      assertEquals("c45", registeredService.getReplicaId());
      assertEquals(DeployedService.Status.UNKNOWN, registeredService.getStatus());
      assertFalse(registeredService.isSelfRegistered());
   }

   @Test
   void testConnectService() {
      GazelleIdentity identity = new MockedGazelleIdentity(Set.of(ROLE_TEST_SERVICE));
      ServiceRegistration serviceRegistration = getServiceRegistration();
      DeployedService service = new DeployedService(
            new Service()
                  .setName("minimalValidService")
                  .setVersion("1.0")
                  .setInstanceId("987654")
                  .setReplicaId("1")
      );
      assertDoesNotThrow(() -> serviceRegistration.connectService(service, identity));
      DeployedService registeredService = serviceRegistration.getService(new ServiceId(service), mockedIdentity);
      assertEquals(AVAILABLE, registeredService.getStatus());
      assertTrue(registeredService.isSelfRegistered());
   }

   @Test
   void testDisconnectService() {
      GazelleIdentity identity = new MockedGazelleIdentity(Set.of(ROLE_TEST_SERVICE));
      ServiceRegistration serviceRegistration = getServiceRegistration();
      DeployedService service = new DeployedService(
            new Service()
                  .setName("minimalValidService")
                  .setVersion("1.0")
                  .setInstanceId("987654")
                  .setReplicaId("1")
      );
      assertDoesNotThrow(() -> serviceRegistration.connectService(service, identity));
      assertTrue(serviceRegistration.isServiceRegistered(new ServiceId(service)));

      assertDoesNotThrow(() -> serviceRegistration.disconnectService(new ServiceId(service)));
      DeployedService disconnectedService = serviceRegistration.getService(new ServiceId(service), mockedIdentity);
      assertEquals(UNREACHABLE, disconnectedService.getStatus());
   }

   @Test
   void testUnknownService() {
      ServiceRegistration serviceRegistration = getServiceRegistration();
      Throwable throwable = assertThrows(NoSuchElementException.class,
            () -> serviceRegistration.getService(new ServiceId("nonExistentService", "1"), mockedIdentity));
      assertEquals("ServiceId{instanceId='nonExistentService', replicaId='1'} does not exist.", throwable.getMessage());
   }

   @Test
   void testRegisterIncompleteService() {
      ServiceRegistration serviceRegistration = getServiceRegistration();
      Service service = new Service()
            .setName("incompleteService");
      assertThrows(IllegalArgumentException.class, () -> serviceRegistration.register(service));
   }

   @Test
   void testRegisterServiceWithReplica() {
      ServiceRegistration serviceRegistration = getServiceRegistration();
      Service service1 = new Service()
            .setName("myService")
            .setInstanceId("123456")
            .setReplicaId("abc")
            .setVersion("1.0");
      Service service2 = new Service()
            .setName("myService")
            .setInstanceId("123456")
            .setReplicaId("def")
            .setVersion("1.0");
      assertDoesNotThrow(() -> serviceRegistration.register(service1));
      assertDoesNotThrow(() -> serviceRegistration.register(service2));
      assertTrue(serviceRegistration.isServiceRegistered(new ServiceId(service1)));
      assertTrue(serviceRegistration.isServiceRegistered(new ServiceId(service2)));
      DeployedService retrievedService1 = serviceRegistration.getService(new ServiceId(service1), mockedIdentity);
      DeployedService retrievedService2 = serviceRegistration.getService(new ServiceId(service2), mockedIdentity);
      assertEquals("abc", retrievedService1.getReplicaId());
      assertEquals("def", retrievedService2.getReplicaId());
   }

   @Test
   void testRegisterUpdate() {
      ServiceRegistration serviceRegistration = getServiceRegistration();
      Service service1 = new Service()
            .setName("myService")
            .setInstanceId("123456")
            .setReplicaId("1")
            .setVersion("1.0");
      Service service2 = new Service()
            .setName("anotherService")
            .setInstanceId("123456")
            .setReplicaId("1")
            .setVersion("2.0");
      assertDoesNotThrow(() -> serviceRegistration.register(service1));
      assertDoesNotThrow(() -> serviceRegistration.register(service2));
      DeployedService retrievedService = serviceRegistration.getService(new ServiceId(service1), mockedIdentity);
      assertEquals("anotherService", retrievedService.getName());
      assertEquals("2.0", retrievedService.getVersion());
   }

   @Test
   void testRemoveServiceError() {
      ServiceRegistration serviceRegistration = getServiceRegistration();
      assertThrows(NoSuchElementException.class,
            () -> serviceRegistration.unregister(new ServiceId("nonExistentService", "1")));
   }

   @Test
   void testRemoveService() {
      ServiceRegistration serviceRegistration = getServiceRegistration();
      Service service = new Service()
            .setName("minimalValidService")
            .setVersion("1.0")
            .setInstanceId("ab123")
            .setReplicaId("c45");
      ServiceId serviceId = new ServiceId(service);

      assertDoesNotThrow(() -> serviceRegistration.register(service));
      assertTrue(serviceRegistration.isServiceRegistered(serviceId));

      assertDoesNotThrow(() -> serviceRegistration.unregister(serviceId));
      assertFalse(serviceRegistration.isServiceRegistered(serviceId));
   }

   @Test
   void testPurge() {
      ServiceRegistration serviceRegistration = getServiceRegistration(Duration.ofHours(2));
      DeployedService service1 = getUnreachableExpiredService();
      DeployedService service2 = getConnectedService();
      DeployedService service2bis = ((DeployedService) getConnectedService().setReplicaId("2"));
      DeployedService service3 = getRecentlyUnreachableService();
      DeployedService service4 = getUnknownExpiredService();
      assertDoesNotThrow(() -> serviceRegistration.doRegister(service1));
      assertDoesNotThrow(() -> serviceRegistration.doRegister(service2));
      assertDoesNotThrow(() -> serviceRegistration.doRegister(service2bis));
      assertDoesNotThrow(() -> serviceRegistration.doRegister(service3));
      assertDoesNotThrow(() -> serviceRegistration.doRegister(service4));
      assertTrue(serviceRegistration.isServiceRegistered(new ServiceId(service1)));
      assertTrue(serviceRegistration.isServiceRegistered(new ServiceId(service2)));
      assertTrue(serviceRegistration.isServiceRegistered(new ServiceId(service2bis)));
      assertTrue(serviceRegistration.isServiceRegistered(new ServiceId(service3)));
      assertTrue(serviceRegistration.isServiceRegistered(new ServiceId(service4)));

      serviceRegistration.purgeExpiredSelfRegistered();

      assertFalse(serviceRegistration.isServiceRegistered(new ServiceId(service1)));
      assertTrue(serviceRegistration.isServiceRegistered(new ServiceId(service2)));
      assertTrue(serviceRegistration.isServiceRegistered(new ServiceId(service2bis)));
      assertTrue(serviceRegistration.isServiceRegistered(new ServiceId(service3)));
      assertFalse(serviceRegistration.isServiceRegistered(new ServiceId(service4)));
   }

   @Test
   void testMonitorHeartbeatDoesNotGrowStatusIndexOverCycles() throws Exception {
      ServiceRegistration serviceRegistration = getServiceRegistration(Duration.ofHours(72), Duration.ofMinutes(1));
      int serviceCount = 25;
      int cycles = 40;

      for (int i = 0; i < serviceCount; i++) {
         serviceRegistration.doRegister(new DeployedService(new Service()
               .setName("selfRegisteredService" + i)
               .setVersion("1.0")
               .setInstanceId("instance-" + i)
               .setReplicaId("1"))
               .setSelfRegistered(true)
               .setStatus(AVAILABLE)
               .setLastUpdate(Instant.now().minus(Duration.ofHours(1))));
      }

      for (int cycle = 0; cycle < cycles; cycle++) {
         serviceRegistration.monitorServiceHeartbeat();

         assertEquals(serviceCount, getIdIndexSize(), "ID_INDEX must stay stable");
         assertEquals(serviceCount, getStatusIndexSize(), "STATUS_INDEX must not accumulate ghost references");
         assertEquals(0, getStatusIndexEntries("AVAILABLE"), "AVAILABLE status bucket must be empty after heartbeat monitoring");

         // Re-arm services as AVAILABLE+expired to repeatedly exercise monitor->update->delete/create logic.
         for (int i = 0; i < serviceCount; i++) {
            serviceRegistrationDAO.update(new DeployedService(new Service()
                  .setName("selfRegisteredService" + i)
                  .setVersion("1.0")
                  .setInstanceId("instance-" + i)
                  .setReplicaId("1"))
                  .setSelfRegistered(true)
                  .setStatus(AVAILABLE)
                  .setLastUpdate(Instant.now().minus(Duration.ofHours(1))));
         }
      }
   }

   private static DeployedService getUnknownExpiredService() {
      return new DeployedService(new Service()
            .setName("selfRegisteredService4")
            .setVersion("1.0")
            .setInstanceId("selfReg000")
            .setReplicaId("1"))
            .setSelfRegistered(true)
            .setStatus(UNKNOWN)
            .setLastUpdate(Instant.now().minus(Duration.ofHours(3)));
   }

   private static DeployedService getRecentlyUnreachableService() {
      return new DeployedService(new Service()
            .setName("selfRegisteredService3")
            .setVersion("1.0")
            .setInstanceId("selfReg789")
            .setReplicaId("1"))
            .setSelfRegistered(true)
            .setStatus(UNREACHABLE)
            .setLastUpdate(Instant.now().minus(Duration.ofHours(1)));
   }

   private static DeployedService getConnectedService() {
      return new DeployedService(new Service()
            .setName("selfRegisteredService2")
            .setVersion("1.0")
            .setInstanceId("selfReg456")
            .setReplicaId("1"))
            .setSelfRegistered(true)
            .setStatus(AVAILABLE)
            .setLastUpdate(Instant.now().minus(Duration.ofHours(3)));
   }

   private static DeployedService getUnreachableExpiredService() {
      return new DeployedService(new Service()
            .setName("selfRegisteredService1")
            .setVersion("1.0")
            .setInstanceId("selfReg123")
            .setReplicaId("1"))
            .setSelfRegistered(true)
            .setStatus(UNREACHABLE)
            .setLastUpdate(Instant.now().minus(Duration.ofHours(3)));
   }

   private ServiceRegistration getServiceRegistration() {
      return getServiceRegistration(Duration.ofHours(72));
   }

   private ServiceRegistration getServiceRegistration(Duration selfRegistrationExpiration) {
      return getServiceRegistration(selfRegistrationExpiration, Duration.ofMinutes(5));
   }

   private ServiceRegistration getServiceRegistration(Duration selfRegistrationExpiration, Duration heartbeatTimeout) {
      Authz authz = new AuthzImpl(new PermissionStoreSPIProvider());
      RegistrationConfiguration config = new RegistrationConfiguration() {
         @Override
         public Duration getSelfRegistrationTimeout() {
            return selfRegistrationExpiration;
         }

         @Override
         public Duration getHeartbeatTimeout() {
            return heartbeatTimeout;
         }
      };
      return new ServiceRegistration(serviceRegistrationDAO, config, authz);
   }

   @SuppressWarnings("unchecked")
   private int getStatusIndexSize() throws Exception {
      java.lang.reflect.Field statusIndexField = InMemoryServiceRepository.class.getDeclaredField("STATUS_INDEX");
      statusIndexField.setAccessible(true);
      return ((TreeMultimap<String, DeployedService>) statusIndexField.get(null)).size();
   }

   @SuppressWarnings("unchecked")
   private int getStatusIndexEntries(String status) throws Exception {
      java.lang.reflect.Field statusIndexField = InMemoryServiceRepository.class.getDeclaredField("STATUS_INDEX");
      statusIndexField.setAccessible(true);
      return ((TreeMultimap<String, DeployedService>) statusIndexField.get(null)).get(status).size();
   }

   @SuppressWarnings("unchecked")
   private int getIdIndexSize() throws Exception {
      java.lang.reflect.Field idIndexField = InMemoryServiceRepository.class.getDeclaredField("ID_INDEX");
      idIndexField.setAccessible(true);
      return ((Map<ServiceId, DeployedService>) idIndexField.get(null)).size();
   }

}
