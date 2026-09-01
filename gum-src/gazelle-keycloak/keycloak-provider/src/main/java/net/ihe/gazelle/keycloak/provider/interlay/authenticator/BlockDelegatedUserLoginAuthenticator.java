package net.ihe.gazelle.keycloak.provider.interlay.authenticator;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.keycloak.core.interlay.identity.KeycloakIdentity;
import net.ihe.gazelle.keycloak.provider.interlay.GazelleUserModelAdapter;
import net.ihe.gazelle.user.management.api.application.organization.DelegatedOrganizationService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserQueryParams;
import net.ihe.gazelle.user.management.api.domain.organization.DelegatedOrganization;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.api.domain.user.DelegatedUser;
import net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup;
import net.ihe.gazelle.user.management.api.domain.user.User;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;

import static org.keycloak.services.validation.Validation.FIELD_USERNAME;

/**
 * Authenticator that blocks login attempts for users belonging to delegated organizations.
 *
 * This authenticator extends the standard UsernamePasswordForm to add validation logic
 * that prevents local login for users who belong to organizations that are managed by
 * external identity providers. It redirects such users to use their delegated authentication
 * method instead of local Keycloak authentication.
 *
 */
public class BlockDelegatedUserLoginAuthenticator extends UsernamePasswordForm implements Authenticator {

    /**
     * Message key for organization delegation notification.
     */
    private static final String ORGANIZATION_DELEGATED = "organizationDelegatedMessage";

    /**
     * Service for looking up user information.
     */
    private final UserLookupService userLookupService;

    /**
     * Service for handling delegated organization operations.
     */
    private final DelegatedOrganizationService delegatedOrganizationService;

    /**
     * Constructs a new BlockDelegatedUserLoginAuthenticator.
     *
     * @param delegatedOrganizationService service for delegated organization operations
     * @param userLookupService service for user lookup operations
     */
    public BlockDelegatedUserLoginAuthenticator(DelegatedOrganizationService delegatedOrganizationService, UserLookupService userLookupService) {
        this.delegatedOrganizationService = delegatedOrganizationService;
        this.userLookupService = userLookupService;
    }

    @Override
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        UserModel user = getUser(context, inputData);

        if (user == null) {
            return false;
        }
        //The User can log in if
        //He is not a delegated user,
        if (isDelegatedUser(context, user))
            return false;
        //He doesn't belong, or he is vendor_admin of a delegated organization and there is no delegated vendor_admin in this organization
        if (isDelegatedOrganization(context, user))
            return false;
        //His credentials are valid
        return super.validateForm(context, inputData);
    }

    // This code is a copy from org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator
    // It is used to be able to get the UserModel from inputData because original code is private and cannot be used.
    private UserModel getUser(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        if (isUserAlreadySetBeforeUsernamePasswordAuth(context)) {
            // Get user from the authentication context in case he was already set before this authenticator
            UserModel user = context.getUser();
            testInvalidUser(context, user);
            return user;
        } else {
            // Normal login. In this case this authenticator is supposed to establish identity of the user from the provided username
            context.clearUser();
            return getUserFromForm(context, inputData);
        }
    }

    // This code is a copy from org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator
    // It is used to be able to get the UserModel from inputData because original code is private and cannot be used.
    private UserModel getUserFromForm(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        String username = inputData.getFirst(AuthenticationManager.FORM_USERNAME);
        if (username == null || username.isEmpty()) {
            context.getEvent().error(Errors.USER_NOT_FOUND);
            Response challengeResponse = challenge(context, getDefaultChallengeMessage(context), FIELD_USERNAME);
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return null;
        }

        // remove leading and trailing whitespace
        username = username.trim();

        context.getEvent().detail(Details.USERNAME, username);
        context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);

        UserModel user = null;
        try {
            user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), username);
        } catch (ModelDuplicateException mde) {
            ServicesLogger.LOGGER.modelDuplicateException(mde);

            // Could happen during federation import
            if (mde.getDuplicateFieldName() != null && mde.getDuplicateFieldName().equals(UserModel.EMAIL)) {
                setDuplicateUserChallenge(context, Errors.EMAIL_IN_USE, Messages.EMAIL_EXISTS, AuthenticationFlowError.INVALID_USER);
            } else {
                setDuplicateUserChallenge(context, Errors.USERNAME_IN_USE, Messages.USERNAME_EXISTS, AuthenticationFlowError.INVALID_USER);
            }
            return user;
        }

        testInvalidUser(context, user);
        return user;
    }

    /**
     * Checks if the user belongs to a delegated organization.
     *
     * @param context the authentication flow context
     * @param userModel the user model to check
     * @return true if the user belongs to a delegated organization, false otherwise
     */
    public boolean isDelegatedUser(AuthenticationFlowContext context, UserModel userModel) {
        if (userModel instanceof GazelleUserModelAdapter gumUser && gumUser.getUser() instanceof DelegatedUser) {
            generateErrorMessage(context, userModel, Messages.INVALID_USER);
            return true;
        }
        return false;
    }

    /**
     * Checks if the user's organization is delegated to an external identity provider.
     *
     * @param context the authentication flow context
     * @param userModel the user model to check
     * @return true if the user's organization is delegated, false otherwise
     */
    public boolean isDelegatedOrganization(AuthenticationFlowContext context, UserModel userModel) {
        if (userModel instanceof GazelleUserModelAdapter gumUser) {
            User user = gumUser.getUser();
            try {
                Organization organization = delegatedOrganizationService.getOrganizationById(user.getOrganizationId());
                if (organization instanceof DelegatedOrganization && !isUserInDelegatedOrganizationAuthorized(user)) {
                    generateErrorMessage(context, userModel, ORGANIZATION_DELEGATED);
                    return true;
                }
            // We don't want to block authentication because of TM here, so we catch all the possible exceptions
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private boolean isUserInDelegatedOrganizationAuthorized(User user) {
        return user.getGroupIds().stream()
                .anyMatch(groupId -> groupId.startsWith(GazelleDefaultGroup.PREFIX_ORGANIZATION_ADMIN.getName()))
                    && isThereDelegatedVendorAdminsInUserOrganization(user);
    }

    private boolean isThereDelegatedVendorAdminsInUserOrganization(User user) {
        UserQueryParams userQueryParams = new UserQueryParams(null, null, null, null,
                GazelleDefaultGroup.PREFIX_ORGANIZATION_ADMIN.getName(),
                user.getOrganizationId(), true, true, null, null
        );
        return userLookupService.searchAndFilterUsersWithCount(
                userQueryParams, null, null, null, null, new KeycloakIdentity()
        ).count() == 0;
    }

    private void generateErrorMessage(AuthenticationFlowContext context, UserModel userModel, String message) {
        context.getEvent().user(userModel);
        context.getEvent().error(Errors.IDENTITY_PROVIDER_ERROR);
        Response challengeResponse = challenge(context, message);
        context.forceChallenge(challengeResponse);
    }
}
