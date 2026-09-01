package net.ihe.gazelle.validation.gateway.migration.output;

import java.nio.file.Path;

public record InputAttachmentSource(
      String filename,
      String contentType,
      Path path,
      String role
) {
}
