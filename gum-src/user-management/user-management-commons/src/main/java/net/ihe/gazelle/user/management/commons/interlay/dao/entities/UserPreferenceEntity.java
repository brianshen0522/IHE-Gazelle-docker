package net.ihe.gazelle.user.management.commons.interlay.dao.entities;

import jakarta.persistence.*;
import net.ihe.gazelle.user.management.api.interlay.user.UserPreferenceResource;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing user preferences in the database.
 */
@Entity
@Table(name = "user_preference", schema = "gum")
public class UserPreferenceEntity {

    @Id
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_preference_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    @Column(name = "profile_picture")
    private byte[] profilePicture;

    @Column(name = "profile_thumbnail")
    private byte[] profileThumbnail;

    @Column(name = "table_label")
    private String tableLabel;

    @Column(name = "notified_by_email")
    private boolean notifiedByEmail;

    @Column(name = "languages_spoken")
    private String languagesSpoken;


    public UserPreferenceEntity() {
    }

    public UserPreferenceEntity(UserPreferenceResource userPreferenceResource) {
        tableLabel = userPreferenceResource.getTableLabel();
        notifiedByEmail = userPreferenceResource.isNotifiedByEmail();
        languagesSpoken = String.join(",", userPreferenceResource.getLanguagesSpoken());
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public byte[] getProfileThumbnail() {
        return profileThumbnail;
    }

    public void setProfileThumbnail(byte[] profileThumbnail) {
        this.profileThumbnail = profileThumbnail;
    }


    public String getTableLabel() {
        return tableLabel;
    }

    public void setTableLabel(String tableLabel) {
        this.tableLabel = tableLabel;
    }

    public boolean isNotifiedByEmail() {
        return notifiedByEmail;
    }

    public void setNotifiedByEmail(boolean notificationByEmail) {
        this.notifiedByEmail = notificationByEmail;
    }


    public String getLanguagesSpoken() {
        return languagesSpoken;
    }

    public void setLanguagesSpoken(String languagesSpoken) {
        this.languagesSpoken = languagesSpoken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPreferenceEntity that = (UserPreferenceEntity) o;
        return notifiedByEmail == that.notifiedByEmail
                && Objects.equals(getUser(), that.user)
                && Objects.deepEquals(profilePicture, that.profilePicture)
                && Objects.deepEquals(profileThumbnail, that.profileThumbnail)
                && Objects.equals(tableLabel, that.tableLabel)
                && Objects.equals(languagesSpoken, that.languagesSpoken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser(),
                Arrays.hashCode(profilePicture),
                Arrays.hashCode(profileThumbnail),
                tableLabel,
                notifiedByEmail,
                languagesSpoken);
    }

    public UserPreferenceResource asUserPreferenceResource() {
        List<String> languages = languagesSpoken != null && !languagesSpoken.isEmpty() ?
                Arrays.stream(languagesSpoken.split(",")).toList() : List.of() ;
        return new UserPreferenceResource(getUser().getId(),
                tableLabel,
                notifiedByEmail,
                languages);
    }
}
