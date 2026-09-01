package net.ihe.gazelle.user.management.commons.interlay.dao.entities;

import jakarta.persistence.*;
import net.ihe.gazelle.user.management.api.domain.group.GroupType;
import net.ihe.gazelle.user.management.api.domain.user.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entity representing a user in the database.
 */
@Entity
@Table(name = "user", schema = "gum")
@Inheritance(strategy = InheritanceType.JOINED)
public class UserEntity {

    public UserEntity() { /* for JPA */ }

    public UserEntity(User user) {
        this.setId(user.getId());
        this.setFirstName(user.getFirstName());
        this.setLastName(user.getLastName());
        this.setEmail(user.getEmail());
        if (user.isActivated() == null)
            this.setActivated(false);
        else
            this.setActivated(user.isActivated());
        this.setLastLoginTimestamp(new Timestamp(user.getLastLoginTimestamp()));
        this.setLastUpdateTimestamp(new Timestamp(user.getLastUpdateTimestamp()));
        this.setRegistrationTimestamp(new Timestamp(user.getLastUpdateTimestamp()));
        this.setActivationCode(user.getActivationCode());
        this.setOrganizationId(user.getOrganizationId());
        this.setLoginCounter(user.getLoginCounter());

        if (user.getGroupIds() != null)
            this.setGroupEntities(user.getGroupIds().stream()
                    .map(GroupEntity::new)
                    .collect(Collectors.toSet()));
        else
            this.setGroupEntities(Set.of());
    }

    public UserEntity(String id, String lastName, String firstName, String organizationId) {
        this.setId(id);
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setOrganizationId(organizationId);
    }


    @Id
    private String id;
    @Column(name="firstname")
    private String firstName;
    @Column(name="lastname")
    private String lastName;
    @Column(name="email", unique = true, nullable = false)
    private String email;
    @Column(name="activated", nullable = false)
    private Boolean activated;
    @Column(name="last_login_timestamp")
    private Timestamp lastLoginTimestamp;
    @Column(name="activation_code")
    private String activationCode;
    @Column(name="organization_id")
    private String organizationId;

    @UpdateTimestamp
    @Column(name="last_update_timestamp")
    private Timestamp lastUpdateTimestamp;

    @CreationTimestamp
    @Column(name="registration_timestamp")
    private Timestamp registrationTimestamp;
    @Column(name="login_counter")
    private Integer loginCounter = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_group", schema = "gum",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"),
            foreignKey = @ForeignKey(name = "fk_user_group"),
            inverseForeignKey = @ForeignKey(name = "fk_group_user")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<GroupEntity> groupEntities;

    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return this.firstName;
    }
    public void setFirstName(String firstname) {
        this.firstName = firstname;
    }

    public String getLastName() {
        return this.lastName;
    }
    public void setLastName(String lastname) {
        this.lastName = lastname;
    }

    public String getEmail() {
        return this.email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean isActivated() {
        return this.activated;
    }
    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public Timestamp getLastLoginTimestamp() {
        return this.lastLoginTimestamp;
    }
    public void setLastLoginTimestamp(Timestamp lastLoginTimestamp) {
        this.lastLoginTimestamp = lastLoginTimestamp;
    }

    public Timestamp getLastUpdateTimestamp() {
        return this.lastUpdateTimestamp;
    }
    public void setLastUpdateTimestamp(Timestamp lastUpdateTimestamp) {this.lastUpdateTimestamp = lastUpdateTimestamp;}

    public Timestamp getRegistrationTimestamp() { return this.registrationTimestamp; }
    public void setRegistrationTimestamp(Timestamp registrationTimestamp) { this.registrationTimestamp = registrationTimestamp; }

    public String getActivationCode() { return this.activationCode; }
    public void setActivationCode(String activationCode) { this.activationCode = activationCode; }

    public void addGroupEntity(GroupEntity newGroupEntity) {
        if (newGroupEntity.getType().equals(GroupType.ORGANIZATION_ADMIN))
            this.groupEntities = this.groupEntities.stream()
                    .map(group -> group.getType().equals(GroupType.ORGANIZATION_ADMIN) ? newGroupEntity : group)
                    .collect(Collectors.toSet());
        else if (newGroupEntity.getType().equals(GroupType.ORGANIZATION))
            this.groupEntities = this.groupEntities.stream()
                    .map(group -> group.getType().equals(GroupType.ORGANIZATION) ? newGroupEntity : group)
                    .collect(Collectors.toSet());

        if (!groupEntities.contains(newGroupEntity))
            this.groupEntities.add(newGroupEntity);
    }
    public void removeGroupEntity(GroupEntity groupEntity) { this.groupEntities.remove(groupEntity); }
    public boolean isOrgaAdministrator() { return  groupEntities.stream().anyMatch(groupEntity -> groupEntity.getType().equals(GroupType.ORGANIZATION_ADMIN)); }
    public Set<GroupEntity> getGroupEntities() { return new HashSet<>(groupEntities); }
    public void setGroupEntities(Set<GroupEntity> groupEntities) { this.groupEntities = new HashSet<>(groupEntities); }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public void setLoginCounter(Integer loginCounter) { this.loginCounter = loginCounter; }
    public Integer getLoginCounter() { return this.loginCounter; }

    public User asUser() {
        User user = new User(this.getId());
        user.setFirstName(this.getFirstName());
        user.setLastName(this.getLastName());
        user.setEmail(this.getEmail());
        user.setActivated(this.isActivated());
        user.setActivationCode(this.getActivationCode());
        user.setOrganizationId(this.getOrganizationId());
        user.setLoginCounter(this.getLoginCounter());

        if (this.getLastLoginTimestamp() != null)
            user.setLastLoginTimestamp(this.getLastLoginTimestamp().getTime());
        if (this.getLastUpdateTimestamp() != null)
            user.setLastUpdateTimestamp(this.getLastUpdateTimestamp().getTime());
        if (this.getRegistrationTimestamp() != null)
            user.setRegistrationTimestamp(this.getRegistrationTimestamp().getTime());
        if (this.getGroupEntities() != null) {
            user.setGroupIds(this.getGroupEntities().stream().map(GroupEntity::getId).collect(Collectors.toSet()));
            addGroupIdsRecursively(user);
        }
        return user;
    }

    public User asUserSummary() {
        User user = new User(this.getId());
        user.setFirstName(this.getFirstName());
        user.setLastName(this.getLastName());
        user.setOrganizationId(this.getOrganizationId());
        return user;
    }

    private void addGroupIdsRecursively(User user) {
        //TODO there is a limitation here with one inheritance level (because we only have inGrouIds and not inGroup entities)
        for (GroupEntity groupEntity : groupEntities) {
            groupEntity.getInGroupIds().forEach(user::addGroupId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return activated == that.activated && Objects.equals(id, that.id)
                && Objects.equals(firstName, that.firstName) && Objects.equals(email, that.email)
                && Objects.equals(lastName, that.lastName) && Objects.equals(lastLoginTimestamp, that.lastLoginTimestamp)
                && Objects.equals(activationCode, that.activationCode)
                && Objects.equals(lastUpdateTimestamp, that.lastUpdateTimestamp)
                && Objects.equals(registrationTimestamp, that.registrationTimestamp)
                && Objects.equals(loginCounter, that.loginCounter)
                && Objects.equals(groupEntities, that.groupEntities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, email, lastName, activated, lastLoginTimestamp, activationCode, lastUpdateTimestamp, registrationTimestamp, loginCounter, groupEntities);
    }
}
