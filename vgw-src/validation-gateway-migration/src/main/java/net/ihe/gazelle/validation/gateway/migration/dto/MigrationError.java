package net.ihe.gazelle.validation.gateway.migration.dto;

import java.time.Instant;

public record MigrationError(
    String evsOid,
    String message,
    Instant occurredAt,
    MigrationErrorType type
) {
}
