/*
 * Copyright 2024 IHE International.
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

package net.ihe.gazelle.user.management.quarkus.interlay.healthcheck;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Startup;
import org.flywaydb.core.Flyway;

/**
 * Startup health check to ensure Flyway migrations are up to date.
 */
@ApplicationScoped
@Startup
public class FlywayDBMigrationHealthCheck implements HealthCheck {

    private final Flyway flyway;
    private volatile boolean migrationsUpToDate;

    /**
     * Creates the health check with the Flyway instance.
     *
     * @param flyway Flyway instance used to inspect pending migrations
     */
    @Inject
    public FlywayDBMigrationHealthCheck(Flyway flyway) {
        this.flyway = flyway;
    }

    @PostConstruct
    void initializeMigrationStatus() {
        // Compute migration status once during startup to avoid repeated Flyway info calls on each health probe.
        migrationsUpToDate = flyway.info().pending().length == 0;
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder response = HealthCheckResponse.named("Flyway DB migration health check");
        return migrationsUpToDate ? response.up().build() : response.down().build();
    }
}
