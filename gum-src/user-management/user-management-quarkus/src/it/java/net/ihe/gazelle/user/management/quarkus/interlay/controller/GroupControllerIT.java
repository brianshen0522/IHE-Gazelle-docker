package net.ihe.gazelle.user.management.quarkus.interlay.controller;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import net.ihe.gazelle.user.management.api.domain.group.GroupType;
import net.ihe.gazelle.user.management.api.interlay.group.GroupResource;
import net.ihe.gazelle.user.management.quarkus.utils.JSONMaker;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static net.ihe.gazelle.security.mocks.OIDCJWTGenerator.getValidJwt;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@QuarkusTestResource(net.ihe.gazelle.security.mocks.KeycloakMockResource.class)
class GroupControllerIT {

    private static final String BEARER = "Bearer ";
    public static final String GUM_REST_GROUPS = "/rest/groups";
    public static final String GUM_REST_USERS = "/rest/v2/users";

    @Test
    void testCreateGroup() {
        GroupResource groupResource = createGroupResource(GroupType.ORGANIZATION_ADMIN, "gazelle_administrator", null);
        String jwt = getValidJwt();
        createGroupRequest(jwt, groupResource);

        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(groupResource.toJson())
                .post(GUM_REST_GROUPS)
                .then().statusCode(409);
    }

    @Test
    void testUpdateGroup() {
        GroupResource groupResource = createGroupResource(GroupType.ROLE, "role_need_to_be_updated", null);
        String jwt = getValidJwt();
        createGroupRequest(jwt, groupResource);

        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body("{\"name\": \"New name of group\"}")
                .patch(GUM_REST_GROUPS + "/role:role_need_to_be_updated")
                .then().statusCode(200);

        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .param("reference", "role_need_to_be_updated")
                .get(GUM_REST_GROUPS)
                .then().statusCode(200)
                .body(containsString("\"reference\":\"role_need_to_be_updated\""))
                .body(containsString("\"name\":\"New name of group\""));
    }

    @Test
    void testDeleteGroup() {
        GroupResource groupResource = createGroupResource(GroupType.ROLE, "groupWithDeletionScope", "This group just exist to be deleted");
        String jwt = getValidJwt();
        createGroupRequest(jwt, groupResource);

        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(groupResource.toJson())
                .delete(GUM_REST_GROUPS + "/role:groupWithDeletionScope")
                .then().statusCode(200);

        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .get(GUM_REST_GROUPS + "?reference=groupWithDeletionScope")
                .then()
                .body(containsString("[]"));

    }

    @Test
    void testNoSuchGroupRequest() {
        String jwt = getValidJwt();

        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .param("search", "ThisGroupDoesNotExist")
                .get(GUM_REST_GROUPS)
                .then().statusCode(200).body(equalTo(("[]")));

        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body("{\"name\": \"New name of group\"}")
                .patch(GUM_REST_GROUPS + "/role:non_existing_group")
                .then().statusCode(404);

        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .delete(GUM_REST_GROUPS + "/nonExistingGroup")
                .then().statusCode(404);
    }

    @Test
    void testIllegalGroupRequest() {
        GroupResource groupResource = createGroupResource(GroupType.ORGANIZATION_ADMIN, null, null);
        String jwt = getValidJwt();
        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(groupResource.toJson())
                .post(GUM_REST_GROUPS)
                .then().statusCode(400);

        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .param("type", "jack")
                .get(GUM_REST_GROUPS)
                .then().statusCode(400);

        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .patch(GUM_REST_GROUPS + "/missingGroupResource")
                .then().statusCode(400);
    }

    @Test
    void testSearchForGroup() {
        GroupResource groupResource = createGroupResource(GroupType.ORGANIZATION, "gazelle_administrator2", "Gazelle administrator 2");
        String jwt = getValidJwt();
        createGroupRequest(jwt, groupResource);

        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .body(groupResource.toJson()).param("type", "org")
                .get(GUM_REST_GROUPS)
                .then().statusCode(200)
                .body(containsString("\"reference\":\"gazelle_administrator2\""))
                .body(containsString("\"name\":\"Gazelle administrator 2\""));
    }

    @Test
    void testGetGroupById() {
        GroupResource groupResource = createGroupResource(GroupType.ORGANIZATION, "gazelle_administratorR", "Gazelle administrator R");
        String jwt = getValidJwt();
        createGroupRequest(jwt, groupResource);

        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .get(GUM_REST_GROUPS + "/org:gazelle_administratorR")
                .then().statusCode(200)
                .body(containsString("\"name\":\"Gazelle administrator R\""));
    }

    @Test
    void testJoinAndLeaveGroup() {
        GroupResource groupResource = createGroupResource(GroupType.ROLE, "group_to_join", null);
        String jwt = getValidJwt();
        createGroupRequest(jwt, groupResource);

        String body = JSONMaker.makeUserCreation("userGroup-fn", "userGroup-ln", "userGroup@test.fr", "KER4", "Kereval4");
        ExtractableResponse<Response> extractableResponse = given().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(body)
                .when().post(GUM_REST_USERS + "/register")
                .then().statusCode(201).extract();

        String userID = extractableResponse.body().jsonPath().getString("id");
        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body("{\"groupId\":\"role:group_to_join\"}")
                .post(GUM_REST_USERS + "/" + userID + "/groups/join")
                .then().statusCode(200);

        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .get(GUM_REST_USERS + "/" + userID)
                .then().statusCode(200)
                .body("groupIds", hasItem("role:group_to_join"));

        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body("{\"groupId\":\"role:group_to_join\"}")
                .post(GUM_REST_USERS + "/" + userID + "/groups/leave")
                .then().statusCode(200);

        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .get(GUM_REST_USERS + "/" + userID)
                .then().statusCode(200)
                .body("groups", not(hasItem("role:group_to_join")));
    }

    @Test
    void testEditInGroupToAssociatedUser() {
        GroupResource groupResource = createGroupResource(GroupType.ROLE, "in_group", null);
        String jwt = getValidJwt();
        createGroupRequest(jwt, groupResource);

        String body = JSONMaker.makeUserCreation("userInGroup-fn", "userInGroup-ln", "userInGroup@test.fr", "KER5", "Kereval5");
        ExtractableResponse<Response> extractableResponse = given().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(body)
                .when().post(GUM_REST_USERS + "/register")
                .then().statusCode(201).extract();

        String orgaId = extractableResponse.body().jsonPath().getString("organizationId");

        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body("{\"inGroupIds\": [\"role:in_group\"]}")
                .patch(GUM_REST_GROUPS + "/org-adm:" + orgaId)
                .then().statusCode(200);

        String userID = extractableResponse.body().jsonPath().getString("id");
        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .get(GUM_REST_USERS + "/" + userID)
                .then().statusCode(200)
                .body("groupIds", hasItem("org-adm:" + orgaId))
                .body("groupIds", hasItem("org:" + orgaId))
                .body("groupIds", hasItem("role:in_group"));
    }


    private static void createGroupRequest(String jwt, GroupResource groupResource) {
        given().when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(groupResource.toJson())
                .post(GUM_REST_GROUPS)
                .then().statusCode(201);
    }

    private static GroupResource createGroupResource(GroupType groupType, String reference, String name) {
        GroupResource groupResource = new GroupResource();
        groupResource.setReference(reference);
        groupResource.setType(groupType.getPrefix());
        groupResource.setName(name);
        return groupResource;
    }
}
