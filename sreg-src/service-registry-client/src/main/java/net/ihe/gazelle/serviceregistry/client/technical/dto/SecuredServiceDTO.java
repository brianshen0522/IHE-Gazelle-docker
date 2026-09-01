package net.ihe.gazelle.serviceregistry.client.technical.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import net.ihe.gazelle.oidc.common.technical.dto.SecuredMessageDTO;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.servicemetadata.technical.jaxrs.ServiceDTO;

/**
 * Data Transfer Object (DTO) for representing a secured service in the service registry client.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecuredServiceDTO extends ServiceDTO<Service> implements SecuredMessageDTO {

   @JsonIgnore
   private String authorization;

   /**
    * Default constructor
    */
   public SecuredServiceDTO() {
      this(new Service(), null);
   }

   /**
    * Constructor with service and authorization parameters.
    * @param service the service to be represented
    * @param authorization the authorization information associated with the service
    */
   public SecuredServiceDTO(Service service, String authorization) {
      super(service);
      this.authorization = authorization;
   }

   @Override
   public String getAuthorization() {
      return authorization;
   }

   /**
    * Sets the authorization information for this service DTO.
    * @param authorization the authorization information to set
    * @return this SecuredServiceDTO instance for method chaining
    */
   public SecuredServiceDTO setAuthorization(String authorization) {
      this.authorization = authorization;
      return this;
   }

}
