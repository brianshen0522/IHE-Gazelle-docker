package net.ihe.gazelle.user.management.quarkus.interlay.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.user.management.api.domain.user.UserPreference;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static net.ihe.gazelle.security.mocks.OIDCJWTGenerator.getValidJwt;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserController for user preferences.
 * <p>
 * See src/it/resources/db/migration/R__Init_db_test_values_UserPreferenceControllerIT.sql to know what data is available
 * to add more for this test .
 */
@QuarkusTest
@TestTransaction
@QuarkusTestResource(KeycloakMockResource.class)
class UserPreferenceControllerIT {
    //TODO find a way to make ApplicationConfig work to retrieve gumBaseUrl
    public static final String GUM_BASE_URL = "http://localhost/gum";
    private static final String BEARER = "Bearer ";
    private static final String IMAGE_JPEG = "image/jpeg";
    public static final String PREFERENCE_PICTURE_PATH = "/preferences/picture";
    String profilePictureUri = getProfileImageUri("201", "normal");
    String profileThumbnailUri = getProfileImageUri("201", "thumbnail");
    private final UserPreference persistedUserPreference = new UserPreference("201",
            profilePictureUri,
            profileThumbnailUri,
            "table",
            true,
            List.of("fr"));
    private final String baseUserPath = getUsersBasePath();


    @Test
    void getUserPreferences() {
        String jwt = getValidJwt();

        UserPreference returnedUserPreference = given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .get(baseUserPath + persistedUserPreference.getUserId() + "/preferences/")
                .then()
                .statusCode(200)
                .extract().response().as(UserPreference.class);
        assertEquals(persistedUserPreference, returnedUserPreference);
    }

    @Test
    void getUserPreferencesDefault() {
        UserPreference defaultUserPreference = new UserPreference("200",
                getProfileImageUri("200", "normal"),
                getProfileImageUri("200", "thumbnail"),
                "",
                false,
                Collections.emptyList());
        String jwt = getValidJwt();

        UserPreference userPreference = given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .get(baseUserPath + defaultUserPreference.getUserId() + "/preferences/")
                .then()
                .statusCode(200)
                .extract().response().as(UserPreference.class);

        assertEquals(defaultUserPreference, userPreference);
    }

    @Test
    void getUserPreferencesBadId() {
        String jwt = getValidJwt();

        given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .get(baseUserPath + "bad id" + "/preferences/")
                .then()
                .statusCode(404);
    }

    @Test
    void getSingleUserPreferenceById() {
        String jwt = getValidJwt();

        String tableLabel = given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .get(baseUserPath + persistedUserPreference.getUserId() + "/preferences/tableLabel")
                .then()
                .statusCode(200)
                .extract().response()
                .asString();
        boolean notifiedByEmail = given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .get(baseUserPath + persistedUserPreference.getUserId() + "/preferences/notifiedByEmail")
                .then()
                .statusCode(200)
                .extract().response()
                .as(boolean.class);
        List<String> languagesSpoken = given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .get(baseUserPath + persistedUserPreference.getUserId() + "/preferences/languagesSpoken")
                .then()
                .statusCode(200)
                .extract().response()
                .as(List.class);


        assertEquals(persistedUserPreference.getTableLabel(), tableLabel);
        assertEquals(persistedUserPreference.isNotifiedByEmail(), notifiedByEmail);
        assertEquals(persistedUserPreference.getLanguagesSpoken(), languagesSpoken);
    }

    @Test
    void getPictureTest() {
        String jwt = getValidJwt();

        byte[] profilePicture = given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .get(baseUserPath + persistedUserPreference.getUserId() + "/preferences/picture?format=normal")
                .then()
                .statusCode(200)
                .extract().response()
                .asByteArray();
        byte[] thumbnailPicture = given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .get(baseUserPath + persistedUserPreference.getUserId() + "/preferences/picture?format=thumbnail")
                .then()
                .statusCode(200)
                .extract().response()
                .asByteArray();

        assertArrayEquals(getImageEncodedBytes("src/it/resources/images/img.jpg"), profilePicture);
        assertArrayEquals(getImageEncodedBytes("src/it/resources/images/thumbnail.jpg"), thumbnailPicture);
    }

    @Test
    void getSingleUserPreferenceByIdPreferenceNotFound() {
        String jwt = getValidJwt();
        given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .get(baseUserPath + persistedUserPreference.getUserId() + "/preferences/badPreference")
                .then()
                .statusCode(404);
    }


    @Test
    void updateUserPreferences() {
        UserPreference initialUserPreference = new UserPreference("202",
                getProfileImageUri("202", "normal"),
                getProfileImageUri("202", "thumbnail"),
                "table",
                true,
                List.of("fr"));
        UserPreference updatedUserPreference = new UserPreference("202",
                getProfileImageUri("202", "normal"),
                getProfileImageUri("202", "thumbnail"),
                "b12",
                false,
                List.of("fr", "en", "it"));
        String jwt = getValidJwt();

        UserPreference actualUserPreference = given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .body(generateJson(updatedUserPreference))
                .put(baseUserPath + "202" + "/preferences/")
                .then()
                .statusCode(200)
                .extract().response().as(UserPreference.class);

        assertNotEquals(initialUserPreference, actualUserPreference);
        assertEquals(updatedUserPreference, actualUserPreference);
    }

