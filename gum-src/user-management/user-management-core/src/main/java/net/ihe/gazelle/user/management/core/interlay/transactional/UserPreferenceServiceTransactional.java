package net.ihe.gazelle.user.management.core.interlay.transactional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.application.user.preference.UserPreferenceService;
import net.ihe.gazelle.user.management.api.domain.user.UserPreference;
import net.ihe.gazelle.user.management.api.interlay.user.UserPreferenceResource;
import net.ihe.gazelle.user.management.commons.application.user.preference.ImageTransformationService;
import net.ihe.gazelle.user.management.commons.application.user.preference.UserPreferenceDAO;
import net.ihe.gazelle.user.management.commons.application.user.preference.UserPreferenceServiceImpl;
import net.ihe.gazelle.user.management.commons.interlay.utils.ScalrImageService;

/**
 * Transactional implementation of the UserPreferenceService interface that delegates the calls to the UserPreferenceServiceImpl.
 */
@RequestScoped
public class UserPreferenceServiceTransactional implements UserPreferenceService {

    private final UserPreferenceService userPreferenceService;
    private static final ImageTransformationService imageTransformationService = new ScalrImageService();

    /**
     * Creates a new instance of UserPreferenceServiceTransactional with the given dependencies.
     * @param userPreferenceDAO the UserPreferenceDAO to use for accessing user preferences in the database
     * @param applicationConfig the ApplicationConfig to use for accessing application configuration settings
     * @param authz the Authz to use for checking user permissions
     */
    @Inject
    public UserPreferenceServiceTransactional(UserPreferenceDAO userPreferenceDAO, ApplicationConfig applicationConfig, Authz authz) {
        this.userPreferenceService = new UserPreferenceServiceImpl(userPreferenceDAO, applicationConfig, authz, imageTransformationService);
    }

    @Override
    @Transactional
    public UserPreference getUserPreferenceByUserId(String userId, GazelleIdentity gazelleIdentity) {
        return userPreferenceService.getUserPreferenceByUserId(userId, gazelleIdentity);
    }

    @Override
    @Transactional
    public UserPreference updateUserPreferenceByUserId(String userId, UserPreferenceResource userPreference, GazelleIdentity gazelleIdentity) {
        return userPreferenceService.updateUserPreferenceByUserId(userId, userPreference, gazelleIdentity);
    }

    @Override
    @Transactional
    public Object getUserPreferenceByPreferenceName(String userId, String preferenceName, GazelleIdentity gazelleIdentity) {
        return userPreferenceService.getUserPreferenceByPreferenceName(userId, preferenceName, gazelleIdentity);
    }

    @Override
    @Transactional
    public byte[] getProfilePicture(String userId, String format, GazelleIdentity gazelleIdentity) {
        return userPreferenceService.getProfilePicture(userId, format, gazelleIdentity);
    }

    @Override
    @Transactional
    public byte[] updateProfilePicture(String userId, byte[] profilePicture, GazelleIdentity gazelleIdentity) {
        return userPreferenceService.updateProfilePicture(userId, profilePicture, gazelleIdentity);
    }

    @Override
    @Transactional
    public byte[] deleteUserProfilePicture(String userId, GazelleIdentity identity) {
        return userPreferenceService.deleteUserProfilePicture(userId, identity);
    }
}
