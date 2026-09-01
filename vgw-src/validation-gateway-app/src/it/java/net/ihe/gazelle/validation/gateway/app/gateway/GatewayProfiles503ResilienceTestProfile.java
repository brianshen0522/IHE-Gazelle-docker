package net.ihe.gazelle.validation.gateway.app.gateway;

import java.util.HashMap;
import java.util.Map;

public class GatewayProfiles503ResilienceTestProfile extends GatewayJwtTestProfile {

   @Override
   public Map<String, String> getConfigOverrides() {
      Map<String, String> overrides = new HashMap<>(super.getConfigOverrides());
      overrides.put("gzl.validation.cache.profiles.ttl-seconds", "1");
      overrides.put("gzl.validation.cache.profiles.failure-cooldown-seconds", "60");
      return overrides;
   }
}
