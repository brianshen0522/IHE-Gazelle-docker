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

package net.ihe.gazelle.serviceregistry.technical.job;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.Groups;
import net.ihe.gazelle.security.mocks.MockedGazelleIdentity;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;
import net.ihe.gazelle.serviceregistry.business.lookup.ServiceLookup;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.AVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class SelfRegistrationJobTest {

    @Inject
    ServiceLookup serviceLookup;

    private final GazelleIdentity identity = new MockedGazelleIdentity(Set.of(Groups.ROLE_ADMIN));

    @Test
    void testSelfRegistrationJob() {
        SearchQuery<ServiceSearchCriteria> query = new SearchQuery<>(
                new ServiceSearchCriteria().setName("Service Registry"),
                null,
                null
        );
        SearchResult<DeployedService> result = serviceLookup.search(query, identity);
        assertNotNull(result);
        assertEquals(1, result.objects().size());
        DeployedService service = result.objects().getFirst();
        assertEquals("Service Registry", service.getName());
        assertEquals(AVAILABLE, service.getStatus());
    }

}
