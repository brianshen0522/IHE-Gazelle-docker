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

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroupTest {

    @Test
    void testGroupConstructor() {
        Group group = new Group("testId", GroupType.ORGANIZATION, "testRef");

        assertEquals("testId", group.getId());
        assertEquals(GroupType.ORGANIZATION, group.getType());
        assertEquals("testRef", group.getReference());
        assertTrue(group.getInGroupIds().isEmpty());
        assertFalse(group.isValid());
    }

    @Test
    void testGroupConstructorFromId() {
        Group group = new Group("org:testOrganization");

        assertEquals("org:testOrganization", group.getId());
        assertEquals(GroupType.ORGANIZATION, group.getType());
        assertEquals("testOrganization", group.getReference());
        assertTrue(group.getInGroupIds().isEmpty());
        assertTrue(group.isValid());
    }

    @Test
    void testGroupGettersAndSetters() {
        Group group = new Group();
        group.setId("idd");
        group.setName("namee");
        group.setReference("reff");
        group.setType(GroupType.ROLE);

        assertEquals("idd", group.getId());
        assertEquals(GroupType.ROLE, group.getType());
        assertEquals("namee", group.getName());
        assertEquals("reff", group.getReference());
        assertFalse(group.isValid());
    }

    @Test
    void testGroupCopyConstructor() {
        Group group = new Group("ID", GroupType.ORGANIZATION_ADMIN, "REF");
        group.setName("NAME");

        Group groupCopied = new Group(group);
        assertEquals("ID", groupCopied.getId());
        assertEquals(GroupType.ORGANIZATION_ADMIN, groupCopied.getType());
        assertEquals("NAME", groupCopied.getName());
        assertEquals("REF", groupCopied.getReference());
        assertFalse(group.isValid());
    }

    @Test
    void testUserPreferenceEntityEquals() {
        EqualsVerifier.simple()
                .forClass(Group.class)
                .suppress(Warning.SURROGATE_OR_BUSINESS_KEY)
                .verify();
    }
}
