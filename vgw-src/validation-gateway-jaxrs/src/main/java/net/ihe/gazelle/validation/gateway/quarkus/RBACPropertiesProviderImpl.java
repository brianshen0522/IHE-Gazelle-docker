package net.ihe.gazelle.validation.gateway.quarkus;

import net.ihe.gazelle.security.business.rbac.RBACPropertiesProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RBACPropertiesProviderImpl implements RBACPropertiesProvider {
    @Override
    public Properties getRBACProperties() {
        Properties rbacProperties = new Properties();
        try (InputStream resourceAsStream = this.getClass().getResourceAsStream("/rbac.properties")){
            if (resourceAsStream == null) {
                throw new IllegalStateException("rbac.properties resource not found");
            }
            rbacProperties.load(resourceAsStream);
            return rbacProperties;
        } catch (IOException e) {
            throw new IllegalStateException("Error while retrieving RBAC properties",e);
        }
    }
}
