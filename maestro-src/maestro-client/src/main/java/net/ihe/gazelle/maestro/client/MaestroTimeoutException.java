package net.ihe.gazelle.maestro.client;

import java.io.Serial;

/**
 * Thrown to indicate that a timeout occurred while interacting with the Maestro service.
 * This exception extends {@code MaestroClientException} and provides additional details
 * about the timeout scenario.
 */
public class MaestroTimeoutException extends MaestroClientException {

   @Serial
   private static final long serialVersionUID = 1854799604890153727L;

   /**
    * Constructs a new {@code MaestroTimeoutException} with the specified detail message
    * and cause. This exception is thrown to indicate that a timeout occurred while
    * interacting with the Maestro service.
    *
    * @param message The detail message describing the reason for the timeout.
    * @param cause   The cause of the timeout, which allows for exception chaining.
    */
   public MaestroTimeoutException(String message, Throwable cause) {
      super(0, message, cause);
   }
}
