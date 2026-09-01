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

package net.ihe.gazelle.user.management.api.application.group;

import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.UnauthorizedException;
import net.ihe.gazelle.user.management.api.domain.group.Group;
import net.ihe.gazelle.user.management.api.domain.group.GroupType;

import java.util.Set;

/**
 * This service has the responsibility to manage groups in GUM (CRUD + join and leave)
 */
public interface GroupService {

    /**
     * Search for groups
     * @param search search pattern (id + name)
     * @param type the type of group
     * @param offset the offset of the first result
     * @param limit the max number of result
     * @param identity the identity of the action request
     * @return the set of groups corresponding to the search criteria
     * @throws UnauthorizedException if the given identity is not authorized to perform the action
     */
    Set<Group> searchForGroup(String search, GroupType type, Integer offset, Integer limit, GazelleIdentity identity);

    /**
     * Retrieve the group corresponding to the given id
     * @param groupId the id of the group to retrieve
     * @param identity the identity of the action request
     * @return the group corresponding to the given id
     * @throws GroupServiceException if the retrieve fails
     * @throws UnauthorizedException if the given identity is not authorized to perform the action
     */
    Group getGroupById(String groupId, GazelleIdentity identity);

    /**
     * Create a group
     * @param group the group to create
     * @param identity the identity of the action request
     * @return created Group
     * @throws IllegalArgumentException if the provided group is not valid
     * @throws GroupServiceException if the creation fails
     * @throws UnauthorizedException if the given identity is not authorized to perform the action
     */
    Group createGroup(Group group, GazelleIdentity identity);

    /**
     * Update the name or/and the groups membership of the given group id
     * @param groupId the id of the group to update
     * @param name the new name of the group
     * @param groupIds the new group membership of the group
     * @param identity the identity of the action request
     * @return the updated Group
     * @throws GroupServiceException if the creation fails
     * @throws UnauthorizedException if the given identity is not authorized to perform the action
     */
    Group updateGroup(String groupId, String name, Set<String> groupIds, GazelleIdentity identity);

    /**
     * Delete the group associated to the given id
     * @param groupId the id of the group to delete
     * @param identity the identity of the action request
     * @throws GroupServiceException if the creation fails
     * @throws UnauthorizedException if the given identity is not authorized to perform the action
     */
    void deleteGroup(String groupId, GazelleIdentity identity);

    /**
     * Join a group
     * @param userId the id of the user to join the group
     * @param groupId the id of the group to join
     * @param identity the identity of the action request
     * @throws IllegalStateException if the group doesn't exist
     * @throws GroupServiceException if the join fails
     * @throws UnauthorizedException if the given identity is not authorized to perform the action
     */
    void joinGroup(String userId, String groupId, GazelleIdentity identity);

    /**
     * Leave a group
     * @param userId the id of the user to leave the group
     * @param groupId the id of the group to leave
     * @param identity the identity of the action request
     * @throws IllegalStateException if the group doesn't exist
     * @throws GroupServiceException if the leave fails
     * @throws UnauthorizedException if the given identity is not authorized to perform the action
     */
    void leaveGroup(String userId, String groupId, GazelleIdentity identity);
}
