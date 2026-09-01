package net.ihe.gazelle.user.management.commons.interlay.dao.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;

/**
 * Entity representing user credentials, linked to a UserEntity.
 */
@Entity
@Table(name = "credentials", schema = "gum")
public class CredentialsEntity {


    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", foreignKey = @ForeignKey(name = "fk_credentials_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    @Column(name="credentials")
    private String credentials;
    @Column(name="reset_password")
    private boolean resetPassword = false;

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public boolean getResetPassword() {
        return resetPassword;
    }

    public void setResetPassword(boolean resetPassword) {
        this.resetPassword = resetPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CredentialsEntity that = (CredentialsEntity) o;
        return resetPassword == that.resetPassword && Objects.equals(user, that.user) && Objects.equals(credentials, that.credentials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, credentials, resetPassword);
    }
}
