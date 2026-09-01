package net.ihe.gazelle.keycloak.provider.interlay.service;

import net.ihe.gazelle.keycloak.core.application.event.GazelleEventException;
import net.ihe.gazelle.keycloak.provider.interlay.notification.InactiveUserNotificationSender;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.commons.interlay.email.ActivationLinkBuilder;
import org.keycloak.email.EmailException;
import org.keycloak.models.GroupModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

import java.util.Optional;

public class InactiveUserEventServiceImpl implements InactiveUserEventService {
    private final ApplicationConfig applicationConfig;

    public InactiveUserEventServiceImpl(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Override
    public void eventLoginOnInactiveAccount(RealmModel realm, UserModel user, UserLookupService userLookupService, InactiveUserNotificationSender inactiveUserNotificationSender) {
        if (user == null || realm == null || userLookupService == null || inactiveUserNotificationSender == null)
            throw new IllegalArgumentException("One of the parameters is null");
        ActivationLinkBuilder activationLinkBuilder = new ActivationLinkBuilder(applicationConfig);
        String activationLink = activationLinkBuilder.buildActivationLink(userLookupService.getActivationCodeForUserId(user.getUsername()));
        if (shouldReceiveAnEmail(user, activationLink)) {
            try {
                if (isAllowedToReceiveActivationLink(realm, user))
                    inactiveUserNotificationSender.sendInactiveAccountVendorAdmin(applicationConfig.getRootTestBedUrl(), activationLink);
                else
                    inactiveUserNotificationSender.sendInactiveAccountVendor(applicationConfig.getRootTestBedUrl());
            } catch (EmailException e) {
                throw new GazelleEventException("Unable to send email for inactive user", e);
            }
        }
    }


    @Override
    public void eventLoginOnDelegatedInactiveAccount(UserModel userModel, UserDelegationService userDelegationService) {
        if (userModel.isEnabled()) {
            userDelegationService.activateDelegatedUser(userModel.getUsername());
        }
    }

    private boolean shouldReceiveAnEmail(UserModel userModel, String activationLink) {
        //user without an activation code, meaning that it manually deactivated, should not receive an email
        return userModel != null && !userModel.isEnabled() && activationLink != null && !activationLink.isEmpty();
    }

    private boolean isAllowedToReceiveActivationLink(RealmModel realm, UserModel userModel) {
        //group name is from tm
        Optional<GroupModel> groupModelOptional = userModel.getGroupsStream().findFirst();
        if (groupModelOptional.isPresent()) {
            RoleModel roleModel = realm.getRole("org-adm:" + groupModelOptional.get().getName());
            return roleModel != null && userModel.hasRole(roleModel);
        } else
            return false;
    }
}
