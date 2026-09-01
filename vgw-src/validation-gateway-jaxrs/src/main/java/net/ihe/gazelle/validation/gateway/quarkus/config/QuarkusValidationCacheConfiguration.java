package net.ihe.gazelle.validation.gateway.quarkus.config;

import net.ihe.gazelle.validation.gateway.technical.cache.ValidationCacheConfiguration;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class QuarkusValidationCacheConfiguration implements ValidationCacheConfiguration {

   @ConfigProperty(name = "gzl.validation.cache.profiles.max-size", defaultValue = "256")
   long profileCacheMaxSize;

   @ConfigProperty(name = "gzl.validation.cache.profiles.ttl-seconds", defaultValue = "300")
   long profileCacheTtlSeconds;

   @ConfigProperty(name = "gzl.validation.cache.profiles.failure-cooldown-seconds", defaultValue = "60")
   long profileCacheFailureCooldownSeconds;

   @Override
   public long getProfileCacheMaxSize() {
      return profileCacheMaxSize;
   }

   @Override
   public long getProfileCacheTtlSeconds() {
      return profileCacheTtlSeconds;
   }

   @Override
   public long getProfileCacheFailureCooldownSeconds() {
      return profileCacheFailureCooldownSeconds;
   }
}
