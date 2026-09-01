package net.ihe.gazelle.validation.gateway.migration.output;

public record TargetHealth(
      boolean accessible,
      String message
) {
}
