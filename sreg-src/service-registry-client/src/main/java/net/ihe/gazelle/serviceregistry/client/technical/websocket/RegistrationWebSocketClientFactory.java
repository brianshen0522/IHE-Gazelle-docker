package net.ihe.gazelle.serviceregistry.client.technical.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.ihe.gazelle.oidc.common.business.accesstoken.AccessTokenService;
import net.ihe.gazelle.serviceregistry.client.business.ServiceRegistrationClient;
import net.ihe.gazelle.serviceregistry.client.technical.job.RegistrationJobConfig;

/**
 * Factory class for creating instances of ServiceRegistrationClient.
 */
@ApplicationScoped
public class RegistrationWebSocketClientFactory {

   private final RegistrationJobConfig registrationJobConfig;
   private final AccessTokenService accessTokenService;

   /**
    * Constructor for RegistrationWebSocketClientFactory with dependencies injected.
    * @param registrationJobConfig the configuration for the registration job, providing necessary settings such as the service registry URL
    * @param accessTokenService the service for retrieving access tokens, used for authenticating with the service registry when creating WebSocket clients
    */
   @Inject
   public RegistrationWebSocketClientFactory(RegistrationJobConfig registrationJobConfig, AccessTokenService accessTokenService) {
      this.registrationJobConfig = registrationJobConfig;
      this.accessTokenService = accessTokenService;
   }

   /**
    * Produces a new instance of ServiceRegistrationClient, specifically a ServiceRegistrationWebSocketClient, configured with the service registry URL and access token service.
    * @return a new ServiceRegistrationClient instance for registering services with the service registry via WebSocket
    */
   @Produces
   @ApplicationScoped
   public ServiceRegistrationClient getServiceRegistrationClient() {
      return new ServiceRegistrationWebSocketClient(
            registrationJobConfig.getServiceRegistryUrl(),
            accessTokenService
      );
   }
}
