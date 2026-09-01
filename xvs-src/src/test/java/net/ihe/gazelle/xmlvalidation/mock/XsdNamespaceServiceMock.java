package net.ihe.gazelle.xmlvalidation.mock;

import net.ihe.gazelle.xmlvalidation.technical.service.XsdNamespaceService;

public class XsdNamespaceServiceMock extends XsdNamespaceService {

    @Override
    public String getTargetNamespace(net.ihe.gazelle.xmlvalidation.business.config.ProfileConfiguration profileConfiguration) {
        return "UNKNOWN";
    }
}
