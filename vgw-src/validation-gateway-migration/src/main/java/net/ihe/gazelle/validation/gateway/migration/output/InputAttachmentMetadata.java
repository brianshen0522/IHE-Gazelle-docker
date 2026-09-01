package net.ihe.gazelle.validation.gateway.migration.output;

public record InputAttachmentMetadata(
      String attachmentId,
      String filename,
      String contentType
) {
}
