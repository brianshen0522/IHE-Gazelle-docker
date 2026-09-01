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

package net.ihe.gazelle.user.management.commons.interlay.dao.entities;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    @Test
    void testConstructorAndGetter(){
        UserEntity userEntity = new UserEntity();
        userEntity.setId("id-user");
        userEntity.setFirstName("firstname-user");
        userEntity.setLastName("lastname-user");
        userEntity.setEmail("user@test.fr");
        userEntity.setActivationCode("user-activation-code");
        userEntity.setLastLoginTimestamp(new Timestamp(1000));
        userEntity.setLastUpdateTimestamp(new Timestamp(2000));
        userEntity.setRegistrationTimestamp(new Timestamp(3000));
        userEntity.setActivated(true);
        userEntity.setLoginCounter(50000);

        assertEquals("id-user",userEntity.getId());
        assertEquals("firstname-user",userEntity.getFirstName());
        assertEquals("lastname-user",userEntity.getLastName());
        assertEquals("user@test.fr", userEntity.getEmail());
        assertEquals("user-activation-code", userEntity.getActivationCode());
        assertEquals(new Timestamp(1000), userEntity.getLastLoginTimestamp());
        assertEquals(new Timestamp(2000), userEntity.getLastUpdateTimestamp());
        assertEquals(new Timestamp(3000), userEntity.getRegistrationTimestamp());
        assertEquals(50000, userEntity.getLoginCounter());
        assertTrue(userEntity.isActivated());
    }

    @Test
    void testGroupEntitiesAssociation(){
        UserEntity userEntity = new UserEntity();
        userEntity.setId("id-user");
        GroupEntity groupEntity1 = new GroupEntity("role:newrole");
        userEntity.setGroupEntities(Set.of(groupEntity1));

        GroupEntity groupEntity2 = new GroupEntity("role:newrole2");
        userEntity.addGroupEntity(groupEntity2);
        assertEquals(2,userEntity.getGroupEntities().size());
        assertTrue(userEntity.getGroupEntities().contains(groupEntity2));

        userEntity.removeGroupEntity(groupEntity2);
        assertEquals(1,userEntity.getGroupEntities().size());
        assertFalse(userEntity.getGroupEntities().contains(groupEntity2));

        userEntity.removeGroupEntity(groupEntity2);
        assertEquals(1,userEntity.getGroupEntities().size());
    }

    @Test
    void testRecursiveGroups(){
        GroupEntity groupEntity1 = new GroupEntity("role:superRole");
        groupEntity1.setInGroupIds(Set.of("role:subSuperRole"));

        GroupEntity groupEntity2 = new GroupEntity("role:strongRole");
        groupEntity2.setInGroupIds(Set.of("role:subStrongRole"));

        UserEntity userEntity = new UserEntity();
        userEntity.setId("id-user");
        userEntity.setGroupEntities(Set.of(groupEntity1, groupEntity2));

        Set<String> userGroupIds = userEntity.asUser().getGroupIds();
        assertTrue(userGroupIds.contains("role:superRole"));
        assertTrue(userGroupIds.contains("role:subSuperRole"));
        assertTrue(userGroupIds.contains("role:strongRole"));
        assertTrue(userGroupIds.contains("role:subStrongRole"));
        assertEquals(4, userGroupIds.size());
    }
}
