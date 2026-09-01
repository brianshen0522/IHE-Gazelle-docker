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

package net.ihe.gazelle.user.management.api.domain.group;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a group in the Gazelle User Management system.
 * <p>
 * A group can be of different types (organization, role, etc.) and may contain subgroups.
 * </p>
 */
public class Group {
    /** Unique identifier for the group. */
    protected String id;
    /** Reference for the group (e.g., name or code). */
    protected String reference;
    /** Type of the group. */
    protected GroupType type;
    /** Name of the group. */
    protected String name;
    /** Identifiers of groups included in this group. */
    protected Set<String> inGroupIds;

    /**
     * Default constructor.
     * Initializes the set of included groups.
     */
    public Group() {
        this.inGroupIds = new HashSet<>();
    }

    /**
     * Constructor with group identifier.
     * Parses the type and reference from the identifier.
     *
     * @param id the group identifier
     * @throws IllegalArgumentException if the identifier format is invalid
     */
    public Group(String id) {
        this();
        this.id = id;
        String[] elements = this.id.split(":");
        if (elements.length == 2) {
            this.type = GroupType.getTypeFromPrefix(elements[0]);
            this.reference = elements[1];
        } else
            throw new IllegalArgumentException("Id is not valid");
    }

    /**
     * Checks if the group is valid.
     *
     * @return true if the group is valid, false otherwise
     */
    public boolean isValid() {
        if (this.id == null || this.type == null || this.reference == null)
            return false;
        String[] elements = this.id.split(":");
        if (elements.length == 2)
            return this.type.getPrefix().equals(elements[0]) && this.reference.equals(elements[1]);
         else
            return false;
    }

    /**
     * Constructor with type and reference.
     *
     * @param type the group type
     * @param reference the group reference
     */
    public Group(GroupType type, String reference) {
        this();
        this.id = type.getPrefix() + ":" + reference;
        this.reference = reference;
        this.type = type;
    }

    /**
     * Constructor with identifier, type, and reference.
     *
     * @param id the group identifier
     * @param type the group type
     * @param reference the group reference
     */
    public Group(String id, GroupType type, String reference) {
        this();
        this.id = id;
        this.type = type;
        this.reference = reference;
    }

    /**
     * Copy constructor.
     * Creates a new Group by copying all fields from another Group.
     *
     * @param roleToCopy the group to copy
     */
    public Group(Group roleToCopy){
        this.id = roleToCopy.getId();
        this.reference = roleToCopy.getReference();
        this.name = roleToCopy.getName();
        this.type = roleToCopy.getType();
        this.inGroupIds = roleToCopy.getInGroupIds();
    }

    /**
     * Gets the group identifier.
     * @return the group id
     */
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
     * Gets the group reference.
     * @return the group reference
     */
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
     * Gets the group type.
     * @return the group type
     */
    public GroupType getType() {
        return type;
    }

    /**
     * Sets the group type.
     * @param type the group type
     */
    public void setType(GroupType type) {
        this.type = type;
    }

    /**
     * Gets the group name.
     * @return the group name
     */
    public String getName() {return name; }

    /**
     * Sets the group name.
     * @param name the group name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Adds a group id to the set of included groups.
     * @param groupId the group id to add
     */
    public void addGroupId(String groupId) { inGroupIds.add(groupId); }

    /**
     * Gets a copy of the set of included group ids.
     * @return a new HashSet of included group ids
     */
    public Set<String> getInGroupIds() { return new HashSet<>(inGroupIds); }

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
     * Sets the included group ids (alias for setInGroupIds).
     * @param groupIds the set of included group ids
     */
    public void setGroupIds(Set<String> groupIds) {
        this.inGroupIds = groupIds == null
                ? new HashSet<>()
                : new HashSet<>(groupIds);
    }

    /**
     * Checks equality with another object.
     * @param o the object to compare
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return Objects.equals(id, group.id) && Objects.equals(reference, group.reference)
                && type == group.type && Objects.equals(name, group.name) && Objects.equals(inGroupIds, group.inGroupIds);
    }

    /**
     * Computes the hash code for this group.
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, reference, type, name, inGroupIds);
    }
}
