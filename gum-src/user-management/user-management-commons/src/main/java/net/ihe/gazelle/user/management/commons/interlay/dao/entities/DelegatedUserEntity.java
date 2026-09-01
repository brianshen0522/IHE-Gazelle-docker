package net.ihe.gazelle.user.management.commons.interlay.dao.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import net.ihe.gazelle.user.management.api.domain.user.DelegatedUser;
import net.ihe.gazelle.user.management.api.domain.user.User;

import java.util.Objects;

/**
 * Entity representing a delegated user in the database.
 */
@Entity
@Table(name = "delegated_user", schema = "gum")
@PrimaryKeyJoinColumn(name = "user_id")
public class DelegatedUserEntity extends UserEntity {

    @Column(name="external_id")
    private String externalId;

    @Column(name="idp_id")
    private String idpId;

    public DelegatedUserEntity() {
        // for JPA
    }

    public DelegatedUserEntity(User user, String externalId, String idpId) {
        super(user);
        this.setIdpId(idpId);
        this.setExternalId(externalId);
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getIdpId() {
        return idpId;
    }

    public void setIdpId(String idpId) {
        this.idpId = idpId;
    }

    @Override
    public DelegatedUser asUser() {
        User user = super.asUser();
        return new DelegatedUser(user, externalId, idpId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DelegatedUserEntity that = (DelegatedUserEntity) o;
        return Objects.equals(externalId, that.externalId) && Objects.equals(idpId, that.idpId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), externalId, idpId);
    }
}
