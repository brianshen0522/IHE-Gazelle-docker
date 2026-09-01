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

package net.ihe.gazelle.user.management.quarkus.interlay.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.group.GroupService;
import net.ihe.gazelle.user.management.api.domain.group.Group;
import net.ihe.gazelle.user.management.api.domain.group.GroupType;
import net.ihe.gazelle.user.management.api.interlay.group.GroupResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

import static net.ihe.gazelle.user.management.quarkus.interlay.ControllerSyntaxHelper.executeActionAndCatchPotentialException;
import static org.apache.http.HttpHeaders.CONTENT_RANGE;

/**
 * Controller implementation for managing groups.
 */
public class GroupControllerImpl implements GroupController {

    /** Error message used when a group search fails. */
    public static final String UNABLE_TO_SEARCH_FOR_GROUPS = "Unable to search for groups";
    /** Error message used when a group creation fails. */
    public static final String UNABLE_TO_CREATE_GROUP = "Unable to create group";
    /** Error message used when a group deletion fails. */
    public static final String UNABLE_TO_DELETE_GROUP = "Unable to delete group";
    /** Error message used when a group update fails. */
    public static final String UNABLE_TO_UPDATE_GROUP = "Unable to update group";

    private final GroupService groupService;
    private final GazelleIdentity gzlIdentity;

    /**
     * Creates a controller instance wired with the required services.
     * @param groupService service handling group operations
     * @param identity current Gazelle identity
     */
    @Inject
    public GroupControllerImpl(GroupService groupService, GazelleIdentity identity) {
        this.groupService = groupService;
        this.gzlIdentity = identity;
    }

    private final Logger logger = LoggerFactory.getLogger(GroupControllerImpl.class.getName());

    @Override
    public Response createGroup(GroupResource groupResource) {
        return executeActionAndCatchPotentialException(gzlIdentity, logger, UNABLE_TO_CREATE_GROUP, () -> {

            Group createdGroup = groupService.createGroup(groupResource.asGroup(), gzlIdentity);
            return Response.ok().status(Response.Status.CREATED).entity(new GroupResource(createdGroup)).build();
        });
    }

    @Override
    public Response searchForGroups(String search, String type, Integer offset, Integer limit) {
        return executeActionAndCatchPotentialException(gzlIdentity, logger, UNABLE_TO_SEARCH_FOR_GROUPS, () -> {
            // If type parameter is null, we perform search with null value to retrieve groups of any group type
            GroupType groupType = type != null ? GroupType.getTypeFromPrefix(type) : null;

            Set<Group> groups = groupService.searchForGroup(search, groupType, offset, limit, gzlIdentity);
            Set<GroupResource> groupResources = groups.stream().map(GroupResource::new).collect(Collectors.toSet());
            return Response.ok(groupResources)
                    .header(CONTENT_RANGE, "groups " + offset + "-" + limit + "/" + groups.size()).build();
        });
    }

    @Override
    public Response getGroupById(String groupId) {
        return executeActionAndCatchPotentialException(gzlIdentity, logger, UNABLE_TO_SEARCH_FOR_GROUPS, () -> {
            Group group = groupService.getGroupById(groupId, gzlIdentity);
            return Response.ok(new GroupResource(group)).build();
        });
    }

    @Override
    public Response updateGroup(String groupId, GroupResource groupResource) {
        return executeActionAndCatchPotentialException(gzlIdentity, logger, UNABLE_TO_UPDATE_GROUP, () -> {

            if (groupResource == null)
                throw new IllegalArgumentException("Missing group resource");
            Group group = groupService.updateGroup(groupId, groupResource.getName(), groupResource.getInGroupIds(), gzlIdentity);
            return Response.ok().status(Response.Status.OK).entity(new GroupResource(group)).build();
        });
    }

    @Override
    public Response deleteGroup(String groupId) {
        return executeActionAndCatchPotentialException(gzlIdentity, logger, UNABLE_TO_DELETE_GROUP, () -> {

            groupService.deleteGroup(groupId, gzlIdentity);
            return Response.ok().status(Response.Status.OK).build();
        });
    }
}
