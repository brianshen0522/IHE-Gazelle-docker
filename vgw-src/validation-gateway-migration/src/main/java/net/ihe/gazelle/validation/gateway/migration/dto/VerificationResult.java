package net.ihe.gazelle.validation.gateway.migration.dto;

public record VerificationResult(String evsOid, boolean foundInDatahouse, String itemId, boolean hasInput) {
}
