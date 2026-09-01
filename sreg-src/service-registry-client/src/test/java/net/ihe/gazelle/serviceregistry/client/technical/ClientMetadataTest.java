/*
 * Copyright 2022-2025 IHE International.
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

package net.ihe.gazelle.serviceregistry.client.technical;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ihe.gazelle.lang.GzlCollectors;
import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterface;
import net.ihe.gazelle.servicemetadata.api.business.MetadataService;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ClientMetadataTest {

   @ApplicationMock
   @Inject
   MetadataService metadataService;

   @Test
   void testConsumedInterfaces() {
      Service service = metadataService.getMetadata();
      Assertions.assertNotNull(service.getConsumedInterfaces());
      assertEquals(2, service.getConsumedInterfaces().size());


      ConsumedInterface registrationAPI = service.getConsumedInterfaces().stream()
            .filter(api -> "Service Registration API".equals(api.getInterfaceName()))
            .collect(GzlCollectors.toSingleton());
      assertFalse(registrationAPI.isRequired());
      assertThat(
            registrationAPI.getSupportedVersions(),
            contains("2.0.0")
      );
      assertThat(
            registrationAPI.getSupportedBindings(),
            contains("WEB_SOCKET")
      );

      ConsumedInterface lookupAPI = service.getConsumedInterfaces().stream()
            .filter(api -> "Service Lookup API".equals(api.getInterfaceName()))
            .collect(GzlCollectors.toSingleton());
      assertTrue(lookupAPI.isRequired());
      assertThat(
            lookupAPI.getSupportedVersions(),
            contains("1.0.0")
      );
      assertThat(
            lookupAPI.getSupportedBindings(),
            contains("REST")
      );
   }

}
