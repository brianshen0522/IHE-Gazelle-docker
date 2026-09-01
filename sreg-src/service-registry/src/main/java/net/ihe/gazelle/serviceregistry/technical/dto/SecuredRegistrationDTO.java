package net.ihe.gazelle.serviceregistry.technical.dto;

import net.ihe.gazelle.oidc.common.technical.dto.SecuredMessageDTO;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.servicemetadata.technical.jaxrs.ServiceDTO;

/**
 * DTO for secured registration of a service. It extends ServiceDTO to include the service information and implements
 * @param <T> the type of service being registered, which must extend the Service interface. It also includes an authorization field
 */
public class SecuredRegistrationDTO<T extends Service> extends ServiceDTO<T> implements SecuredMessageDTO {
    private final String authorization;

    /**
     * Default constructor for Jackson deserialization.
     */
    public SecuredRegistrationDTO() {
        this.authorization = null;
    }

    /**
     * Constructor to create a SecuredRegistrationDTO with the given service and authorization token.
     * @param service the service being registered
     * @param authorization the authorization token required for registration
     */
    public SecuredRegistrationDTO(T service, String authorization) {
        super(service);
        this.authorization = authorization;
    }

    @Override
    public String getAuthorization() {
        return authorization;
    }
}
