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

package net.ihe.gazelle.user.management.core.interlay.transactional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.group.GroupService;
import net.ihe.gazelle.user.management.api.domain.group.Group;
import net.ihe.gazelle.user.management.api.domain.group.GroupType;
import net.ihe.gazelle.user.management.commons.application.group.GroupDAO;
import net.ihe.gazelle.user.management.commons.application.group.GroupServiceImpl;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;

import java.util.Set;

/**
 * A transactional implementation of the GroupService interface. This class is responsible for managing groups and their members,
 * ensuring that all operations are performed within a transactional context to maintain data integrity.
 */
@RequestScoped
public class GroupServiceTransactional implements GroupService {

    private final GroupService groupService;

    /**
     * Constructor for GroupServiceTransactional. It initializes the underlying GroupService implementation with the provided
     * GroupDAO and Authz instances.
     * @param authz the authorization service used to check permissions
     * @param groupDAO the DAO used to perform group operations
     * @param userLookupDAO the DAO used to perform user lookup operations
     */
    @Inject
    public GroupServiceTransactional(Authz authz, GroupDAO groupDAO, UserLookupDAO userLookupDAO) {
        this.groupService = new GroupServiceImpl(authz, groupDAO, userLookupDAO);
    }

    @Override
    public Set<Group> searchForGroup(String search, GroupType type, Integer offset, Integer limit, GazelleIdentity identity) {
        return groupService.searchForGroup(search, type, offset, limit, identity);
    }

    @Override
    public Group getGroupById(String groupId, GazelleIdentity identity) {
        return groupService.getGroupById(groupId, identity);
    }

    @Override
    @Transactional
    public Group createGroup(Group group, GazelleIdentity identity) {
        return groupService.createGroup(group, identity);
    }

    @Override
    @Transactional
    public Group updateGroup(String groupId, String name, Set<String> groupIds, GazelleIdentity identity) {
        return groupService.updateGroup(groupId, name, groupIds, identity);
    }

    @Override
    @Transactional
    public void deleteGroup(String groupId, GazelleIdentity identity) {
        groupService.deleteGroup(groupId, identity);
    }

    @Override
    @Transactional
    public void joinGroup(String userId, String groupId, GazelleIdentity identity) {
        groupService.joinGroup(userId, groupId, identity);
    }

    @Override
    @Transactional
    public void leaveGroup(String userId, String groupId, GazelleIdentity identity) {
        groupService.leaveGroup(userId, groupId, identity);
    }
}
