package net.ihe.gazelle.maestro.client;

import java.io.Serial;

/**
 * Represents an HTTP error encountered while interacting with a Maestro service.
 * This exception extends {@code MaestroClientException} to include the HTTP response body.
 */
public class MaestroHttpError extends MaestroClientException {

   @Serial
   private static final long serialVersionUID = 5611903462212664650L;

   /**
    * Stores the HTTP response body associated with an error.
    */
   private final String body;

   /**
    * Constructs a new {@code MaestroHttpError} with the specified HTTP status code and body content.
    *
    * @param status The HTTP status code associated with the error.
    * @param body   The content of the HTTP response body, which can provide additional error details.
    */
   public MaestroHttpError(int status, String body) {
      super(status, "HTTP " + status + (body != null && !body.isBlank() ? ": " + body : ""));
      this.body = body;
   }

   /**
    * Retrieves the HTTP response body associated with the error.
    *
    * @return the content of the HTTP response body, or {@code null} if the body is not present.
    */
   public String getBody() {
      return body;
   }
}
