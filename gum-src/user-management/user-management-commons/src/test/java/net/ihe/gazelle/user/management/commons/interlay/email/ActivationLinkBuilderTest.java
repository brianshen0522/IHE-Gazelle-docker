package net.ihe.gazelle.user.management.commons.interlay.email;

import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.commons.ConfigurationsMock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ActivationLinkBuilderTest {

    @Test
    void buildActivationLinkTest() {
        ApplicationConfig applicationConfig = new ConfigurationsMock();
        ActivationLinkBuilder activationLinkBuilder= new ActivationLinkBuilder(applicationConfig);

        String activationCode = "activationCode";
        String expectedActivationLink = applicationConfig.getGUMUIBaseUrl()
                .concat(ActivationLinkBuilder.ACTIVATE_USER_PATH).concat(activationCode);
        assertEquals(expectedActivationLink, activationLinkBuilder.buildActivationLink(activationCode));
    }

    @Test
    void buildActivationLinkNullOrEmpty() {
        ApplicationConfig applicationConfig = new ConfigurationsMock();
        ActivationLinkBuilder activationLinkBuilder= new ActivationLinkBuilder(applicationConfig);

        assertNull(activationLinkBuilder.buildActivationLink(null));
        assertNull(activationLinkBuilder.buildActivationLink(""));
    }
}