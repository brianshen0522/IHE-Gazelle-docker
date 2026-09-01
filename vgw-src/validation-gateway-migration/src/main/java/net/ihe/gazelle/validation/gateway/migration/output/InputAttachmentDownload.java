package net.ihe.gazelle.validation.gateway.migration.output;

import java.io.IOException;
import java.io.OutputStream;

public record InputAttachmentDownload(String filename, String contentType, AttachmentWriter writer) {

   @FunctionalInterface
   public interface AttachmentWriter {
      void writeTo(OutputStream outputStream) throws IOException;
   }
}
