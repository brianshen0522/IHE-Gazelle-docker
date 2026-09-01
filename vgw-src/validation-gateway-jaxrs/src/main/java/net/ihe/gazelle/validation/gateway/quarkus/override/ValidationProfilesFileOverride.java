package net.ihe.gazelle.validation.gateway.quarkus.override;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ihe.gazelle.validation.gateway.technical.override.ValidationProfilesOverride;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class ValidationProfilesFileOverride implements ValidationProfilesOverride {

   private final ObjectMapper objectMapper;
   private final Path overrideIndexPath;
   private final AtomicReference<Map<String, String>> overrideIndex = new AtomicReference<>();

   @Inject
   public ValidationProfilesFileOverride(ObjectMapper objectMapper,
         @ConfigProperty(name = "validation.override.index.path",
               defaultValue = "/opt/validation-gateway/overrides-index.json") String overrideIndexPath) {
      this(objectMapper, Path.of(overrideIndexPath));
   }

   ValidationProfilesFileOverride(ObjectMapper objectMapper, Path overrideIndexPath) {
      this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
      this.overrideIndexPath = Objects.requireNonNull(overrideIndexPath, "overrideIndexPath must not be null");
   }

   @Override
   public Optional<List<ValidationProfile>> resolve(String serviceName) {
      Optional<String> serviceNameKey = serviceNameKey(serviceName);
      if (serviceNameKey.isEmpty()) {
         return Optional.empty();
      }
      Optional<String> configuredPath = resolvePath(serviceNameKey.get());
      if (configuredPath.isEmpty()) {
         return Optional.empty();
      }
      Path path = Path.of(configuredPath.get());
      if (!Files.isRegularFile(path)) {
         throw new IllegalStateException("Validation profiles override configured for service '" + serviceNameKey.get()
               + "' but file does not exist: " + path);
      }
      try {
         List<ValidationProfile> profiles = objectMapper.readValue(
               Files.readAllBytes(path),
               new TypeReference<>() { }
         );
         return Optional.of(profiles == null ? List.of() : profiles);
      } catch (IOException exception) {
         throw new IllegalStateException("Failed to read validation profiles override for service '" + serviceNameKey.get()
               + "' from: " + path, exception);
      }
   }

   private static Optional<String> serviceNameKey(String serviceName) {
      if (serviceName == null || serviceName.isBlank()) {
         return Optional.empty();
      }
      return Optional.of(serviceName);
   }

   private Map<String, String> overrideIndex() {
      Map<String, String> current = overrideIndex.get();
      if (current != null) {
         return current;
      }
      synchronized (this) {
         current = overrideIndex.get();
         if (current == null) {
            current = loadIndex(overrideIndexPath);
            overrideIndex.set(current);
         }
      }
      return current;
   }

   private Optional<String> resolvePath(String serviceNameKey) {
      return Optional.ofNullable(overrideIndex().get(serviceNameKey))
            .map(String::trim)
            .filter(value -> !value.isEmpty());
   }

   private Map<String, String> loadIndex(Path path) {
      if (!Files.exists(path)) {
         return Map.of();
      }
      if (!Files.isRegularFile(path)) {
         throw new IllegalStateException("Validation profiles override index path is not a regular file: " + path);
      }
      try {
         Map<String, String> index = objectMapper.readValue(Files.readAllBytes(path), new TypeReference<>() { });
         if (index == null || index.isEmpty()) {
            return Map.of();
         }
         java.util.Map<String, String> configured = new java.util.HashMap<>();
         for (Map.Entry<String, String> entry : index.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
               continue;
            }
            String key = entry.getKey();
            String cleanedPath = entry.getValue().trim();
            if (!key.isBlank() && !cleanedPath.isEmpty()) {
               configured.put(key, cleanedPath);
            }
         }
         return java.util.Collections.unmodifiableMap(configured);
      } catch (IOException exception) {
         throw new IllegalStateException("Failed to read validation profiles override index file: " + path, exception);
      }
   }
}
