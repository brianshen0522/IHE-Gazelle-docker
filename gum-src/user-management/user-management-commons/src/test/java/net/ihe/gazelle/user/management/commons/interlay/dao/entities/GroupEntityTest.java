package net.ihe.gazelle.user.management.commons.interlay.dao.entities;

import net.ihe.gazelle.user.management.api.domain.group.Group;
import net.ihe.gazelle.user.management.api.domain.group.GroupType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupEntityTest {

    @Test
    void testGroupEntitiesFromGroup() {
        Group group = new Group("ID", GroupType.ORGANIZATION, "REF");
        group.setName("NAME");

        GroupEntity groupEntity = new GroupEntity(group);
        assertEquals(group.getId(), groupEntity.getId());
        assertEquals(group.getType(), groupEntity.getType());
        assertEquals(group.getReference(), groupEntity.getReference());
        assertEquals(group.getName(), groupEntity.getName());
        assertEquals(group.getInGroupIds(), groupEntity.getInGroupIds());
    }

    @Test
    void testGroupEntitiesAsGroup() {
        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setId("ID");
        groupEntity.setName("NAME");
        groupEntity.setReference("REFERENCE");
        groupEntity.setType(GroupType.ORGANIZATION_ADMIN);

        Group group = groupEntity.asGroup();
        assertEquals(groupEntity.getId(), group.getId());
        assertEquals(groupEntity.getType(), group.getType());
        assertEquals(groupEntity.getReference(), group.getReference());
        assertEquals(groupEntity.getName(), group.getName());
        assertEquals(groupEntity.getInGroupIds(), group.getInGroupIds());
    }
}