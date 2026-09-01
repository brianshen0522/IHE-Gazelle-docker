/*
 * Copyright 2026 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.user.management.quarkus.interlay.controller.user;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.group.GroupService;
import net.ihe.gazelle.user.management.api.application.user.edit.UserEditService;
import net.ihe.gazelle.user.management.api.application.user.lookup.SortOrder;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserQueryParams;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserSearchResult;
import net.ihe.gazelle.user.management.api.application.user.preference.UserPreferenceService;
import net.ihe.gazelle.user.management.api.application.user.registration.UserRegistrationService;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.api.domain.user.UserPreference;
import net.ihe.gazelle.user.management.api.interlay.user.*;
import net.ihe.gazelle.user.management.quarkus.interlay.controller.LocaleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static net.ihe.gazelle.user.management.quarkus.interlay.ControllerSyntaxHelper.executeActionAndCatchPotentialException;
import static org.apache.http.HttpHeaders.CONTENT_RANGE;

/**
 * Controller implementation for managing users, handling operations such as creation, registration, activation, searching, and preference management.
 */
@RequestScoped
public class UserControllerImpl implements UserController {

    public static final String COULD_NOT_REGISTER_USER = "Could not register user ";
    private static final String COULD_NOT_CREATE_USER = "Could not create user ";
    private static final String COULD_NOT_ACTIVATE_USER = "Could not activate user ";
    private static final String COULD_NOT_SEARCH_FOR_USER = "Could not search for user ";
    private static final String COULD_NOT_UPDATE_USER = "Could not update user ";
    private static final String COULD_NOT_DEACTIVATE_USER = "Could not deactivate user ";
    private static final String COULD_NOT_UPDATE_USER_PREFERENCES = "Could not update preferences of user ";
    private static final String COULD_NOT_GET_SINGLE_PREFERENCE = "Could not get single preference of user ";
    private static final String COULD_NOT_GET_USER_PREFERENCES = "Could not get user preferences of user ";
    private static final String COULD_NOT_DELETE_PROFILE_PICTURE = "Could not delete profile picture of user ";
    private static final String COULD_NOT_DELETE_USER = "Could not delete the user ";
    private static final String COULD_NOT_GET_PROFILE_PICTURE = "Could not get profile picture of user ";
    private static final String COULD_NOT_UPDATE_PROFILE_PICTURE_OF_USER = "Could not update profile picture of user ";
    private static final String COULD_NOT_GET_USER_BY_ID = "Could not get user by id ";
    private static final String PROVIDED_RESOURCE_IS_NULL = "Provided resource is null";
    private static final String USER_FOUND = "User found: {}";

    private final Logger logger = LoggerFactory.getLogger(UserControllerImpl.class.getName());

    private final UserEditService userEditService;
    private final UserLookupService userLookupService;
    private final UserRegistrationService userRegistrationService;
    private final GazelleIdentity identity;
    private final UserPreferenceService userPreferenceService;
    private final GroupService groupService;
    private final LocaleProvider localeProvider;

    /**
     * Creates a controller instance wired with the required services.
     * @param userEditService service handling user edition operations
     * @param userLookupService service handling user lookup operations
     * @param userRegistrationService service handling user registration operations
     * @param identity current Gazelle identity
     * @param userPreferenceService service handling user preference operations
     * @param groupService service handling group operations
     */
    @Inject
    public UserControllerImpl(UserEditService userEditService, UserLookupService userLookupService, UserRegistrationService userRegistrationService, GazelleIdentity identity, UserPreferenceService userPreferenceService, GroupService groupService) {
        this.userEditService = userEditService;
        this.userLookupService = userLookupService;
        this.userRegistrationService = userRegistrationService;
        this.identity = identity;
        this.userPreferenceService = userPreferenceService;
        this.groupService = groupService;
        this.localeProvider = new LocaleProvider();
    }

    @Context
    HttpHeaders headers;

