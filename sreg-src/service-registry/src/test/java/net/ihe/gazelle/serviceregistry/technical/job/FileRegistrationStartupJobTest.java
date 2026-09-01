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

package net.ihe.gazelle.serviceregistry.technical.job;

import net.ihe.gazelle.modelmarshaller.technical.serialization.DeserializationException;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.AuthzImpl;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.serviceregistry.api.business.ServiceId;
import net.ihe.gazelle.serviceregistry.business.registration.RegistrationConfiguration;
import net.ihe.gazelle.serviceregistry.business.registration.ServiceRegistration;
import net.ihe.gazelle.serviceregistry.technical.dao.FileServiceRepository;
import net.ihe.gazelle.serviceregistry.technical.dao.InMemoryServiceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.InvalidPathException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;


class FileRegistrationStartupJobTest {

   private final Authz authz = new AuthzImpl(new PermissionStoreSPIProvider());
   private InMemoryServiceRepository serviceRegistrationDAO;
   private ServiceRegistration serviceRegistration;

   @BeforeEach
   void setUp() {
      serviceRegistrationDAO = new InMemoryServiceRepository();
      RegistrationConfiguration config = new RegistrationConfiguration() {
         @Override
         public Duration getSelfRegistrationTimeout() {
            return Duration.ofHours(72);
         }
         
         @Override
         public Duration getHeartbeatTimeout() {
            return Duration.ofMinutes(5);
         }
      };
      serviceRegistration = new ServiceRegistration(serviceRegistrationDAO, config, authz);
   }

   @AfterEach
   void tearDown() {
      serviceRegistrationDAO.dropAll();
   }

   @Test
   void testLoadValidFile() {
      FileServiceRepository fileServiceRepository = new FileServiceRepository(() -> "src/test/resources/file-registration/services.json");
      FileRegistrationStartupJob job = new FileRegistrationStartupJob(serviceRegistration, fileServiceRepository);

      job.onStart(null);

      assertTrue(serviceRegistrationDAO.isServiceRegistered(new ServiceId("123abc", "001")));
      assertTrue(serviceRegistrationDAO.isServiceRegistered(new ServiceId("234bcd", "234")));
      assertTrue(serviceRegistrationDAO.isServiceRegistered(new ServiceId("345cde", "a10")));
   }

   @Test
   void testInvalidFilePath() {
      MockLogger logger = new MockLogger();
      FileServiceRepository fileServiceRepository = new FileServiceRepository(() -> "bad*file\u0000name.json");
      FileRegistrationStartupJob job = new FileRegistrationStartupJob(logger, serviceRegistration, fileServiceRepository);

      job.onStart(null);

      MockLogger.LogEntry entry = logger.getLogEntries().getFirst();
      assertTrue(entry.throwable() instanceof InvalidPathException);
      assertEquals("ERROR", entry.level());
      assertEquals("Services file path is invalid.", entry.message());
   }

   @Test
   void testFileNotFound() {
      MockLogger logger = new MockLogger();
      FileServiceRepository fileServiceRepository = new FileServiceRepository(() -> "src/test/resources/file-registration/nonexistent.json");
      FileRegistrationStartupJob job = new FileRegistrationStartupJob(logger, serviceRegistration, fileServiceRepository);

      job.onStart(null);

      MockLogger.LogEntry entry = logger.getLogEntries().getFirst();
      assertEquals("WARN", entry.level());
      assertEquals("Services file not found. {}", entry.message());
      assertEquals("File not found: src/test/resources/file-registration/nonexistent.json", entry.args()[0].toString());
   }

   @Test
   void testFileDeserializationError() {
      MockLogger logger = new MockLogger();
      FileServiceRepository fileServiceRepository = new FileServiceRepository(() -> "src/test/resources/file-registration/ko_binding_type.json");
      FileRegistrationStartupJob job = new FileRegistrationStartupJob(logger, serviceRegistration, fileServiceRepository);

      job.onStart(null);

      MockLogger.LogEntry entry = logger.getLogEntries().getFirst();
      assertTrue(entry.throwable() instanceof DeserializationException);
      assertEquals("ERROR", entry.level());
      assertEquals("Error deserializing services from file.", entry.message());
   }

   @Test
   void testInvalidService() {
      MockLogger logger = new MockLogger();
      FileServiceRepository fileServiceRepository = new FileServiceRepository(() -> "src/test/resources/file-registration/ko_url_relative.json");
      FileRegistrationStartupJob job = new FileRegistrationStartupJob(logger, serviceRegistration, fileServiceRepository);

      job.onStart(null);

      assertTrue(serviceRegistrationDAO.isServiceRegistered(new ServiceId("123abc", "001")));
      assertFalse(serviceRegistrationDAO.isServiceRegistered(new ServiceId("234bcd", "234")));

      MockLogger.LogEntry entry = logger.getLogEntries().get(1);
      assertEquals("ERROR", entry.level());
      assertEquals("Error registring service {} : {}", entry.message());
      assertEquals("KO_relative_URL", entry.args()[0].toString());
   }
}
