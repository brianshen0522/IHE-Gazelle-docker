package net.ihe.gazelle.validation.gateway.technical.service.support;

import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.validation.v2.api.business.ValidationService;
import net.ihe.gazelle.validation.v2.client.ValidationServiceClientFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CapturingHandler implements ValidationServiceClientFactory {

   private final String interfaceName;
   private final AtomicReference<ProvidedInterface> requestedInterface;

   public CapturingHandler(String interfaceName) {
      this(interfaceName, null);
   }

   public CapturingHandler(String interfaceName, AtomicReference<ProvidedInterface> requestedInterface) {
      this.interfaceName = interfaceName;
      this.requestedInterface = requestedInterface;
   }

   @Override
   public String getInterfaceName() {
      return interfaceName;
   }

   @Override
   public boolean supports(ProvidedInterface providedInterface) {
      return providedInterface != null && interfaceName.equals(providedInterface.getInterfaceName());
   }

   @Override
   public ValidationService create(ProvidedInterface providedInterface) {
      if (requestedInterface != null) {
         requestedInterface.set(providedInterface);
      }
      return new FakeValidationService(List.of());
   }
}
