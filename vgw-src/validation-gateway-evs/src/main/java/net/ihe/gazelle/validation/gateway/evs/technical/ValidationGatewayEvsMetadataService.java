package net.ihe.gazelle.validation.gateway.evs.technical;

import jakarta.enterprise.context.ApplicationScoped;
import net.ihe.gazelle.servicemetadata.api.technical.AbstractMetadataService;

@ApplicationScoped
@GatewayModule
public class ValidationGatewayEvsMetadataService extends AbstractMetadataService {
    protected ValidationGatewayEvsMetadataService() {
        super(ValidationGatewayEvsMetadataService.class);
    }

    @Override
    public String getServiceName() {
        return "Validation Gateway EVS API";
    }

    @Override
    public String getServiceDescription() {
        return "Backward-compatible EVS API for validation gateway.";
    }
}
