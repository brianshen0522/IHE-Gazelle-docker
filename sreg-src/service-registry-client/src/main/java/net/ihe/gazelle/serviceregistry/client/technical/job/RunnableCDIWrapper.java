package net.ihe.gazelle.serviceregistry.client.technical.job;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a wrapper for Runnable that activates the CDI request context before running the wrapped Runnable and terminates it afterward.
 */
public class RunnableCDIWrapper implements Runnable {

   private static final Logger LOG = LoggerFactory.getLogger(RunnableCDIWrapper.class);

   private final Runnable runnable;
   private final ManagedContext requestContext;

   /**
    * Constructs a new RunnableCDIWrapper that wraps the given Runnable and manages the CDI request context.
    * @param runnable a Runnable to be wrapped and executed with an active CDI request context
    */
   public RunnableCDIWrapper(Runnable runnable) {
      this.runnable = runnable;
      requestContext = Arc.container().requestContext();
   }

   @Override
   public void run() {
      requestContext.activate();
      try {
         runnable.run();
      } catch (Exception e) {
         LOG.error("Unexpected error in runnable", e);
      } finally {
         requestContext.terminate();
      }
   }
}
