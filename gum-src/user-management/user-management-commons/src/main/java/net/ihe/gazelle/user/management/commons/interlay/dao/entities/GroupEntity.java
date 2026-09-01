package net.ihe.gazelle.user.management.commons.interlay.dao.entities;

import jakarta.persistence.*;
import net.ihe.gazelle.user.management.api.domain.group.Group;
import net.ihe.gazelle.user.management.api.domain.group.GroupType;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entity representing a group in the database.
 */
@Entity
@Table(name = "group", schema = "gum")
public class GroupEntity {

    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "reference")
    private String reference;

    @ElementCollection(fetch = FetchType.LAZY)
    @Column(name="in_group_id")
    @JoinTable(name = "group_ingroup", schema = "gum",
            joinColumns = @JoinColumn(name = "group_id"),
            foreignKey = @ForeignKey(name = "fk_group_ingroup"))
    private Set<String> inGroupIds;

    @Column(name="type")
    @Enumerated(EnumType.STRING)
    private GroupType type;

    public GroupEntity() {
        inGroupIds = new HashSet<>();
    }

    public GroupEntity(Group group) {
        this.setId(group.getId());
        this.setType(group.getType());
        this.setReference(group.getReference());
        this.setName(group.getName());
        this.setInGroupIds(group.getInGroupIds());
    }

    public GroupEntity(String groupId) {
        this(new Group(groupId));
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Set<String> getInGroupIds() {
        return new HashSet<>(inGroupIds);
    }

    public void setInGroupIds(Set<String> inGroupIds) {
        this.inGroupIds = inGroupIds == null
                ? new HashSet<>()
                : new HashSet<>(inGroupIds);
    }

    public GroupType getType() {
        return type;
    }

    public void setType(GroupType type) {
        this.type = type;
    }

    public Group asGroup() {
        Group group =  new Group(getId(), getType(), getReference());
        group.setName(this.getName());
        group.setInGroupIds(getInGroupIds());
        return group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupEntity that = (GroupEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(reference, that.reference) && Objects.equals(getInGroupIds(), that.getInGroupIds()) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reference, getInGroupIds(), type);
    }
}
