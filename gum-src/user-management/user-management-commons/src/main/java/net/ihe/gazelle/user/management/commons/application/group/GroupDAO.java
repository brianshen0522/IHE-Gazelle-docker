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

import net.ihe.gazelle.user.management.api.domain.group.Group;
import net.ihe.gazelle.user.management.api.domain.group.GroupType;
import net.ihe.gazelle.user.management.api.application.ConflictException;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;

import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Interface defining the data access operations for groups.
 */
public interface GroupDAO {

    /**
     * Search for groups
     * @param search search pattern (id + name)
     * @param type   the type of group
     * @param offset the offset of the first result
     * @param limit  the max number of result
     * @return the set of groups matching the search criteria
     */
    Set<Group> searchForGroup(String search, GroupType type, Integer offset, Integer limit);

    /**
     * Retrieve a group
     * @param groupId the id of the group to update
     * @return the retrieved group
     * @throws NoSuchElementException if the group is not found
     * @throws GazelleDAOException    if the retrieve fails
     */
    Group getGroupById(String groupId);

    /**
     * Create a group
     * @param group the group to create
     * @return the created group
     * @throws ConflictException   if the group already exist
     * @throws GazelleDAOException if the creation fails
     */
    Group createGroup(Group group);

    /**
     * Update a group
     * @param groupId  the id of the group to update
     * @param name     the new group name
     * @param groupIds the ids of the groups that the group to update is in
     * @return the updated group
     * @throws NoSuchElementException if the group is not found
     * @throws GazelleDAOException    if the creation fails
     */
    Group updateGroup(String groupId, String name, Set<String> groupIds);

    /**
     * Delete a group
     *
     * @param groupId the id of the group to delete
     * @throws NoSuchElementException if the group is not found
     * @throws GazelleDAOException    if the deletion fails
     */
    void deleteGroup(String groupId);

    /**
     * Join a group
     * @param userId  the id of the user to join the group
     * @param groupId the id of the group to join
     * @throws NoSuchElementException if the group is not found
     */
    void joinGroup(String userId, String groupId);

    /**
     * Leave a group
     * @param userId  the id of the user to leave the group
     * @param groupId the id of the group to leave
     * @throws NoSuchElementException if the group is not found or is the group is not present in user groups
     */
    void leaveGroup(String userId, String groupId);

}
