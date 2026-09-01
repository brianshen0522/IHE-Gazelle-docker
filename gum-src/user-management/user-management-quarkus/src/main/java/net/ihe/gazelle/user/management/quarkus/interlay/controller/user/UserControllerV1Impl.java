package net.ihe.gazelle.user.management.quarkus.interlay.controller.user;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.user.lookup.SortOrder;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserQueryParams;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserSearchResult;
import net.ihe.gazelle.user.management.api.interlay.user.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static net.ihe.gazelle.user.management.quarkus.interlay.ControllerSyntaxHelper.executeActionAndCatchPotentialException;
import static org.apache.http.HttpHeaders.CONTENT_RANGE;

@RequestScoped
public class UserControllerV1Impl implements UserControllerV1 {
    private static final String COULD_NOT_SEARCH_FOR_USER = "Could not search for user ";

    private final UserController userController;
    private final UserLookupService userLookupService;
    private final Logger logger = LoggerFactory.getLogger(UserControllerV1Impl.class.getName());
    private final GazelleIdentity identity;

    @Inject
    public UserControllerV1Impl(UserController userController, UserLookupService userLookupService, GazelleIdentity identity) {
        this.userController = userController;
        this.userLookupService = userLookupService;
        this.identity = identity;
    }

    @Override
    public Response searchAndFilter(String search, String firstName, String lastName,
                                    String email, String organizationId, String group, Boolean activated,
                                    Boolean delegated, String externalId, String idpId,
                                    Integer offset, Integer limit, String sortBy, SortOrder sortOrder) {
        String errorMessage = COULD_NOT_SEARCH_FOR_USER + search;
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            UserQueryParams query = new UserQueryParams(search, firstName, lastName, email, group, organizationId, activated, delegated, externalId, idpId);
            UserSearchResult userSearchResult = userLookupService.searchAndFilterUsersWithCount(query, offset, limit, sortBy, sortOrder, identity);
            UserSearchResponse userSearchResponse =
                    new UserSearchResponse(userSearchResult.users().stream().map(UserResource::new).toList(),
                            userSearchResult.offset(), userSearchResult.limit(), userSearchResult.count());

            return Response.ok().status(Response.Status.OK)
                    .header(CONTENT_RANGE, "users " + userSearchResponse.offset() + "-" + userSearchResponse.limit() + "/" + userSearchResponse.count())
                    .entity(userSearchResponse).build();
        });
    }

    @Override
    public Response getValueCount(String propertyName, String search, String firstName, String lastName, String organizationId, String group, Boolean activated, Boolean delegated) {
        UserQueryParams queryParams = new UserQueryParams(search, firstName, lastName, null, group, organizationId, activated, delegated, null, null);
        Map<String, Long> valueMap = userLookupService.getValueCount(propertyName, queryParams, identity);
        return Response.ok(valueMap).build();
    }

    @Override
    public Response registerUserV1(UserRegisterRequest userRegisterRequest) {
        return userController.registerUser(userRegisterRequest);
    }

    @Override
    public Response createUserV1(UserCreationRequest userCreationRequest) {
        return userController.createUser(userCreationRequest);
    }

    @Override
    public Response patchV1(String userId, UserEditionResource userResource) {
        return userController.patch(userId, userResource);
    }

    @Override
    public Response searchAndFilterSummaryV1(String search, String firstName, String lastName, String email, String organizationId, String group, Boolean activated, Boolean delegated, String externalId, String idpId, Integer offset, Integer limit, String sortBy, SortOrder sortOrder) {
        return userController.searchAndFilterSummary(search,firstName, lastName, email, organizationId, group, activated, delegated,
                externalId, idpId, offset, limit, sortBy, sortOrder);
    }

    @Override
    public Response getUserByIdV1(String userId) {
        return userController.getUserById(userId);
    }

    @Override
    public Response getUserSummaryByIdV1(String userId) {
        return userController.getUserSummaryById(userId);
    }

    @Override
    public Response activateFromActivationCodeV1(String activationCode) {
        return userController.activateFromActivationCode(activationCode);
    }

    @Override
    public Response activateV1(ActivationResource activationResource) {
        return userController.activate(activationResource);
    }

    @Override
    public Response deactivateV1(ActivationResource activationResource) {
        return userController.deactivate(activationResource);
    }

    @Override
    public Response deleteUserV1(String userId) {
        return userController.deleteUser(userId);
    }

    @Override
    public Response getUserPreferencesV1(String userId) {
        return userController.getUserPreferences(userId);
    }

    @Override
    public Response getSingleUserPreferenceV1(String userId, String preferenceId) {
        return userController.getSingleUserPreference(userId, preferenceId);
    }

    @Override
    public Response updateUserPreferenceV1(String userId, UserPreferenceResource userPreferenceResource) {
        return userController.updateUserPreference(userId, userPreferenceResource);
    }

    @Override
    public Response getProfilePictureV1(String userId, String format) {
        return userController.getProfilePicture(userId, format);
    }

    @Override
    public Response updateProfilePictureV1(String userId, byte[] profilePicture) {
        return userController.updateProfilePicture(userId, profilePicture);
    }

    @Override
    public Response deleteUserProfilePictureV1(String userId) {
        return userController.deleteUserProfilePicture(userId);
    }

    @Override
    public Response joinGroupV1(String userId, GroupIdResource groupIdResource) {
        return userController.joinGroup(userId, groupIdResource);
    }

    @Override
    public Response leaveGroupV1(String userId, GroupIdResource groupIdResource) {
        return userController.leaveGroup(userId, groupIdResource);
    }
}
