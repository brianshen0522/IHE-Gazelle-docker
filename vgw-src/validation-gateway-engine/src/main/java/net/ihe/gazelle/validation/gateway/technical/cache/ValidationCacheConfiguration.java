package net.ihe.gazelle.validation.gateway.technical.cache;

/**
 * Configuration for validation profile caching policies.
 */
public interface ValidationCacheConfiguration {

   /**
    * @return the maximum number of cache entries for validation profiles.
    */
   long getProfileCacheMaxSize();

   /**
    * @return the time-to-live applied to cached validation profiles.
    */
   long getProfileCacheTtlSeconds();

   /**
    * @return the cooldown delay after a failed profiles refresh before retrying.
    */
   long getProfileCacheFailureCooldownSeconds();

   ValidationCacheConfiguration DEFAULT = new DefaultValidationCacheConfiguration();

   final class DefaultValidationCacheConfiguration implements ValidationCacheConfiguration {

      private static final long DEFAULT_MAX_SIZE = 256;
      private static final long DEFAULT_TTL_SECONDS = 60L * 5;
      private static final long DEFAULT_FAILURE_COOLDOWN_SECONDS = 60;

      @Override
      public long getProfileCacheMaxSize() {
         return DEFAULT_MAX_SIZE;
      }

      @Override
      public long getProfileCacheTtlSeconds() {
         return DEFAULT_TTL_SECONDS;
      }

      @Override
      public long getProfileCacheFailureCooldownSeconds() {
         return DEFAULT_FAILURE_COOLDOWN_SECONDS;
      }
   }
}
