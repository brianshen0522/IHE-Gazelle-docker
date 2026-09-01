package net.ihe.gazelle.xmlvalidation.mock;

import net.ihe.gazelle.servicemetadata.api.business.MetadataService;
import net.ihe.gazelle.servicemetadata.api.business.Service;

/**
 * Lightweight test metadata service that avoids loading MicroProfile config during unit tests.
 */
public class XMLMetadataServiceMock implements MetadataService {

    private final Service service = new Service()
            .setName("xml-validation-service-mock")
            .setVersion("unknown")
            .setDescription("Mock XML Validation Service for Testing Purposes");

    @Override
    public Service getMetadata() {
        return service;
    }
}
