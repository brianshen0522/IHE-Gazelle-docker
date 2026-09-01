package net.ihe.gazelle.keycloak.provider.interlay.mapper;

import jakarta.persistence.EntityManager;
import net.ihe.gazelle.keycloak.core.application.error.ErrorServiceGUI;
import net.ihe.gazelle.keycloak.core.interlay.error.ErrorServiceGUIImpl;
import net.ihe.gazelle.keycloak.core.interlay.identity.KeycloakIdentity;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.AuthzImpl;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupServiceImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserLookupDAOImpl;
import org.keycloak.broker.oidc.KeycloakOIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.OIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.mappers.AbstractClaimMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ClaimToEmailMapper extends AbstractClaimMapper {
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();
    private final ErrorServiceGUI errorService = new ErrorServiceGUIImpl();
    private final GazelleIdentity keycloakIdentity = new KeycloakIdentity();
    private static final String EMAIL_CONFLICT = "emailConflict";
    public static final String PROVIDER_ID = "gazelle-email-idp-mapper";
    protected static final String[] COMPATIBLE_PROVIDERS = {KeycloakOIDCIdentityProviderFactory.PROVIDER_ID, OIDCIdentityProviderFactory.PROVIDER_ID};
    Logger log = LoggerFactory.getLogger(ClaimToEmailMapper.class);

    //Properties shown in Keycloak GUI
    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(CLAIM);
        property.setLabel("Claim");
        property.setHelpText("Name of claim to search for in token. You can reference nested claims using a '.'," +
                " i.e. 'address.locality'. To use dot (.) literally, escape it with backslash (\\.)");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);
    }

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getDisplayCategory() {
        return "Email conflict handler";
    }

    @Override
    public String getDisplayType() {
        return "Claim to Email (Gazelle)";
    }

    @Override
    public String getHelpText() {
        return "Throw an error page when a conflict of email is detected";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        log.debug("Import new user {} {}", user.getFirstName(), user.getLastName());
        updateBrokeredUser(session, realm, user, mapperModel, context);
    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel userModel, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        log.debug("Updating user {}", userModel.getFirstName());
        String emailClaimName = mapperModel.getConfig().get(CLAIM);
        String email = (String) getClaimValue(context, emailClaimName);
        verifyDuplicateEmail(session, userModel, email);
        updateEmailIfNeeded(session, userModel, email);
    }

    @Override
    public boolean supportsSyncMode(IdentityProviderSyncMode syncMode) {
        Set<IdentityProviderSyncMode> identityProviderSyncModes = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.LEGACY, IdentityProviderSyncMode.IMPORT, IdentityProviderSyncMode.FORCE));
        return identityProviderSyncModes.contains(syncMode);
    }

    private void verifyDuplicateEmail(KeycloakSession session, UserModel userModel, String email) {
        try {
            User user = getUserLookupService(session).getUserByEmail(email, keycloakIdentity);
            if (!Objects.equals(user.getId(), userModel.getUsername())) {
                String logMessage = String.format("Email %s is already used", email);
                throw errorService.generateIdentityProviderCustomErrorPage(logMessage, this.getClass(), EMAIL_CONFLICT);
            }
        } catch (NoSuchElementException _) {
            // Do nothing
        }
    }

    private void updateEmailIfNeeded(KeycloakSession session, UserModel userModel, String email) {
        try {
            User user = getUserLookupService(session).getUserById(userModel.getUsername(), keycloakIdentity);
            if (!Objects.equals(user.getEmail(), email)) {
                userModel.setEmail(email);
            }
        } catch (NoSuchElementException _) {
            // Do nothing
        }
    }

    private UserLookupService getUserLookupService(KeycloakSession keycloakSession) {
        EntityManager em = keycloakSession.getProvider(JpaConnectionProvider.class, "gum-store").getEntityManager();
        UserLookupDAO userLookupDAO = new UserLookupDAOImpl(em);
        Authz authz = new AuthzImpl(new PermissionStoreSPIProvider());
        return new UserLookupServiceImpl(userLookupDAO, authz);
    }
}
