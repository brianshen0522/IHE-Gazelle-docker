/*
 * Copyright 2024 IHE International.
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

package net.ihe.gazelle.user.management.core.application.service;

import net.ihe.gazelle.user.management.api.application.user.registration.UserRegistrationException;
import net.ihe.gazelle.user.management.api.application.user.registration.UserRegistrationService;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.ConfigurationsMock;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;
import net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ApplicationConfigMockedForServicesTest {
    public static final String RANDOM_PASSWORD = "Password!35";
    private ConfigurationsMock applicationConfig;

    private UserRegistrationService userRegistrationService;

    @BeforeEach
    void init() {
        applicationConfig = new ConfigurationsMock();
        userRegistrationService = new UserRegistrationServiceImpl(null, null,null, null,null, applicationConfig, null, null);
    }


    @Test
    void userRegistrationDisabledTest() {
        // Update application configurations
        applicationConfig.enableUserRegistration(false);
        applicationConfig.enableOrganizationCreation(true);

        User user = new User();
        Organization organization = new Organization();
        Exception exception = assertThrows(UserRegistrationException.class, () -> userRegistrationService.registerUser(user, true,
                RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH));
        assertEquals(exception.getMessage(), ErrorMessage.USER_REGISTRATION_DISABLED.getMessage());

        exception = assertThrows(UserRegistrationException.class, () -> userRegistrationService.registerUserWithNewOrganization(user, organization, true,
                RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH));
        assertEquals(exception.getMessage(), ErrorMessage.USER_REGISTRATION_DISABLED.getMessage());
    }

    @Test
    void organizationCreationDisabledTest() {
        // Update application configurations
        applicationConfig.enableUserRegistration(true);
        applicationConfig.enableOrganizationCreation(false);

        User user = new User();
        Organization organization = new Organization();
        Exception exception = assertThrows(UserRegistrationException.class, () -> userRegistrationService.registerUserWithNewOrganization(user, organization, true,
                RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH));
        assertTrue(exception.getMessage().contains(ErrorMessage.ORGANIZATION_CREATION_DISABLED.getMessage()));
    }
}
