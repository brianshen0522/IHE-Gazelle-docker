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

package net.ihe.gazelle.serviceregistry.business.lookup;

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.AuthzImpl;
import net.ihe.gazelle.security.business.Groups;
import net.ihe.gazelle.security.mocks.MockedGazelleIdentity;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;
import net.ihe.gazelle.serviceregistry.business.registration.RegistrationConfiguration;
import net.ihe.gazelle.serviceregistry.business.registration.ServiceRegistration;
import net.ihe.gazelle.serviceregistry.technical.dao.InMemoryServiceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.UNREACHABLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceLookupPossibleValuesTest {

    private final Authz authz = new AuthzImpl(new PermissionStoreSPIProvider());
    private final MockedGazelleIdentity mockedIdentity = new MockedGazelleIdentity(Set.of(Groups.ROLE_ADMIN));
    private ServiceLookup serviceLookup;
    private ServiceRegistration registration;
    private InMemoryServiceRepository dao;
    private RegistrationUtil registrationUtil;

    @BeforeEach
    void setUp() {
        dao = new InMemoryServiceRepository();
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
        registration = new ServiceRegistration(dao, config, authz);
        serviceLookup = new ServiceLookupImpl(dao, authz);
        registrationUtil = new RegistrationUtil(registration);
    }

    @AfterEach
    void tearDown() {
        dao.dropAll();
    }

    @Test
    void testNoServices() {
        List<String> result = serviceLookup.getSuggestions("name", new ServiceSearchCriteria(), mockedIdentity);
        assertThat(result, empty());
    }

    @Test
    void testNoMatchingServices() {
        registrationUtil.registerAllServices();

        List<String> result = serviceLookup.getSuggestions("name", new ServiceSearchCriteria().setStatus(UNREACHABLE), mockedIdentity);
        assertThat(result, empty());

        List<String> result2 = serviceLookup.getSuggestions("badField", new ServiceSearchCriteria().setName("HL7v2 Validator"), mockedIdentity);
        assertThat(result2, empty());
    }

    @Test
    void testGetNameSuggestions() {
        registrationUtil.registerAllServices();

        List<String> result1 = serviceLookup.getSuggestions("name", new ServiceSearchCriteria(), mockedIdentity);
        assertThat(result1, hasSize(8));

        List<String> result2 = serviceLookup.getSuggestions("name", new ServiceSearchCriteria().setStatus(DeployedService.Status.AVAILABLE), mockedIdentity);
        assertThat(result2, hasSize(6));
        assertTrue(result2.contains("HL7v2 Validator"));

        List<String> result3 = serviceLookup.getSuggestions("name", new ServiceSearchCriteria().setSelfRegistered(false), mockedIdentity);
        assertThat(result3, hasSize(2));
        assertTrue(result3.contains("Test Management"));

    }

    @Test
    void testGetOtherSuggestions() {
        registrationUtil.registerAllServices();
        ServiceSearchCriteria serviceSearchCriteria = new ServiceSearchCriteria().setName("HL7v2 Validator");

        List<String> result2 = serviceLookup.getSuggestions("providedInterface", serviceSearchCriteria, mockedIdentity);
        assertThat(result2, hasSize(1));
        assertThat(result2.getFirst(), equalTo("Gazelle Validation API"));

        List<String> result3 = serviceLookup.getSuggestions("status", serviceSearchCriteria, mockedIdentity);
        assertTrue(result3.contains("AVAILABLE"));

        List<String> result4 = serviceLookup.getSuggestions("instanceId", serviceSearchCriteria, mockedIdentity);
        assertThat(result4, hasSize(1));
        assertEquals("66666", result4.getFirst());

        List<String> result5 = serviceLookup.getSuggestions("selfRegistered", serviceSearchCriteria, mockedIdentity);
        assertThat(result5, hasSize(1));
        assertEquals("true", result5.getFirst());
    }
}
