package net.ihe.gazelle.serviceregistry.technical.security;

import net.ihe.gazelle.security.business.rbac.RBACPropertiesProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Implementation of {@link RBACPropertiesProvider} that loads RBAC properties from a file named "rbac.properties" located in the classpath.
 */
public class RBACPropertiesProviderImpl implements RBACPropertiesProvider {

    /**
     * Default constructor for RBACPropertiesProviderImpl.
     */
    public RBACPropertiesProviderImpl() {
        // Nothing to initialize
    }

    @Override
    public Properties getRBACProperties() {
        Properties rbacProperties = new Properties();
        try (InputStream resourceAsStream = this.getClass().getResourceAsStream("/rbac.properties")){
            rbacProperties.load(resourceAsStream);
            return rbacProperties;
        } catch (IOException e) {
            throw new IllegalStateException("Error while retrieving RBAC properties",e);
        }
    }

}
