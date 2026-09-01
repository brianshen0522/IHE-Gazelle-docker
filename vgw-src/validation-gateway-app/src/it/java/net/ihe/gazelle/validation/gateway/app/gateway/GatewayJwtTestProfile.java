package net.ihe.gazelle.validation.gateway.app.gateway;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class GatewayJwtTestProfile implements QuarkusTestProfile {

    // Align JWT validation settings with OIDCJWTGenerator for Quarkus tests.
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "gzl.k8s.id", "test-k8s-id",
                "gzl.sso.url", "http://localhost:12345",
                "gzl.sso.admin.user", "admin",
                "gzl.sso.admin.password", "admin",
                "gzl.m2m.client.secret", "test-secret",
                "mp.jwt.verify.publickey.location", "classpath:public_key.pem",
                "mp.jwt.verify.issuer", "https://localhost:12345",
                "gzl.jwt.verify.audience", "http://localhost",
                "datahouse.indexes.enabled", "false"
        );
    }
}