    @Test
    void updateUserPreferencesUserNotFound() {
        UserPreference updatedUserPreference = new UserPreference("203",
                null,
                null,
                "b12",
                true,
                List.of("fr", "en", "it"));

        UserPreference expectedUserPreference = new UserPreference("203",
                getProfileImageUri("203", "normal"),
                getProfileImageUri("203", "thumbnail"),
                "b12",
                true,
                List.of("fr", "en", "it"));

        String jwt = getValidJwt();
        UserPreference actualUserPreference = given().when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .body(generateJson(updatedUserPreference))
                .put(baseUserPath + "203" + "/preferences/")
                .then()
                .statusCode(200)
                .extract().response().as(UserPreference.class);

        assertEquals(expectedUserPreference, actualUserPreference);
    }

    @Test
    void updateUserProfilePicture() {
        String jwt = getValidJwt();
        String userId = "204";
        byte[] originalProfilePicture = given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .get(baseUserPath + userId + "/preferences/picture?format=normal")
                .then()
                .statusCode(200)
                .extract().response()
                .asByteArray();
        byte[] originalThumbnailPicture = given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .get(baseUserPath + userId + "/preferences/picture?format=thumbnail")
                .then()
                .statusCode(200)
                .extract().response()
                .asByteArray();

        byte[] updatedProfilePicture = given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .body(getImageEncodedBytes("src/it/resources/images/img_rotated.jpg"))
                .contentType(IMAGE_JPEG)
                .put(baseUserPath + userId + PREFERENCE_PICTURE_PATH)
                .then()
                .statusCode(200)
                .extract().response()
                .asByteArray();
        byte[] updatedProfileThumbnail = given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .get(baseUserPath + userId + PREFERENCE_PICTURE_PATH + "?format=thumbnail")
                .then()
                .statusCode(200)
                .extract().response()
                .asByteArray();

        assertFalse(Arrays.equals(originalProfilePicture, updatedProfilePicture));
        assertFalse(Arrays.equals(originalThumbnailPicture, updatedProfileThumbnail));
        assertArrayEquals(getImageEncodedBytes("src/it/resources/images/img_rotated.jpg"), updatedProfilePicture);
        assertArrayEquals(getImageEncodedBytes("src/it/resources/images/thumbnail_rotated.jpg"), updatedProfileThumbnail);
    }

    @Test
    void getUserProfileUserNotFound() {
        String jwt = getValidJwt();
        given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .get(baseUserPath + "5243315" + PREFERENCE_PICTURE_PATH + "?format=thumbnail")
                .then()
                .statusCode(404)
                .extract().response()
                .asByteArray();
    }

    @Test
    void getUserProfileBadFormat() {
        String jwt = getValidJwt();
        given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .get(baseUserPath + "204" + PREFERENCE_PICTURE_PATH + "?format=big")
                .then()
                .statusCode(400)
                .extract().response()
                .asByteArray();
    }

    @Test
    void deleteUserProfilePicture() {
        String jwt = getValidJwt();
        byte[] expectedBeforeDeletePicture = getImageEncodedBytes("src/it/resources/images/img_rotated.jpg");
        byte[] expectedBeforeDeleteThumbnail = getImageEncodedBytes("src/it/resources/images/thumbnail_rotated.jpg");
        String userId = "205";

        byte[] actualProfilePictureBefore = given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .get(baseUserPath + userId + PREFERENCE_PICTURE_PATH + "?format=normal")
                .then()
                .statusCode(200)
                .extract().response()
                .asByteArray();
        assertArrayEquals(expectedBeforeDeletePicture, actualProfilePictureBefore);

        byte[] actualThumbnailPictureBefore = given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .get(baseUserPath + userId + "/preferences/picture?format=thumbnail")
                .then()
                .statusCode(200)
                .extract().response()
                .asByteArray();
        assertArrayEquals(expectedBeforeDeleteThumbnail, actualThumbnailPictureBefore);

        Response response = given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .contentType(IMAGE_JPEG)
                .when()
                .delete(baseUserPath + userId + PREFERENCE_PICTURE_PATH)
                .then()
                .statusCode(200)
                .extract().response();
        assertTrue(response.asString().isEmpty());

        byte[] expectedDefaultPicture = getImageEncodedBytes("src/it/resources/images/default_img.jpg");
        byte[] actualProfilePictureAfter = given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .get(baseUserPath + userId + "/preferences/picture?format=normal")
                .then()
                .statusCode(200)
                .contentType(IMAGE_JPEG)
                .extract().response()
                .asByteArray();
        assertArrayEquals(expectedDefaultPicture, actualProfilePictureAfter);

        byte[] expectedDefaultThumbnail = getImageEncodedBytes("src/it/resources/images/default_thumbnail.jpg");
        byte[] actualThumbnailPictureAfter = given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .get(baseUserPath + userId + "/preferences/picture?format=thumbnail")
                .then()
                .statusCode(200)
                .contentType(IMAGE_JPEG)
                .extract().response()
                .asByteArray();
        assertArrayEquals(expectedDefaultThumbnail, actualThumbnailPictureAfter);
    }

    @Test
    void deleteUserProfileUserNotFound() {
        String jwt = getValidJwt();
        String userId = "notFound";

        given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .contentType(IMAGE_JPEG)
                .delete(baseUserPath + userId + PREFERENCE_PICTURE_PATH)
                .then()
                .statusCode(404)
                .extract().response()
                .asByteArray();
        given()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .when()
                .delete(baseUserPath + userId + PREFERENCE_PICTURE_PATH)
                .then()
                .statusCode(404)
                .extract().response()
                .asByteArray();
    }


    private String generateJson(UserPreference updatedUserPreference) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(updatedUserPreference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] getImageEncodedBytes(String pathname) {
        try {
            return FileUtils.readFileToByteArray(new File(pathname));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getProfileImageUri(String userId, String format) {
        return GUM_BASE_URL + "/rest/v2/users/" + userId + "/preferences/picture?format=" + format;
    }

    protected String getUsersBasePath() {
        return "/rest/v2/users/";
    }
}
