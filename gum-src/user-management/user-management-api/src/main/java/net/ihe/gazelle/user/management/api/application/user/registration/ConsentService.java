package net.ihe.gazelle.user.management.api.application.user.registration;

/**
 * Service interface for managing user consent in Gazelle User Management.
 * <p>
 * This interface defines methods for handling user consent during registration and account management.
 * </p>
 */
public interface ConsentService {

    /**
     * Accept the consent of a user.
     *
     * @param userId the id of the user
     * @throws IllegalArgumentException if the userId is null
     */
    void acceptUserConsent(String userId);

    /**
     * Check if a user needs to give their consent.
     *
     * @param userId the id of the user
     * @return true if the user needs to give their consent, false otherwise
     */
    boolean needToGiveConsent(String userId);

}
