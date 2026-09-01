package net.ihe.gazelle.user.management.commons.interlay.dao.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Entity representing the history of user consents, including the consent decision and timestamp.
 */
@Entity
@Table(name = "user_consent_history", schema = "gum")
public class ConsentHistoryEntity {

    public ConsentHistoryEntity() {
        // for JPA
    }

    @ManyToOne()
    @Id
    @JoinColumn(name = "consent_id", foreignKey = @ForeignKey(name = "fk_user_consent_history"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ConsentEntity consent;

    @Id
    @Column(name="timestamp")
    private Timestamp timestamp;

    @Column(name="decision")
    private String decision;

    public ConsentEntity getConsent() {
        return consent;
    }

    public void setConsent(ConsentEntity consent) {
        this.consent = consent;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsentHistoryEntity that = (ConsentHistoryEntity) o;
        return Objects.equals(consent, that.consent) && Objects.equals(timestamp, that.timestamp) && Objects.equals(decision, that.decision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consent, timestamp, decision);
    }
}
