package net.ihe.gazelle.user.management.quarkus.interlay.controller;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;


@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(KeycloakMockResource.class)
@TestTransaction
class EditUserControllerIT extends AbstractEditUserControllerIT{

    @Override
    protected String getBaseUsersPath() {
        return "rest/v2/users";
    }
}
