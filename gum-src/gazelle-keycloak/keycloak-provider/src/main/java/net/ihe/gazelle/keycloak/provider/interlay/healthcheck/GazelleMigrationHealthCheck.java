package net.ihe.gazelle.keycloak.provider.interlay.healthcheck;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Startup;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@Startup
@ApplicationScoped
public class GazelleMigrationHealthCheck implements HealthCheck {

    public static final String MIGRATION_PERSISTENCE_FILE = "/opt/app-version/gazelle-user-management-keycloak";
    public static final String APP_VERSION = "APP_VERSION";

    @Override
    public HealthCheckResponse call() {
        //TODO need to improve the way of check that the migration is over
        File myFile = new File(MIGRATION_PERSISTENCE_FILE);
        try {
            try (Scanner myReader = new Scanner(myFile)) {

                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    // Remove potential -snapshot of jira id after version number with regex
                    String version = System.getenv(APP_VERSION);
                    if (version == null) {
                        return HealthCheckResponse.down("APP_VERSION is not set, unable to check the state of migrations.");
                    }
                    String cleanVersion = version.replaceAll("-.*", "");

                    if (data.equals(cleanVersion))
                        return HealthCheckResponse.up("All migrations are done.");
                }
            }
        } catch (FileNotFoundException | IllegalStateException e) {
            return HealthCheckResponse.down("Unable to check the state of migrations");
        }
        return HealthCheckResponse.down("Migrations are not done yet");
    }
}