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

package net.ihe.gazelle.user.management.api.interlay.group;

import net.ihe.gazelle.user.management.api.domain.group.Group;
import net.ihe.gazelle.user.management.api.domain.group.GroupType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupResourceTest {

    @Test
    void testConstructor() {
        Group group = new Group("role:super_role");
        group.setName("super_role name");
        GroupResource groupResource = new GroupResource(group);

        assertEquals("role", groupResource.getType());
        assertEquals("super_role", groupResource.getReference());
        assertEquals("super_role name", groupResource.getName());
        assertTrue(groupResource.getInGroupIds().isEmpty());
    }

    @Test
    void testGettersAndSetters() {
        GroupResource groupResource = new GroupResource();
        groupResource.setType("type");
        groupResource.setName("name");
        groupResource.setReference("reference");
        groupResource.setInGroupIds(Set.of("groupId1"));

        assertEquals("type", groupResource.getType());
        assertEquals("name", groupResource.getName());
        assertEquals("reference", groupResource.getReference());
        assertTrue(groupResource.getInGroupIds().contains("groupId1"));
    }

    @Test
    void testAsGroup() {
        GroupResource groupResource = new GroupResource();
        groupResource.setType("org");
        groupResource.setName("name1");
        groupResource.setReference("reference1");

        Group group = groupResource.asGroup();
        assertEquals(GroupType.ORGANIZATION, group.getType());
        assertEquals("reference1", group.getReference());
        assertEquals("name1", group.getName());
        assertEquals("org:reference1", group.getId());
    }

    @Test
    void testToJson() {
        String type = "type2";
        String reference = "reference2";
        GroupResource groupResource = new GroupResource();
        groupResource.setId(type + ":" + reference);
        groupResource.setType(type);
        groupResource.setReference(reference);

        String expectedResult = "{\"id\":\"" + type + ":" + reference + "\",\"reference\":\"" + reference + "\",\"type\":\"" + type + "\"}";
        assertEquals(expectedResult, groupResource.toJson());

        groupResource.setName("name2");
        expectedResult = "{\"id\":\"" + type + ":" + reference + "\",\"name\":\"name2\",\"reference\":\"" + reference + "\",\"type\":\"" + type + "\"}";
        assertEquals(expectedResult, groupResource.toJson());
    }
}
