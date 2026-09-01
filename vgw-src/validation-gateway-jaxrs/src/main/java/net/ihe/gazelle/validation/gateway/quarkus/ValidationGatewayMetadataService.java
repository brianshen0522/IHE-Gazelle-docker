package net.ihe.gazelle.validation.gateway.quarkus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import net.ihe.gazelle.servicemetadata.api.technical.AbstractMetadataService;

@ApplicationScoped
@Default
public class ValidationGatewayMetadataService extends AbstractMetadataService {
    protected ValidationGatewayMetadataService() {
        super(ValidationGatewayMetadataService.class);
    }

    @Override
    public String getServiceName() {
        return "Validation Gateway";
    }

    @Override
    public String getServiceDescription() {
        return "A gateway service that routes validation requests to appropriate validation services based on defined criteria.";
    }
}
