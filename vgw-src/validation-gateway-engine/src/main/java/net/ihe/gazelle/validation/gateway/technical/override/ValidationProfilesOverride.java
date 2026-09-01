package net.ihe.gazelle.validation.gateway.technical.override;

import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;

import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface ValidationProfilesOverride {

   Optional<List<ValidationProfile>> resolve(String serviceName);

   static ValidationProfilesOverride none() {
      return serviceName -> Optional.empty();
   }
}
