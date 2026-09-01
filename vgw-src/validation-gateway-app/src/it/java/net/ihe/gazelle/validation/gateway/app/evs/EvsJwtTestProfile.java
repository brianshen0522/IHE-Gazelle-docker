package net.ihe.gazelle.validation.gateway.app.evs;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class EvsJwtTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "gzl.k8s.id", "test-k8s-id",
                "mp.jwt.verify.publickey.location", "classpath:public_key.pem",
                "mp.jwt.verify.issuer", "https://localhost:12345",
                "gzl.jwt.verify.audience", "http://localhost",
                "datahouse.indexes.enabled", "false"
        );
    }
}
