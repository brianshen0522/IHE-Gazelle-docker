package net.ihe.gazelle.validation.gateway.app.evs;

import java.util.HashMap;
import java.util.Map;

public class EvsServiceRegistryDownJwtTestProfile extends EvsJwtTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> overrides = new HashMap<>(super.getConfigOverrides());
        overrides.put("gzl.service.registry.enabled", "true");
        overrides.put("gzl.service.registry.url", "http://localhost:1/service-registry");
        return overrides;
    }
}
