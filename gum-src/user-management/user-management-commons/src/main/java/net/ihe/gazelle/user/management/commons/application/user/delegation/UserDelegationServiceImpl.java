package net.ihe.gazelle.user.management.commons.application.user.delegation;

import jakarta.transaction.Transactional;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.api.domain.user.DelegatedUser;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;

import java.util.NoSuchElementException;
import java.util.UUID;

public class UserDelegationServiceImpl implements UserDelegationService {

    private final UserDelegationDAO userDelegationDAO;
    private final UserLookupDAO userLookupDAO;
    private final UserEditDAO userEditDAO;

    public UserDelegationServiceImpl(UserDelegationDAO userDelegationDAO, UserLookupDAO userLookupDAO, UserEditDAO userEditDAO) {
        this.userDelegationDAO = userDelegationDAO;
        this.userLookupDAO = userLookupDAO;
        this.userEditDAO = userEditDAO;
    }

    @Override
    @Transactional
    public DelegatedUser createDelegatedUser(User user, String externalId, String idpId) {
        assertParametersValid(externalId, idpId);
        assertNewDelegatedUserIsValid(user);

        // Create delegated user
        String userId = UUID.randomUUID().toString();
        user.setId(userId);
        user.setActivated(true);
        // Register the user
        return userDelegationDAO.createDelegatedUser(user, externalId, idpId);
    }

    @Override
    @Transactional
    public DelegatedUser transformUserIntoDelegatedUser(String userEmail, String externalId, String idpId) {
        assertParametersValid(externalId, idpId);
        // Transform the user into a delegated user
        return userDelegationDAO.transformUserIntoDelegatedUser(userEmail, externalId, idpId);
    }

    @Override
    public DelegatedUser getDelegatedUser(String externalId, String idpId) {
        if (externalId == null) throw new IllegalArgumentException("externalId is null");
        if (idpId == null) throw new IllegalArgumentException("idpId is null");
        return userDelegationDAO.getDelegatedUser(externalId, idpId);
    }

    @Override
    public DelegatedUser getDelegatedUserById(String userId) {
        if (userId == null) throw new IllegalArgumentException(ErrorMessage.USER_ID_IS_NULL.getMessage());
        return userDelegationDAO.getDelegatedUserById(userId);
    }

    @Override
    public boolean isUserDelegatedFromId(String userId) {
        if (userId == null) throw new IllegalArgumentException(ErrorMessage.USER_ID_IS_NULL.getMessage());
        try {
            DelegatedUser user = userDelegationDAO.getDelegatedUserById(userId);
            return userId.equals(user.getId());
        } catch (NoSuchElementException _) {
            return false;
        }
    }

    @Override
    public boolean isUserDelegatedFromEmail(String email) {
        if (email == null) throw new IllegalArgumentException(ErrorMessage.USER_EMAIL_IS_NULL.getMessage());
        try {
            User user = userLookupDAO.getUserByEmail(email);
            DelegatedUser delegatedUser = userDelegationDAO.getDelegatedUserById(user.getId());
            return email.equals(delegatedUser.getEmail());
        } catch (NoSuchElementException _) {
            return false;
        }
    }

    @Override
    public void activateDelegatedUser(String userId) {
        userEditDAO.updateActivatedStatusOfUser(userId, true);
    }

    @Override
    public boolean isDelegatedUserExisting(String externalId, String idpId) {
        assertParametersValid(externalId,idpId);
        return userDelegationDAO.isDelegatedUserExisting(externalId,idpId);
    }

    private static void assertParametersValid(String externalId, String idpId) {
        if (externalId == null || idpId == null) {
            throw new IllegalArgumentException("Required "+ (externalId!=null
                    ?"externalId"
                    :"idpId")+" parameter is null");
        }
    }

    /**
     * Check if the provided delegated user is valid
     *
     * @param user the user to check
     */
    private void assertNewDelegatedUserIsValid(User user) {
        if (user == null) throw new IllegalArgumentException("User is null");
        if (user.getFirstName() == null) throw new IllegalArgumentException("Firstname is null");
        if (user.getLastName() == null) throw new IllegalArgumentException("Lastname is null");
        if (user.getEmail() == null) throw new IllegalArgumentException(ErrorMessage.USER_EMAIL_IS_NULL.getMessage());
    }
}