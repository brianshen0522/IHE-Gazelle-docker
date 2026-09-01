package net.ihe.gazelle.user.management.commons.interlay.dao.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;

/**
 * Entity representing user consent for data processing.
 */
@Entity
@Table(name = "user_consent", schema = "gum")
@SequenceGenerator(name = "userConsentSeq", sequenceName = "gum.user_consent_seq", allocationSize=1)
public class ConsentEntity {

    public ConsentEntity() {
        // for JPA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userConsentSeq")
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", foreignKey = @ForeignKey(name = "fk_user_consent"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    @Column(name="consent")
    private boolean consent;

    public UserEntity getUser() { return user; }

    public void setUser(UserEntity user) { this.user = user; }

    public boolean consent() { return consent; }

    public void setConsent(boolean consent) { this.consent = consent; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsentEntity that = (ConsentEntity) o;
        return id == that.id && consent == that.consent && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, consent);
    }
}
