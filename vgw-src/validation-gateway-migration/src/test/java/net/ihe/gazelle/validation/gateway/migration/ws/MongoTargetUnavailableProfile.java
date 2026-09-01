package net.ihe.gazelle.validation.gateway.migration.ws;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class MongoTargetUnavailableProfile implements QuarkusTestProfile {

   @Override
   public Map<String, String> getConfigOverrides() {
      return Map.of(
            "mongodb.connection.string", "mongodb://127.0.0.1:1/?serverSelectionTimeoutMS=150",
            "mongodb.database", "migration_test",
            "mongodb.connection.server-selection-timeout-ms", "150",
            "mongodb.connection.connect-timeout-ms", "150",
            "mongodb.connection.max-wait-ms", "150"
      );
   }
}
