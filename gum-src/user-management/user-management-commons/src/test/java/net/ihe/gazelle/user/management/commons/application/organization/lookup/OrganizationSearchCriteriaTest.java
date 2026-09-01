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

package net.ihe.gazelle.user.management.commons.application.organization.lookup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrganizationSearchCriteriaTest {

    @Test
    void testOrganizationSearchCriteria() {
        OrganizationSearchCriteria criteria = new OrganizationSearchCriteria();
        criteria.setShortnameParam("TestShortName");

        assertEquals(1, criteria.getSearchParameters().size());
        assertEquals("shortname", criteria.getSearchParameters().getFirst().getName());
        assertEquals("TestShortName", criteria.getSearchParameters().getFirst().getFirstValue());
    }
}
