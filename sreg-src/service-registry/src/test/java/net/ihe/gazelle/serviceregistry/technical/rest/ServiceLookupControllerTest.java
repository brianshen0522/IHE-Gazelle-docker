/*
 * Copyright 2025 IHE International.
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

package net.ihe.gazelle.serviceregistry.technical.rest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.inject.spi.CDI;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.security.mocks.OIDCJWTGenerator;
import net.ihe.gazelle.serviceregistry.business.lookup.RegistrationUtil;
import net.ihe.gazelle.serviceregistry.business.registration.ServiceRegistration;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(KeycloakMockResource.class)
class ServiceLookupControllerTest {

    private static final String TOKEN = OIDCJWTGenerator.getValidJwt();

    @BeforeAll
    void init() {
        new RegistrationUtil(CDI.current().select(ServiceRegistration.class).get())
                .registerAllServices();
    }


    @Test
    void testGetServiceNotFound() {
        given().log().ifValidationFails()
                .when().header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .get("/services/999999/xyz")
                .then()
                .log().ifValidationFails()
                .statusCode(404)
                .contentType("text/plain")
                .body(is("Service not found: ServiceId{instanceId='999999', replicaId='xyz'} does not exist."));
    }

    @Test
    void testGetServiceInvalidId() {
        given().log().ifValidationFails()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .get("/services/   /123")
                .then()
                .log().ifValidationFails()
                .statusCode(400)
                .contentType("text/plain")
                .body(containsString("Instance ID cannot be null or blank"));
    }

    @Test
    void testGetIndexes() {
        given().log().ifValidationFails()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .get("/services/indexes")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", is(6))
                .body("[0].name", is("name"))
                .body("[1].name", is("instanceId"))
                .body("[2].name", is("selfRegistered"))
                .body("[3].name", is("status"))
                .body("[4].name", is("providedInterface"))
                .body("[5].name", is("consumedInterface"));
    }

    @Test
    void testSearchAll() {
        given().log().ifValidationFails()
                .when().header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .get("/services")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .header("Content-Range", "DeployedService 1-9/9")
                .contentType("application/json")
                .body("size()", is(9))
                .body("[0].name", is("EVS Client"))
                .body("[1].name", is("HL7v2 Validator"))
                .body("[2].name", is("IUA Simulator"))
                .body("[3].name", is("Maestro"))
                .body("[4].name", is("Maestro"))
                .body("[5].name", is("mCSD Simulator"))
                .body("[6].name", is("MHD Simulator"))
                .body("[7].name", is("Test Management"))
                .body("[8].name", is("XML Validator"));
    }

    @Test
    void testSearchWithPagination() {
        given().log().ifValidationFails()
                .when().header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .get("/services?_offset=0&_limit=4")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .header("Content-Range", "DeployedService 1-4/9")
                .contentType("application/json")
                .body("size()", is(4))
                .body("[0].name", is("EVS Client"))
                .body("[1].name", is("HL7v2 Validator"))
                .body("[2].name", is("IUA Simulator"))
                .body("[3].name", is("Maestro"));

        given().log().ifValidationFails()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .get("/services?_offset=4&limit=20")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .header("Content-Range", "DeployedService 5-9/9")
                .contentType("application/json")
                .body("size()", is(5))
                .body("[0].name", is("Maestro"))
                .body("[1].name", is("mCSD Simulator"))
                .body("[2].name", is("MHD Simulator"))
                .body("[3].name", is("Test Management"))
                .body("[4].name", is("XML Validator"));

    }

    @Test
    void testSearchAvailableSimulatorFromProvidedInterface() {
        given().log().ifValidationFails()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .get("/services?providedInterface={interface}&status={status}",
                        Map.of(
                                "interface", "Gazelle Simulation API",
                                "status", "AVAILABLE,UNKNOWN"
                        ))
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .header("Content-Range", "DeployedService 1-3/3")
                .contentType("application/json")
                .body("size()", is(3))
                .body("[0].name", is("IUA Simulator"))
                .body("[1].name", is("mCSD Simulator"))
                .body("[2].name", is("MHD Simulator"));
    }

    @Test
    void testSearchAvailableSimulatorFromConsumedInterface() {
        given().log().ifValidationFails()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .get("/services?consumedInterface={interface}&status={status}",
                        Map.of(
                                "interface", "Service Registration API",
                                "status", "AVAILABLE,UNKNOWN"
                        ))
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .header("Content-Range", "DeployedService 1-2/2")
                .contentType("application/json")
                .body("size()", is(2))
                .body("[0].name", is("HL7v2 Validator"))
                .body("[1].name", is("mCSD Simulator"));
    }

    @Test
    void testGetPossibleValuesForStatus() {
        given().log().ifValidationFails()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .get("/services/indexes/status/values")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/json")
                .body("contains('UNKNOWN')", is(true))
                .body("contains('AVAILABLE')", is(true));


    }

    @Test
    void testGetPossibleValuesForName() {
        given().log().ifValidationFails()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .get("/services/indexes/name/values")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", is(8))
                .body("contains('mCSD Simulator')", is(true));
    }

    @Test
    void testGetPossibleValuesUnauthorized() {
        given().log().ifValidationFails()
                .when()
                .get("/services")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", is(9))
                .body("contains('version')", is(false))
                .body("contains('AVAILABLE')", is(false))
                .body("contains('UNREACHABLE')", is(false));
    }
}