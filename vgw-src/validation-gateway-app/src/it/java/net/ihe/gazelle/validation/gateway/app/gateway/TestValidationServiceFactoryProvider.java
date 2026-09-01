package net.ihe.gazelle.validation.gateway.app.gateway;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import net.ihe.gazelle.modelbased.client.ModelBasedValidationServiceClientFactory;
import net.ihe.gazelle.validation.v2.client.ValidationServiceFactoryProvider;
import net.ihe.gazelle.validation.v2.client.ValidationServiceClientFactory;
import net.ihe.gazelle.validation.v2.client.ValidationServiceApiClientFactory;

import java.util.List;

@ApplicationScoped
@Alternative
@Priority(1)
public class TestValidationServiceFactoryProvider implements ValidationServiceFactoryProvider {

   @Override
   public List<ValidationServiceClientFactory> getFactories() {
      return List.of(
            new ValidationServiceApiClientFactory(),
            new ModelBasedValidationServiceClientFactory()
      );
   }
}
