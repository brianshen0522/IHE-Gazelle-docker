package net.ihe.gazelle.xmlvalidation.mock;

import net.ihe.gazelle.xmlvalidation.business.config.ValidationServiceConfiguration;

public class ValidationServiceConfigurationMock implements ValidationServiceConfiguration {
    @Override
    public String getSchematronEngineVersion() {
        return "mock-version";
    }

    @Override
    public String getSchematronEngineName() {
        return "mock-name-of-schematron";
    }
}
