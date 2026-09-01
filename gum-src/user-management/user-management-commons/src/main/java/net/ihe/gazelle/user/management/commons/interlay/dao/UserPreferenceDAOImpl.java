package net.ihe.gazelle.user.management.commons.interlay.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.api.interlay.user.UserPreferenceResource;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;
import net.ihe.gazelle.user.management.commons.application.user.preference.UserPreferenceDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.UserEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.UserPreferenceEntity;
import org.hibernate.query.sqm.PathElementException;

import java.util.List;
import java.util.NoSuchElementException;

import static net.ihe.gazelle.user.management.commons.interlay.dao.user.UserEditDAOImpl.USER_ID;


/**
 * DAO implementation for managing user preferences, providing methods to retrieve, update, and create user preferences in the database.
 */
public class UserPreferenceDAOImpl implements UserPreferenceDAO {

    private final EntityManager entityManager;

    public UserPreferenceDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public UserPreferenceResource getUserPreferenceByUserId(String userId) {
        UserPreferenceEntity userPreferenceEntity = entityManager.find(UserPreferenceEntity.class, userId);
        if (userPreferenceEntity == null)
            throw new NoSuchElementException("No user preference found for user id " + userId);
        return userPreferenceEntity.asUserPreferenceResource();
    }

    @Override
    public UserPreferenceResource updateUserPreferenceByUserId(String userId, UserPreferenceResource userPreferenceResource) {
        UserPreferenceEntity userPreferenceEntity = entityManager.find(UserPreferenceEntity.class, userId);
        if (userPreferenceEntity == null) {
            userPreferenceEntity = new UserPreferenceEntity();
            userPreferenceEntity.setUser(entityManager.find(UserEntity.class, userId));
        }
        if (userPreferenceResource.getLanguagesSpoken() != null)
            userPreferenceEntity.setLanguagesSpoken(String.join(",", userPreferenceResource.getLanguagesSpoken()));
        if (userPreferenceResource.getTableLabel() != null)
            userPreferenceEntity.setTableLabel(userPreferenceResource.getTableLabel());

        userPreferenceEntity.setNotifiedByEmail(userPreferenceResource.isNotifiedByEmail());

        return entityManager.merge(userPreferenceEntity).asUserPreferenceResource();
    }

    @Override
    public UserPreferenceResource createUserPreference(UserPreferenceResource userPreferenceResource) {
        try {
            UserEntity userEntity = entityManager.find(UserEntity.class, userPreferenceResource.getUserId());
            UserPreferenceEntity userPreferenceEntity = new UserPreferenceEntity(userPreferenceResource);
            userPreferenceEntity.setUser(userEntity);
            entityManager.persist(userPreferenceEntity);
            return userPreferenceResource;
        } catch (Exception e) {
            String message = String.format("Could not create user preference for user id %s", userPreferenceResource.getUserId());
            throw new GazelleDAOException(message, e);
        }
    }

    @Override
    public Object getUserPreferenceByPreferenceName(String userId, String preferenceName) {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Object> cq = cb.createQuery(Object.class);
            Root<UserPreferenceEntity> root = cq.from(UserPreferenceEntity.class);
            cq.select(root.get(preferenceName))
                    .where(cb.equal(root.get("user").get("id"), userId));
            return entityManager.createQuery(cq).getSingleResult();

        } catch (NoResultException e) {
            String message = String.format("%s column does not exist for user id %s", preferenceName, userId);
            throw new NoSuchElementException(message, e);
        } catch (PathElementException e) {
            String message = String.format("Preference %s not found for user id %s", preferenceName, userId);
            throw new NoSuchElementException(message, e);
        } catch (Exception e) {
            String message = String.format("Error while getting %s for user id %s", preferenceName, userId);
            throw new GazelleDAOException(message, e);
        }
    }

    @Override
    public byte[] getProfilePictureForUserIdBytes(String userId) {
        return getImageAsByte(userId, "pref.profilePicture", "profile picture");
    }


    @Override
    public byte[] getProfileThumbnailForUserIdBytes(String userId) {
        return getImageAsByte(userId, "pref.profileThumbnail", "profile thumbnail");
    }

    @Override
    public List<String> getLanguagesSpokenForUserId(String userId) {
        try {
            String languages = (String) entityManager.createQuery("SELECT pref.languagesSpoken FROM UserPreferenceEntity pref WHERE pref.user.id = :userId")
                    .setParameter(USER_ID, userId)
                    .getSingleResult();
            return List.of(languages.split(","));

        } catch (NoResultException e) {
            String message = String.format("Could not find spoken languages for user id %s", userId);
            throw new NoSuchElementException(message, e);
        } catch (Exception e) {
            String message = String.format("Could not get spoken languages for user id %s", userId);
            throw new GazelleDAOException(message, e);
        }
    }

    @Override
    public byte[] updateUserProfilePicture(String userId, byte[] profilePicture) {
        UserPreferenceEntity userPreferenceEntity = entityManager.find(UserPreferenceEntity.class, userId);
        userPreferenceEntity.setProfilePicture(profilePicture);
        return entityManager.merge(userPreferenceEntity).getProfilePicture();
    }

    @Override
    public byte[] updateUserProfileThumbnail(String userId, byte[] profileThumbnail) {
        UserPreferenceEntity userPreferenceEntity = entityManager.find(UserPreferenceEntity.class, userId);
        userPreferenceEntity.setProfileThumbnail(profileThumbnail);
        return entityManager.merge(userPreferenceEntity).getProfileThumbnail();
    }

    @Override
    public User getUserFromUserId(String userId) {
        UserEntity user = entityManager.find(UserEntity.class, userId);
        return user != null ? user.asUser() : null;
    }

    private byte[] getImageAsByte(String userId, String sqlAttributeName, String displayName) {
        try {
            return (byte[]) entityManager.createQuery("SELECT " + sqlAttributeName + " FROM UserPreferenceEntity pref WHERE pref.user.id = :userId")
                    .setParameter(USER_ID, userId)
                    .getSingleResult();
        } catch (NoResultException e) {
            String message = String.format("No %s found for user id %s", displayName, userId);
            throw new NoSuchElementException(message, e);
        } catch (Exception e) {
            String message = String.format("Could not get %s for user id %s",displayName, userId);
            throw new GazelleDAOException(message, e);
        }
    }
}
