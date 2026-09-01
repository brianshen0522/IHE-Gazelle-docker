package net.ihe.gazelle.maestro.client;

import java.io.Serial;

/**
 * Represents a generic exception that occurs while interacting with the Maestro service.
 * This exception provides additional context through an HTTP status code.
 */
public class MaestroClientException extends RuntimeException {

   @Serial
   private static final long serialVersionUID = -5157212852508596255L;

   /**
    * The HTTP status code associated with this exception.
    */
   private final int status;

   /**
    * Constructs a new {@code MaestroClientException} with the specified HTTP status code and detail message.
    *
    * @param status  The HTTP status code associated with the error.
    * @param message The detail message providing additional information about the error.
    */
   public MaestroClientException(int status, String message) {
      super(message);
      this.status = status;
   }

   /**
    * Constructs a new {@code MaestroClientException} with the specified HTTP status code,
    * detail message, and cause.
    *
    * @param status  The HTTP status code associated with the error.
    * @param message The detail message providing additional information about the error.
    * @param cause   The cause of the exception, which may provide further context
    *                for debugging or error analysis.
    */
   public MaestroClientException(int status, String message, Throwable cause) {
      super(message, cause);
      this.status = status;
   }

   /**
    * Retrieves the HTTP status code associated with this exception.
    *
    * @return the HTTP status code that represents the error condition.
    */
   public int getStatus() {
      return status;
   }
}
