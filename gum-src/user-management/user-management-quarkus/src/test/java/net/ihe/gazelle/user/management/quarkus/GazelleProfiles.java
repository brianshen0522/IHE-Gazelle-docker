package net.ihe.gazelle.user.management.quarkus;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Collections;
import java.util.Set;

public class GazelleProfiles {

    public static class UnitTest implements QuarkusTestProfile {
        @Override
        public Set<String> tags() {
            return Collections.singleton("unit-test");
        }
    }

    public static class IntegrationTest implements QuarkusTestProfile {
        @Override
        public Set<String> tags() {
            return Collections.singleton("integration-test");
        }
    }
}
