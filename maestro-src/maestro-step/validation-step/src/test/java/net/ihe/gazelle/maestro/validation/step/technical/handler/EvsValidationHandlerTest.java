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

package net.ihe.gazelle.maestro.validation.step.technical.handler;

import net.ihe.gazelle.evsapi.client.business.ValidationClient;
import net.ihe.gazelle.evsapi.client.business.ValidationServiceProfile;
import net.ihe.gazelle.evsapi.client.business.Validator;
import net.ihe.gazelle.maestro.validation.step.business.ValidationHandler;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EvsValidationHandlerTest {

   private static final String SERVICE_NAME = "EVS_SERVICE";

   private final ValidationClient validationClient = mock(ValidationClient.class);
   private ValidationHandler handler;

   @BeforeEach
   void setUp() {
      handler = new EvsValidationHandler(SERVICE_NAME, validationClient);
   }

   @Test
   void isAvailableReturnsTrueWhenProfilesExist() {
      when(validationClient.getValidationProfilesByServiceName(SERVICE_NAME))
            .thenReturn(List.of(
                  new ValidationServiceProfile()
                  .setValidator(
                        new Validator().setKeyword("profile")
                  )
            ));

      assertTrue(handler.isAvailable());
   }

   @Test
   void isAvailableReturnsFalseWhenClientReturnsNull() {
      when(validationClient.getValidationProfilesByServiceName(SERVICE_NAME)).thenReturn(null);

      assertFalse(handler.isAvailable());
   }

   @Test
   void getValidationProfilesMapsEvsProfiles() {
      ValidationServiceProfile evsProfile = new ValidationServiceProfile()
            .setValidator(
                  new Validator().setKeyword("profile")
            )
            .setServiceName("EVS");
      when(validationClient.getValidationProfilesByServiceName(SERVICE_NAME))
            .thenReturn(List.of(evsProfile));

      List<ValidationProfile> profiles = handler.getValidationProfiles();

      assertNotNull(profiles);
      assertEquals(1, profiles.size());
   }

}