    @Override
    public Response createUser(UserCreationRequest userCreationRequest) {
        String errorMessage = COULD_NOT_CREATE_USER + (userCreationRequest != null ? userCreationRequest.getEmail() : "");
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            if (userCreationRequest == null) throw new IllegalArgumentException(PROVIDED_RESOURCE_IS_NULL);
            Locale locale = localeProvider.getLocaleFromHeaders(headers);
            User user = new User();
            if (userCreationRequest.getOrganization() == null) {
                user = userRegistrationService.createUser(userCreationRequest.asUser(), identity, locale);
                logger.info("User created and joined orga {}", user.getOrganizationId());
            } else {
                user = userRegistrationService.createUserWithNewOrganization(userCreationRequest.asUser(), userCreationRequest.getOrganization().asOrganization(), identity, locale);
                logger.info("User created and created orga {}", userCreationRequest);
            }
            return Response.ok().status(Response.Status.CREATED).entity(new UserResource(user)).build();
        });
    }


    @Override
    public Response registerUser(UserRegisterRequest userRegisterRequest) {
        String errorMessage = COULD_NOT_REGISTER_USER + (userRegisterRequest != null ? userRegisterRequest.getEmail() : "");
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            if (userRegisterRequest == null) throw new IllegalArgumentException(PROVIDED_RESOURCE_IS_NULL);

            // Register user as member of an organization
            boolean userConsent = userRegisterRequest.getConsent() != null && userRegisterRequest.getConsent();
            Locale locale = localeProvider.getLocaleFromHeaders(headers);

            User user;
            if (userRegisterRequest.getOrganization() == null) {
                user = userRegistrationService.registerUser(userRegisterRequest.asUser(), userConsent,
                        userRegisterRequest.getPassword(), userRegisterRequest.getPasswordConfirmation(), locale);
                logger.info("User registered and joined orga {}", userRegisterRequest);
            } else {
                user = userRegistrationService.registerUserWithNewOrganization(userRegisterRequest.asUser(), userRegisterRequest.getOrganization().asOrganization(),
                        userConsent, userRegisterRequest.getPassword(), userRegisterRequest.getPasswordConfirmation(), locale);
                logger.info("User registered and created orga {}", userRegisterRequest);
            }
            return Response.ok().status(Response.Status.CREATED).entity(new UserResource(user)).build();
        });
    }

    @Override
    public Response patch(String userId, UserEditionResource userEditionResource) {
        String errorMessage = COULD_NOT_UPDATE_USER + userId;
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            if (userEditionResource == null) throw new IllegalArgumentException(PROVIDED_RESOURCE_IS_NULL);
            Locale locale = localeProvider.getLocaleFromHeaders(headers);
            User user = userEditService.updateAttributes(userId, userEditionResource.asUser(), identity, locale);
            logger.debug("User updated: {}", user);
            return Response.ok().status(Response.Status.OK).entity(new UserResource(user)).build();
        });
    }

    @Override
    public Response searchAndFilterSummary(String search, String firstName, String lastName,
                                           String email, String organizationId, String group, Boolean activated,
                                           Boolean delegated, String externalId, String idpId,
                                           Integer offset, Integer limit, String sortBy, SortOrder sortOrder) {
        String errorMessage = COULD_NOT_SEARCH_FOR_USER + search;
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            UserQueryParams query = new UserQueryParams(search, firstName, lastName, email, group, organizationId, activated, delegated, externalId, idpId);
            UserSearchResult userSearchResult = userLookupService.searchAndFilterUsersSummary(query, offset, limit, sortBy, sortOrder, identity);
            UserSummarySearchResponse userSearchResponse =
                    new UserSummarySearchResponse(userSearchResult.users().stream().map(UserSummaryResource::new).toList(),
                            userSearchResult.offset(), userSearchResult.limit());

            return Response.ok().status(Response.Status.OK)
                    .header(CONTENT_RANGE, "users " + userSearchResponse.offset() + "-" + userSearchResponse.limit() + "/" + userSearchResult.count())
                    .entity(userSearchResponse).build();
        });
    }


    @Override
    public Response getUserById(String userId) {
        String errorMessage = COULD_NOT_GET_USER_BY_ID + userId;
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            String decodedId = URLDecoder.decode(userId, StandardCharsets.UTF_8);
            User user = userLookupService.getUserById(decodedId, identity);
            logger.debug(USER_FOUND, user);
            return Response.ok().status(Response.Status.OK).entity(new UserResource(user)).build();
        });
    }

    @Override
    public Response getUserSummaryById(String userId) {
        String errorMessage = COULD_NOT_GET_USER_BY_ID + userId;
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            String decodedId = URLDecoder.decode(userId, StandardCharsets.UTF_8);
            User user = userLookupService.getUserSummaryById(decodedId, identity);
            logger.debug(USER_FOUND, user);
            return Response.ok().status(Response.Status.OK).entity(new UserSummaryResource(user)).build();
        });
    }

    @Override
    public Response activateFromActivationCode(String activationCode) {
        String errorMessage = COULD_NOT_ACTIVATE_USER + activationCode;
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            User user = userRegistrationService.activateUserWithActivationCode(activationCode);
            return Response.ok().status(Response.Status.OK).entity(new UserResource(user)).build();
        });
    }

    @Override
    public Response activate(ActivationResource activationResource) {
        String errorMessage = COULD_NOT_ACTIVATE_USER + (activationResource != null ? activationResource.getUserId() : "");
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            if (activationResource == null) throw new IllegalArgumentException(PROVIDED_RESOURCE_IS_NULL);
            userEditService.activateUser(activationResource.getUserId(), identity);
            return Response.ok().status(Response.Status.OK).build();
        });
    }

    @Override
    public Response deactivate(ActivationResource activationResource) {
        String errorMessage = COULD_NOT_DEACTIVATE_USER + activationResource.getUserId();
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            userEditService.deactivateUser(activationResource.getUserId(), identity);
            return Response.ok().status(Response.Status.OK).build();
        });
    }

    @Override
    public Response deleteUser(String userId) {
        String errorMessage = COULD_NOT_DELETE_USER + userId;
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            Locale locale = localeProvider.getLocaleFromHeaders(headers);
            userEditService.deleteUser(userId, identity, locale);
            return Response.ok().status(Response.Status.OK).build();
        });
    }

    @Override
    public Response getUserPreferences(String userId) {
        String errorMessage = COULD_NOT_GET_USER_PREFERENCES + userId;
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            String decodedId = URLDecoder.decode(userId, StandardCharsets.UTF_8);
            UserPreference userPreference = userPreferenceService.getUserPreferenceByUserId(decodedId, identity);
            return Response.ok().status(Response.Status.OK).entity(userPreference).build();
        });
    }

    @Override
    public Response getSingleUserPreference(String userId, String preferenceId) {
        String errorMessage = COULD_NOT_GET_SINGLE_PREFERENCE + userId;
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            String decodedId = URLDecoder.decode(userId, StandardCharsets.UTF_8);
            String decodedPreferenceId = URLDecoder.decode(preferenceId, StandardCharsets.UTF_8);
            Object preference = userPreferenceService.getUserPreferenceByPreferenceName(decodedId, decodedPreferenceId, identity);
            return Response.ok().status(Response.Status.OK).entity(preference).build();
        });
    }

    @Override
    public Response getProfilePicture(String userId, String format) {
        String errorMessage = COULD_NOT_GET_PROFILE_PICTURE + userId;
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            String decodedId = URLDecoder.decode(userId, StandardCharsets.UTF_8);
            byte[] preference = userPreferenceService.getProfilePicture(decodedId, format, identity);
            return Response.ok().status(Response.Status.OK).entity(preference).build();
        });
    }

    @Override
    public Response updateUserPreference(String userId, UserPreferenceResource userPreferenceResource) {
        String errorMessage = COULD_NOT_UPDATE_USER_PREFERENCES + userId;
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            String decodedId = URLDecoder.decode(userId, StandardCharsets.UTF_8);
            UserPreference updatedUserPreference = userPreferenceService.updateUserPreferenceByUserId(decodedId, userPreferenceResource, identity);
            return Response.ok().status(Response.Status.OK).entity(updatedUserPreference).type(MediaType.APPLICATION_JSON).build();
        });
    }

    @Override
    public Response updateProfilePicture(String userId, byte[] profilePicture) {
        String errorMessage = COULD_NOT_UPDATE_PROFILE_PICTURE_OF_USER + userId;
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            String decodedId = URLDecoder.decode(userId, StandardCharsets.UTF_8);
            byte[] updatedProfilePicture = userPreferenceService.updateProfilePicture(decodedId, profilePicture, identity);
            return Response.ok().status(Response.Status.OK).entity(updatedProfilePicture).build();
        });
    }

    @Override
    public Response deleteUserProfilePicture(String userId) {
        String errorMessage = COULD_NOT_DELETE_PROFILE_PICTURE + userId;
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            String decodedId = URLDecoder.decode(userId, StandardCharsets.UTF_8);
            userPreferenceService.deleteUserProfilePicture(decodedId, identity);
            return Response.ok().status(Response.Status.OK).build();
        });
    }

    @Override
    public Response joinGroup(String userId, GroupIdResource groupIdResource) {
        String errorMessage = userId + " could not join the group " + groupIdResource;
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            String decodedId = URLDecoder.decode(userId, StandardCharsets.UTF_8);
            groupService.joinGroup(decodedId, groupIdResource.getGroupId(), identity);
            return Response.ok().status(Response.Status.OK).build();
        });
    }

    @Override
    public Response leaveGroup(String userId, GroupIdResource groupIdResource) {
        String errorMessage = userId + " could not leave the group " + groupIdResource;
        return executeActionAndCatchPotentialException(identity, logger, errorMessage, () -> {
            String decodedId = URLDecoder.decode(userId, StandardCharsets.UTF_8);
            groupService.leaveGroup(decodedId, groupIdResource.getGroupId(), identity);
            return Response.ok().status(Response.Status.OK).build();
        });
    }


}
