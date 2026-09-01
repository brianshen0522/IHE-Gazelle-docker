package net.ihe.gazelle.user.management.quarkus.interlay.metadata;

import jakarta.inject.Inject;
import net.ihe.gazelle.servicemetadata.api.business.MetadataService;
import net.ihe.gazelle.servicemetadata.technical.jaxrs.MetadataRestControllerImpl;

/**
 * REST controller exposing the service metadata endpoints for GUM.
 */
public class GUMServiceMetadataRestController extends MetadataRestControllerImpl {

    /**
     * Creates the controller with the provided metadata service.
     *
     * @param metadataService metadata service implementation
     */
    @Inject
    public GUMServiceMetadataRestController(MetadataService metadataService) {
        super(metadataService);
    }

}
