package net.ihe.gazelle.validation.gateway.migration.evs;

public record EvsHandledObjectSource(
      int id,
      String role,
      String originalFileName,
      String filePath
) {
}
