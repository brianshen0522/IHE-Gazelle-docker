package net.ihe.gazelle.validation.gateway.migration.dto;

import java.time.Instant;

public record MigrationPreview(String evsOid, String itemId, String result, Instant validationDate) {
}
