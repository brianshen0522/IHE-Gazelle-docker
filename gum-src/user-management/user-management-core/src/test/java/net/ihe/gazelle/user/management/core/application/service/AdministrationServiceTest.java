/*
 * Copyright 2024 IHE International.
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

package net.ihe.gazelle.user.management.core.application.service;

import net.ihe.gazelle.user.management.core.interlay.dao.AdministrationDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Timestamp;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class AdministrationServiceTest {

    private static final int DAYS_IN_TWO_MONTH = 62;
    @Mock
    private AdministrationDAO administrationDAO;
    private AdministrationService administrationService;

    @BeforeEach
    void beforeEach() {
        administrationService = new AdministrationServiceImpl(administrationDAO);
    }

    @Test
    void testGetTimestampMinusOneMonth() {
        Timestamp timestamp = AdministrationServiceImpl.getCurrentTimestampPlusDays(0);
        assertEquals(LocalDate.now().getMonth(), timestamp.toLocalDateTime().getMonth());

        timestamp = AdministrationServiceImpl.getCurrentTimestampPlusDays(-DAYS_IN_TWO_MONTH);
        assertEquals(LocalDate.now().minusDays(DAYS_IN_TWO_MONTH).getMonth(), timestamp.toLocalDateTime().getMonth());

        timestamp = AdministrationServiceImpl.getCurrentTimestampPlusDays(DAYS_IN_TWO_MONTH);
        assertEquals(LocalDate.now().plusDays(DAYS_IN_TWO_MONTH).getMonth(), timestamp.toLocalDateTime().getMonth());
    }

    @Test
    void testPurgeInactiveUsers() {
        Mockito.when(administrationDAO.purgeInactiveAndNonConsentUsers(Mockito.any())).thenReturn(17);
        assertDoesNotThrow(() -> administrationService.purgeInactiveUsers(31));
    }
}
