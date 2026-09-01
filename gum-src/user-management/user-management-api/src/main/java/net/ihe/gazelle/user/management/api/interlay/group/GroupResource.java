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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.user.management.api.domain.group.Group;
import net.ihe.gazelle.user.management.api.domain.group.GroupType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.HashSet;
import java.util.Set;

/**
 * Resource representation of a group for API responses in Gazelle User Management.
 * <p>
 * This class is used to transfer group data between the backend and clients,
 * including group type, reference, name, and included group IDs.
 * </p>
 */
@Schema(name = "GroupResource", description = "Represent a group")
@JsonPropertyOrder({"id", "type", "reference", "name", "inGroupIds"})
public class GroupResource {
    /** Unique identifier for the group. */
    private String id;
    /** Type of the group as a string prefix. */
    private String type;
    /** Reference for the group (e.g., name or code). */
    private String reference;
    /** Name of the group. */
    private String name;
    /** Identifiers of groups included in this group. */
    private Set<String> inGroupIds;

    /**
     * Default constructor.
     * Initializes the set of included groups.
     */
    public GroupResource() {
        this.inGroupIds = new HashSet<>();
    }

    /**
     * Constructs a GroupResource from a Group domain object.
     * @param group the Group domain object
     */
    public GroupResource(Group group) {
        this.id = group.getId();
        this.type = group.getType().getPrefix();
        this.reference = group.getReference();
        this.name = group.getName();
        this.inGroupIds = group.getInGroupIds();
    }

    /**
     * Gets the group identifier.
     * @return the group id
     */
    @Schema(
            description = "The group id.",
            required = true,
            examples = {"role:my_role"}
    )
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * Sets the group identifier.
     * @param id the group id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the group type as a string prefix.
     * @return the group type
     */
    @Schema(
            description = "The group type.",
            required = true,
            examples = {"role"}
    )
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * Sets the group type as a string prefix.
     * @param type the group type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the group reference.
     * @return the group reference
     */
    @Schema(
            description = "The group reference.",
            required = true,
            examples = {"my_role"}
    )
    @JsonProperty("reference")
    public String getReference() {
        return reference;
    }

    /**
     * Sets the group reference.
     * @param reference the group reference
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Gets the group name.
     * @return the group name
     */
    @Schema(
            description = "The group name.",
            required = true,
            examples = {"My role"}
    )
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Sets the group name.
     * @param name the group name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets a copy of the set of included group ids.
     * @return a new HashSet of included group ids
     */
    @Schema(
            description = "The set of included group ids."
    )
    @JsonProperty("inGroupIds")
    public Set<String> getInGroupIds() {
        return new HashSet<>(inGroupIds);
    }

    /**
     * Sets the included group ids.
     * @param inGroupIds the set of included group ids
     */
    public void setInGroupIds(Set<String> inGroupIds) {
        this.inGroupIds = inGroupIds == null
                ? new HashSet<>()
                : new HashSet<>(inGroupIds);
    }

    /**
     * Convert this resource to a domain Group instance.
     *
     * @return a new Group populated with the values from this resource
     */
    public Group asGroup() {
        Group group = new Group(GroupType.getTypeFromPrefix(type), this.reference);
        group.setName(this.name);
        inGroupIds.forEach(group::addGroupId);
        return group;
    }

    /**
     * Return a compact JSON representation of this resource.
     * <p>
     * Used for simple logging/debugging and HTTP payloads in places where a full JSON
     * serializer is not required.
     * </p>
     *
     * @return a JSON string representing this GroupResource
     */
    public String toJson() {
        StringBuilder stringBuilder = new StringBuilder("{");
        stringBuilder.append("\"id\":\"").append(this.id).append("\",");
        if (name != null)
            stringBuilder.append("\"name\":\"").append(this.name).append("\",");
        if (reference != null)
            stringBuilder.append("\"reference\":\"").append(this.reference).append("\",");
        if (type != null)
            stringBuilder.append("\"type\":\"").append(this.type).append("\",");

        return stringBuilder.append("}").toString().replace(",}", "}");
    }
}
