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

package net.ihe.gazelle.user.management.commons.interlay.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import net.ihe.gazelle.user.management.api.domain.group.Group;
import net.ihe.gazelle.user.management.api.domain.group.GroupType;
import net.ihe.gazelle.user.management.api.application.ConflictException;
import net.ihe.gazelle.user.management.commons.application.group.GroupDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.GroupEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.UserEntity;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage.USER_NOT_FOUND;

/**
 * Implementation of the GroupDAO interface using JPA EntityManager for database operations.
 */
public class GroupDAOImpl implements GroupDAO {

    private static final String TYPE_SQL_PARAM = "type";
    private static final String SEARCH_SQL_PARAM = "search";
    public static final String GROUP_NOT_FOUND = "Group not found";
    private final EntityManager em;

    public GroupDAOImpl(EntityManager em) { this.em = em; }

    @Override
    public Set<Group> searchForGroup(String search, GroupType type, Integer offset, Integer limit) {
        StringBuilder queryString = getQueryStringFromParameter(search, type);
        Query query = em.createQuery(queryString.toString());
        query = queryString.toString().contains(":"+TYPE_SQL_PARAM) ? query.setParameter(TYPE_SQL_PARAM, type) : query;
        query = queryString.toString().contains(":"+SEARCH_SQL_PARAM) ? query.setParameter(SEARCH_SQL_PARAM, "%"+search.toLowerCase()+"%") : query;

        if (offset != null)
            query.setFirstResult(offset);
        if (limit != null)
            query.setMaxResults(limit);

        List<GroupEntity> groupEntities = query.getResultList();
        return groupEntities.stream().map(GroupEntity::asGroup).collect(Collectors.toSet());
    }

    @Override
    public Group getGroupById(String groupId) {
        GroupEntity groupEntity = em.find(GroupEntity.class, groupId);
        if (groupEntity == null)
            throw new NoSuchElementException("Group with the id " + groupId + " not found");

        return groupEntity.asGroup();
    }

    private static StringBuilder getQueryStringFromParameter(String search, GroupType type) {
        StringBuilder queryString = new StringBuilder();

        if (search == null && type == null)
            queryString.append("SELECT gr FROM GroupEntity gr");
        else if (search != null && type != null)
            queryString.append("SELECT gr FROM GroupEntity gr WHERE (LOWER(gr.id) LIKE :search OR LOWER(gr.name) LIKE :search)  AND gr.type = :type");
        else if (type != null)
            queryString.append("SELECT gr FROM GroupEntity gr WHERE gr.type = :type");
        else // search != null
            queryString.append("SELECT gr FROM GroupEntity gr WHERE LOWER(gr.id) LIKE :search OR LOWER(gr.name) LIKE :search");
        return queryString;
    }

    @Override
    public Group createGroup(Group group) {
        GroupEntity existingGroup = em.find(GroupEntity.class, group.getId());
        if (existingGroup != null)
            throw new ConflictException("Group already exist with the same group id " + group.getId());

        GroupEntity groupEntity = new GroupEntity(group);
        return em.merge(groupEntity).asGroup();
    }

    @Override
    public Group updateGroup(String groupId, String name, Set<String> groupIds) {
        GroupEntity groupEntity = em.find(GroupEntity.class, groupId);
        if (groupEntity == null)
            throw new NoSuchElementException(GROUP_NOT_FOUND + " " + groupId);

        groupEntity.setName(name);
        groupEntity.setInGroupIds(groupIds);
        return em.merge(groupEntity).asGroup();
    }

    @Override
    public void deleteGroup(String groupId) {
        GroupEntity groupEntity = em.find(GroupEntity.class, groupId);
        if (groupEntity == null)
            throw new NoSuchElementException(GROUP_NOT_FOUND + " " + groupId);

        em.remove(groupEntity);
    }

    @Override
    public void joinGroup(String userId, String groupId) {
        UserEntity userEntity = em.find(UserEntity.class, userId);
        if (userEntity == null)
            throw new NoSuchElementException(USER_NOT_FOUND.getMessage());
        GroupEntity groupEntity = em.find(GroupEntity.class, groupId);
        if (groupEntity == null)
            throw new NoSuchElementException(GROUP_NOT_FOUND + " " + groupId);

        userEntity.addGroupEntity(groupEntity);
        em.persist(userEntity);
    }

    @Override
    public void leaveGroup(String userId, String groupId) {
        UserEntity userEntity = em.find(UserEntity.class, userId);
        if (userEntity == null)
            throw new NoSuchElementException(USER_NOT_FOUND.getMessage());

        Optional<GroupEntity> optionalGroupEntity = userEntity.getGroupEntities()
                .stream()
                .filter( (groupEntity -> groupEntity.getId().equals(groupId)))
                .findFirst();

        if (optionalGroupEntity.isEmpty())
            throw new NoSuchElementException("Group ("+groupId+") not found in user groups");
        userEntity.removeGroupEntity(optionalGroupEntity.get());
        em.persist(userEntity);
    }
}
