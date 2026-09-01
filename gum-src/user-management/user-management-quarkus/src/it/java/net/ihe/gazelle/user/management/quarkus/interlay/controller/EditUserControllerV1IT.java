package net.ihe.gazelle.user.management.quarkus.interlay.controller;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
@QuarkusTestResource(KeycloakMockResource.class)
class EditUserControllerV1IT extends AbstractEditUserControllerIT {

    @Override
    protected String getBaseUsersPath() {
        return "rest/users";
    }


    @Override
    protected String getFirstname() {
        return "FIRSTNAMEtwo";
    }

    @Override
    protected String getLastname() {
        return "LASTNAMEtwo";
    }

    @Override
    protected String getMail() {
        return "edit-user-controller@test-v1.fr";
    }

    @Override
    protected String getOrgaName() {
        return "KerevalV1";
    }

    @Override
    protected String getOrgaShortname() {
        return "KERV1";
    }

    @Override
    protected String getEmail() {
        return "new-email@test-v1.fr";
    }

}
