package net.ihe.gazelle.user.management.api.interlay.user;

import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.api.interlay.organization.OrganizationCreationRequest;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class UserRegisterRequestTest {

    public static final String REQUEST_FIRSTNAME = "requestFirstName";
    public static final String REQUEST_LASTNAME = "requestLastName";
    public static final String REQUEST_EMAIL = "requestEmail";
    public static final String REQUEST_ORGANIZATION_ID = "organizationId";

    @Test
    void testBasicConstructorWithSetters() {
        UserRegisterRequest userRegisterRequest = getUserRegisterRequest();

        assertEquals(REQUEST_FIRSTNAME, userRegisterRequest.getFirstName());
        assertEquals(REQUEST_LASTNAME, userRegisterRequest.getLastName());
        assertEquals(REQUEST_EMAIL, userRegisterRequest.getEmail());
        assertEquals(true, userRegisterRequest.getConsent());
        assertEquals(REQUEST_ORGANIZATION_ID, userRegisterRequest.getOrganizationId());
        assertEquals(new OrganizationCreationRequest(), userRegisterRequest.getOrganization());
        assertEquals("password1", userRegisterRequest.getPassword());
        assertEquals("password2", userRegisterRequest.getPasswordConfirmation());
    }

    @Test
    void testAsUserMethod() {
        UserRegisterRequest userRegisterRequest = getUserRegisterRequest();
        User user = userRegisterRequest.asUser();

        assertEquals(REQUEST_FIRSTNAME, user.getFirstName());
        assertEquals(REQUEST_LASTNAME, user.getLastName());
        assertEquals(REQUEST_EMAIL, user.getEmail());
        assertEquals(REQUEST_ORGANIZATION_ID, user.getOrganizationId());
    }

    @Test
    void testEqualsAndHashcode() {
        EqualsVerifier.simple().forClass(UserRegisterRequest.class).verify();
    }

    private static UserRegisterRequest getUserRegisterRequest() {
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest();
        userRegisterRequest.setFirstName(REQUEST_FIRSTNAME);
        userRegisterRequest.setLastName(REQUEST_LASTNAME);
        userRegisterRequest.setEmail(REQUEST_EMAIL);
        userRegisterRequest.setConsent(true);
        userRegisterRequest.setOrganizationId(REQUEST_ORGANIZATION_ID);
        userRegisterRequest.setOrganization(new OrganizationCreationRequest());
        userRegisterRequest.setPassword("password1");
        userRegisterRequest.setPasswordConfirmation("password2");
        return userRegisterRequest;
    }


}
