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

package net.ihe.gazelle.user.management.api.domain.group;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupTypeTest {

    @Test
    void testPrefixOfGroupType() {
        assertEquals("role",GroupType.ROLE.getPrefix());
        assertEquals("org",GroupType.ORGANIZATION.getPrefix());
        assertEquals("org-adm",GroupType.ORGANIZATION_ADMIN.getPrefix());
    }

    @Test
    void testGroupTypeFromPrefix() {
        assertEquals(GroupType.ROLE,GroupType.getTypeFromPrefix("role"));
        assertEquals(GroupType.ORGANIZATION,GroupType.getTypeFromPrefix("org"));
        assertEquals(GroupType.ORGANIZATION_ADMIN,GroupType.getTypeFromPrefix("org-adm"));
    }
}
