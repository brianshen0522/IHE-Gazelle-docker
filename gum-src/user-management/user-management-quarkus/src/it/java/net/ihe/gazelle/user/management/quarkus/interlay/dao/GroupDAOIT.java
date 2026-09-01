package net.ihe.gazelle.user.management.quarkus.interlay.dao;


import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.user.management.api.domain.group.Group;
import net.ihe.gazelle.user.management.api.domain.group.GroupType;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.group.GroupDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActivateRequestContext
@Transactional
class GroupDAOIT {

    @Inject
    GroupDAO groupDAO;
    @Inject
    UserRegistrationDAO userRegistrationDAO;
    @Inject
    UserLookupDAO userLookupDAO;

    private final String PREFIX = getClass().getSimpleName();
    private final String TEST_USER_ID = PREFIX +"ID";
    private static final String TEST_ORG_ID3 = "org:myGroup3ForTesting";
    private static final String MY_GROUP_FOR_TESTING = "My group for testing";
    private static final String MY_GROUP_SEARCH_FOR_TESTING = "My group search for testing";

    @Test
    void testCreateGroup() {
        String TEST_ORG_ID1 = "org:myGroupForTesting";
        String TEST_INORG_ID = "org:myInGroupForTesting";
        Group group = new Group(TEST_ORG_ID1);
        group.setName(MY_GROUP_FOR_TESTING);
        group.addGroupId(TEST_INORG_ID);
        groupDAO.createGroup(group);

        Set<Group> groups = groupDAO.searchForGroup(MY_GROUP_FOR_TESTING,null,0,1);
        assertEquals(1,groups.size());
        assertTrue(groups.stream().anyMatch(group1 -> MY_GROUP_FOR_TESTING.equals(group1.getName())));
        assertTrue(groups.stream().anyMatch(group1 -> group1.getInGroupIds().contains(TEST_INORG_ID)));
    }

    @Test
    void testSearchGroup() {
        Group group = new Group("org:myGroupSearchForTesting");
        group.setName(MY_GROUP_SEARCH_FOR_TESTING);
        groupDAO.createGroup(group);

        Set<Group> groups = groupDAO.searchForGroup(null, GroupType.ORGANIZATION,0,50);
        assertTrue(groups.stream().anyMatch(group1 -> MY_GROUP_SEARCH_FOR_TESTING.equals(group1.getName())));

        groups = groupDAO.searchForGroup(MY_GROUP_SEARCH_FOR_TESTING, null,0,10);
        assertTrue(groups.stream().anyMatch(group1 -> MY_GROUP_SEARCH_FOR_TESTING.equals(group1.getName())));

        groups = groupDAO.searchForGroup("testing", null, 0, 10);
        assertTrue(groups.stream().anyMatch(group1 -> MY_GROUP_SEARCH_FOR_TESTING.equals(group1.getName())));

        groups = groupDAO.searchForGroup(MY_GROUP_SEARCH_FOR_TESTING, GroupType.ORGANIZATION,0,10);
        assertTrue(groups.stream().anyMatch(group1 -> MY_GROUP_SEARCH_FOR_TESTING.equals(group1.getName())));

        groups = groupDAO.searchForGroup(MY_GROUP_SEARCH_FOR_TESTING, GroupType.ORGANIZATION,0,50);
        assertTrue(groups.stream().anyMatch(group1 -> MY_GROUP_SEARCH_FOR_TESTING.equals(group1.getName())));
    }

    @Test
    void testGetGroupById() {
        Group group = new Group("org:myRetrievedGroupForTesting");
        group.setName("My group is well retrieved");
        groupDAO.createGroup(group);

        assertThrows(NoSuchElementException.class, () -> groupDAO.getGroupById("badId"));
        Group retrievedGroup = groupDAO.getGroupById("org:myRetrievedGroupForTesting");
        assertEquals("My group is well retrieved", retrievedGroup.getName());

    }


