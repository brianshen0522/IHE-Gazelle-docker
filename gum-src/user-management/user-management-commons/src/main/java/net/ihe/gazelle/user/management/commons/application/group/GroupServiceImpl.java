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

package net.ihe.gazelle.user.management.commons.application.group;

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.Groups;
import net.ihe.gazelle.user.management.api.application.group.GroupService;
import net.ihe.gazelle.user.management.api.domain.group.Group;
import net.ihe.gazelle.user.management.api.domain.group.GroupType;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;

import java.util.Set;

import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.GAZELLE_ADMIN;
import static net.ihe.gazelle.user.management.commons.application.permission.GUMPermissionStore.*;

public class GroupServiceImpl implements GroupService {

    private static final String GROUP_ID_IS_NULL = "Group id is null";
    private static final String USER_ID_IS_NULL = "User id is null";

    private final Authz authz;
    private final GroupDAO groupDAO;
    private final UserLookupDAO userLookupDAO;

    public GroupServiceImpl(Authz authz, GroupDAO groupDAO, UserLookupDAO userLookupDAO) {
        this.authz = authz;
        this.groupDAO = groupDAO;
        this.userLookupDAO = userLookupDAO;
    }

    @Override
    public Set<Group> searchForGroup(String search, GroupType type, Integer offset, Integer limit, GazelleIdentity identity) {
        authz.assertAuthorized(identity, GROUP_READ);
        if (offset == null)
            offset = 0;
        if (limit == null)
            limit = 500;

        return groupDAO.searchForGroup(search, type, offset, limit);
    }

    @Override
    public Group getGroupById(String groupId, GazelleIdentity identity) {
        authz.assertAuthorized(identity, GROUP_READ);
        return groupDAO.getGroupById(groupId);
    }

    @Override
    public Group createGroup(Group group, GazelleIdentity identity) {
        authz.assertAuthorized(identity, GROUP_CREATE);
        assertGroupValid(group);
        return groupDAO.createGroup(group);
    }

    @Override
    public Group updateGroup(String groupId, String name, Set<String> groupIds, GazelleIdentity identity) {
        authz.assertAuthorized(identity, GROUP_UPDATE);
        if (groupId == null)
            throw new IllegalArgumentException(GROUP_ID_IS_NULL);
        return groupDAO.updateGroup(groupId, name, groupIds);
    }

    @Override
    public void deleteGroup(String groupId, GazelleIdentity identity) {
        authz.assertAuthorized(identity, GROUP_DELETE);
        if (groupId == null)
            throw new IllegalArgumentException(GROUP_ID_IS_NULL);

        if (groupId.contains(Groups.PREFIX_ORGANIZATION))
            authz.assertAuthorized(identity, USER_ORGANIZATION_UPDATE);

        groupDAO.deleteGroup(groupId);
    }

    @Override
    public void joinGroup(String userId, String groupId, GazelleIdentity identity) {
        if (groupId == null)
            throw new IllegalArgumentException(GROUP_ID_IS_NULL);
        if (userId == null)
            throw new IllegalArgumentException(USER_ID_IS_NULL);

        User targetUser = userLookupDAO.getUserById(userId);
        authz.assertAuthorized(identity, USER_GROUP_UPDATE, groupId, targetUser, false);
        groupDAO.joinGroup(userId, groupId);
    }

    @Override
    public void leaveGroup(String userId, String groupId, GazelleIdentity identity) {
        if (groupId == null)
            throw new IllegalArgumentException(GROUP_ID_IS_NULL);
        if (userId == null)
            throw new IllegalArgumentException(USER_ID_IS_NULL);

        User targetUser = userLookupDAO.getUserById(userId);
        boolean isGazelleAdminRoleToBeRemoved = GAZELLE_ADMIN.getName().equals(groupId);
        authz.assertAuthorized(identity, USER_GROUP_UPDATE, groupId, targetUser, isGazelleAdminRoleToBeRemoved);
        groupDAO.leaveGroup(userId, groupId);
    }

    private void assertGroupValid(Group group) {
        if (group == null)
            throw new IllegalArgumentException("Group is null");
        if (!group.isValid())
            throw new IllegalArgumentException("Group is not valid");
        if (!group.getId().startsWith(group.getType().getPrefix() + ":"))
            throw new IllegalArgumentException("Group id is not starting with type");
        if (!group.getId().endsWith(group.getReference()))
            throw new IllegalArgumentException("Group id is not ending with reference");
    }
}
