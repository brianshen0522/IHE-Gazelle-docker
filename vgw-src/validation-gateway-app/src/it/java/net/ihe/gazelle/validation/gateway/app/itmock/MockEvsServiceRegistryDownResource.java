package net.ihe.gazelle.validation.gateway.app.itmock;

import java.util.HashMap;
import java.util.Map;

public class MockEvsServiceRegistryDownResource extends MockEvsServiceResource {

    public MockEvsServiceRegistryDownResource() {
        super("SERVICE_REGISTRY_DOWN");
    }

    @Override
    public Map<String, String> start() {
        Map<String, String> properties = new HashMap<>(super.start());
        properties.put("gzl.service.registry.enabled", "true");
        properties.put("gzl.service.registry.url", "http://localhost:1/service-registry");
        return properties;
    }
}