    @Test
    void testUpdateGroup() {
        Group group = new Group("org:myUpdatedGroupForTesting");
        group.setName(MY_GROUP_FOR_TESTING);
        Set<String> inGroup = new HashSet<>();
        inGroup.add("org:ingroup");
        group.setInGroupIds(inGroup);
        groupDAO.createGroup(group);

        Set<String> inGroupIds = new HashSet<>();
        inGroupIds.add("myInGroup");
        Group updatedGroup = groupDAO.updateGroup("org:myUpdatedGroupForTesting", "New name", inGroupIds);
        assertEquals("New name", updatedGroup.getName());
        assertEquals(GroupType.ORGANIZATION, updatedGroup.getType());

        Set<Group> groups = groupDAO.searchForGroup("org:myUpdatedGroupForTesting", null,0,10);
        assertTrue(groups.stream().anyMatch(grp -> grp.getInGroupIds().contains("myInGroup")));
        assertTrue(groups.stream().anyMatch(grp -> grp.getName().equals("New name")));
    }

    @Test
    void testDeleteGroup() {
        Group group = new Group("org:myDeletedGroupForTesting");
        group.setName(MY_GROUP_FOR_TESTING);
        Set<String> inGroup = new HashSet<>();
        inGroup.add("org:inGroup");
        group.setInGroupIds(inGroup);
        groupDAO.createGroup(group);

        groupDAO.deleteGroup("org:myDeletedGroupForTesting");
        Set<Group> groups = groupDAO.searchForGroup("org:myDeletedGroupForTesting", null,0,1);
        assertTrue(groups.isEmpty());
    }

    @Test
    void testJoinGroupBadArguments() {
        assertThrows(NoSuchElementException.class, () -> groupDAO.joinGroup("badUserId", TEST_ORG_ID3));
        assertThrows(NoSuchElementException.class, () -> groupDAO.joinGroup(TEST_USER_ID,"badGroupId"));
    }

    @Test
    void testLeaveGroupBadArguments() {
        assertThrows(NoSuchElementException.class, () -> groupDAO.leaveGroup("badUserId", TEST_ORG_ID3));
        assertThrows(NoSuchElementException.class, () -> groupDAO.leaveGroup(TEST_USER_ID,"badGroupId"));
    }

    @Test
    @TestTransaction // Usage of this transaction to do not impact other test with created data
    void testJoinAndLeaveGroup() {
        createNewUserForTesting(TEST_USER_ID);

        String TEST_ORG_ID4 = "org:myGroup4ForTesting";
        String MY_GROUP5_FOR_TESTING = "My group5 for testing";
        Group group = new Group(TEST_ORG_ID4);
        group.setName(MY_GROUP5_FOR_TESTING);
        groupDAO.createGroup(group);

        groupDAO.joinGroup(TEST_USER_ID, TEST_ORG_ID4);
        User user = userLookupDAO.getUserById(TEST_USER_ID);
        assertFalse(user.getGroupIds().isEmpty());
        assertTrue(user.getGroupIds().stream().anyMatch(group1 -> group1.equals(TEST_ORG_ID4)));

        groupDAO.leaveGroup(TEST_USER_ID, TEST_ORG_ID4);
        assertTrue(userLookupDAO.getUserById(TEST_USER_ID).getGroupIds().isEmpty());
    }

    @Test
    @TestTransaction // Usage of this transaction to do not impact other test with created data
    void testJoinAndLeaveBadState() {
        String TEST_USER_ID2 = PREFIX +"ID2";
        createNewUserForTesting(TEST_USER_ID2);

        String TEST_ORG_ID5 = "org:myGroup5ForTesting";
        Group group = new Group(TEST_ORG_ID5);
        groupDAO.createGroup(group);

        // Test join
        groupDAO.joinGroup(TEST_USER_ID2, TEST_ORG_ID5);
        groupDAO.joinGroup(TEST_USER_ID2, TEST_ORG_ID5);
        User user = userLookupDAO.getUserById(TEST_USER_ID2);
        assertEquals(1, user.getGroupIds().size());
        assertTrue(user.getGroupIds().stream().anyMatch(group1 -> group1.equals(TEST_ORG_ID5)));

        // Test leave
        groupDAO.leaveGroup(TEST_USER_ID2, TEST_ORG_ID5);
        Exception e = assertThrows(NoSuchElementException.class, () -> groupDAO.leaveGroup(TEST_USER_ID2, TEST_ORG_ID5));
        assertTrue(e.getMessage().contains("not found in user groups"));
    }

    private void createNewUserForTesting(String userId) {
        User newUser = new User(userId);
        newUser.setFirstName("testFirstName");
        newUser.setLastName("testLastName");
        newUser.setEmail(PREFIX+"@test.fr");
        newUser.setActivated(false);
        newUser.setActivationCode("roleDAOSecretActivationCode");
        userRegistrationDAO.registerUser(newUser);
    }
}
