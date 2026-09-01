package net.ihe.gazelle.maestro.client;

import java.io.Serial;

/**
 * Represents an exception that occurs specifically during a connection attempt with the Maestro service.
 * This exception is a specialized form of {@code MaestroClientException} and is used
 * when connection-related errors prevent successful interaction with the service..
 */
public class MaestroConnectionException extends MaestroClientException {

   @Serial
   private static final long serialVersionUID = 6817384582306795509L;

   /**
    * Constructs a new {@code MaestroConnectionException} with the specified detail message and cause.
    * This exception is thrown when a connection-related error occurs while interacting with the Maestro service.
    *
    * @param message The detail message providing additional information about the connection error.
    * @param cause   The cause of the exception, which may provide further context for debugging or error analysis.
    */
   public MaestroConnectionException(String message, Throwable cause) {
      super(0, message, cause);
   }
}
