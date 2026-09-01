package net.ihe.gazelle.validation.gateway.technical.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.ihe.gazelle.validation.v2.api.business.ValidationService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class ValidationProfileCache {

   private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ValidationProfileCache.class);

   private final Cache<String, CachedProfiles> cache;
   private final Duration failureCooldown;
   private final Duration cacheEntryRetention;

   public ValidationProfileCache() {
      this(ValidationCacheConfiguration.DEFAULT);
   }

   public ValidationProfileCache(ValidationCacheConfiguration configuration) {
      requireNonNull(configuration, "configuration must not be null");
      Duration ttl = Duration.ofSeconds(configuration.getProfileCacheTtlSeconds());
      this.failureCooldown = Duration.ofSeconds(configuration.getProfileCacheFailureCooldownSeconds());
      this.cacheEntryRetention = computeCacheEntryRetention(ttl, failureCooldown);
      this.cache = Caffeine.newBuilder()
            .maximumSize(configuration.getProfileCacheMaxSize())
            .expireAfterWrite(cacheEntryRetention)
            .build();
   }

   public ValidationProfileCache(long maxSize, Duration ttl) {
      this(maxSize, ttl, Duration.ofSeconds(ValidationCacheConfiguration.DEFAULT.getProfileCacheFailureCooldownSeconds()));
   }

   public ValidationProfileCache(long maxSize, Duration ttl, Duration failureCooldown) {
      Duration effectiveTtl = requireNonNull(ttl, "ttl must not be null");
      this.failureCooldown = requireNonNull(failureCooldown, "failureCooldown must not be null");
      this.cacheEntryRetention = computeCacheEntryRetention(effectiveTtl, this.failureCooldown);
      this.cache = Caffeine.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(cacheEntryRetention)
            .build();
   }

   public List<ValidationProfile> getProfiles(String cacheKey, ValidationService validationService) {
      Objects.requireNonNull(cacheKey, "cacheKey must not be null");
      Objects.requireNonNull(validationService, "validationService must not be null");
      CachedProfiles cached = cache.getIfPresent(cacheKey);
      Instant now = Instant.now();
      if (cached != null && cached.shouldSkipFetch(now, failureCooldown)) {
         return cached.profiles();
      }
      try {
         ProfileFetchCapable.ProfileFetchResponse response = tryFetchProfiles(validationService, cached);
         if (response == null) {
            if (cached != null) {
               return cached.profiles();
            }
            List<ValidationProfile> profiles = validationService.getValidationProfiles();
            CachedProfiles updated = new CachedProfiles(null, profiles, now, null);
            cache.put(cacheKey, updated);
            return profiles;
         }
         if (response.status() == 304 && cached != null) {
            cache.put(cacheKey, cached.touch(now).clearFailure());
            return cached.profiles();
         }
         if (response.status() == 200 && response.profiles() != null) {
            CachedProfiles updated = new CachedProfiles(response.etag(), response.profiles(), now, null);
            cache.put(cacheKey, updated);
            return updated.profiles();
         }
         return List.of();
      } catch (RuntimeException e) {
         if (cached != null) {
            logger.warn("Failed to fetch validation profiles from service for cache key '{}', returning cached profiles", cacheKey, e);
            cache.put(cacheKey, cached.withFailureAt(now));
            return cached.profiles();
         }
         throw e;
      }
   }

   private ProfileFetchCapable.ProfileFetchResponse tryFetchProfiles(
         ValidationService validationService,
         CachedProfiles cached) {
      String ifNoneMatch = cached != null ? cached.etag() : null;
      if (validationService instanceof ProfileFetchCapable capable) {
         try {
            return capable.fetchProfiles(ifNoneMatch);
         } catch (UnsupportedOperationException ignored) {
            return null;
         }
      }
      return null;
   }

   /**
    * Keep entries alive long enough to serve stale data during the failure cooldown
    * even after the normal freshness TTL has elapsed.
    */
   private static Duration computeCacheEntryRetention(Duration ttl, Duration failureCooldown) {
      return ttl.plus(failureCooldown);
   }

   private record CachedProfiles(String etag, List<ValidationProfile> profiles, Instant fetchedAt, Instant lastFailureAt) {
      private CachedProfiles touch(Instant now) {
         return new CachedProfiles(etag, profiles, now, lastFailureAt);
      }

      private CachedProfiles withFailureAt(Instant now) {
         return new CachedProfiles(etag, profiles, fetchedAt, now);
      }

      private CachedProfiles clearFailure() {
         if (lastFailureAt == null) {
            return this;
         }
         return new CachedProfiles(etag, profiles, fetchedAt, null);
      }

      private boolean shouldSkipFetch(Instant now, Duration cooldown) {
         if (lastFailureAt == null || cooldown.isZero() || cooldown.isNegative()) {
            return false;
         }
         return lastFailureAt.plus(cooldown).isAfter(now);
      }
   }
}
