package net.ihe.gazelle.keycloak.provider.interlay.mapper;

import jakarta.persistence.EntityManager;
import net.ihe.gazelle.keycloak.core.interlay.identity.KeycloakIdentity;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.AuthzImpl;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupServiceImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserLookupDAOImpl;
import org.keycloak.broker.oidc.KeycloakOIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.OIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.mappers.AbstractClaimToRoleMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.*;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ProviderConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ClaimToRoleMapper extends AbstractClaimToRoleMapper {

    private final Logger log = LoggerFactory.getLogger(ClaimToRoleMapper.class);
    public static final String CLAIM_KEY_PROPERTY_NAME = "claimKey";
    public static final String CLAIM_VALUE_PROPERTY_NAME = "claimValue";
    public static final String TARGET_ROLE_PROPERTY_NAME = "role";
    public static final String ORGANIZATION_NAME_CLAIM = "organizationName";
    private static final String[] COMPATIBLE_PROVIDERS = {KeycloakOIDCIdentityProviderFactory.PROVIDER_ID, OIDCIdentityProviderFactory.PROVIDER_ID};
    private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();
    private final GazelleIdentity keycloakIdentity = new KeycloakIdentity();
    private UserLookupService userLookupService;

    static {
        ProviderConfigProperty claimKeyProperty = new ProviderConfigProperty();
        claimKeyProperty.setName(CLAIM_KEY_PROPERTY_NAME);
        claimKeyProperty.setLabel("Claim key for roles");
        claimKeyProperty.setHelpText("Key of the claim to extract roles (example : profile.role).");
        claimKeyProperty.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(claimKeyProperty);

        ProviderConfigProperty claimValueProperty = new ProviderConfigProperty();
        claimValueProperty.setName(CLAIM_VALUE_PROPERTY_NAME);
        claimValueProperty.setLabel("Claim value");
        claimValueProperty.setHelpText("Value of the claim to match (example: Director).");
        claimValueProperty.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(claimValueProperty);

        ProviderConfigProperty roleProperty = new ProviderConfigProperty();
        roleProperty.setName(TARGET_ROLE_PROPERTY_NAME);
        roleProperty.setLabel("Target Role");
        roleProperty.setHelpText("Target role to assign if the claim matches. (example : role:gazelle_admin, org:{orgaId})");
        roleProperty.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(roleProperty);

        ProviderConfigProperty orgaProperty = new ProviderConfigProperty();
        orgaProperty.setName(ORGANIZATION_NAME_CLAIM);
        orgaProperty.setLabel("Claim key for organization name");
        orgaProperty.setDefaultValue(ORGANIZATION_NAME_CLAIM);
        orgaProperty.setType(ProviderConfigProperty.STRING_TYPE);
        orgaProperty.setHelpText("Name of claim to search organisation name in token (example: organization.name).");
        configProperties.add(orgaProperty);
    }

    public static final String PROVIDER_ID = "gazelle-role-idp-mapper";

    @Override
    public boolean supportsSyncMode(IdentityProviderSyncMode syncMode) {
        return IDENTITY_PROVIDER_SYNC_MODES.contains(syncMode);
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
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getDisplayCategory() {
        return "Gazelle role importer";
    }

    @Override
    public String getDisplayType() {
        return "Gazelle mapper for roles";
    }

    @Override
    public String getHelpText() {
        return "Map an expected claim value to a Gazelle role (example : Director -> org-adm:{orgaId} ).";
    }

    @Override
    protected boolean applies(IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        String claimKey = mapperModel.getConfig().get(CLAIM_KEY_PROPERTY_NAME);
        String claimValueExpected = mapperModel.getConfig().get(CLAIM_VALUE_PROPERTY_NAME);
        Object claimValue = getClaimValue(context, claimKey);

        return claimValue != null && claimValueExpected != null && claimValueExpected.equals(claimValue.toString());
    }


    /**
     * Initialize the UserLookupService if it is not already initialized.
     * @param keycloakSession the keycloak session
     */
    private UserLookupService createUserLookupService(KeycloakSession keycloakSession) {
            EntityManager em = keycloakSession.getProvider(JpaConnectionProvider.class, "gum-store").getEntityManager();
            UserLookupDAO userLookupDAO = new UserLookupDAOImpl(em);
            Authz authz = new AuthzImpl(new PermissionStoreSPIProvider());
            return new UserLookupServiceImpl(userLookupDAO,authz);
    }

    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        this.userLookupService = createUserLookupService(session);
        updateDynamicOrgaMembership(mapperModel, context);
        createRoleIfNotExists(session, realm, mapperModel);
        super.importNewUser(session, realm, user, mapperModel, context);
    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        this.userLookupService = createUserLookupService(session);
        updateDynamicOrgaMembership(mapperModel, context);
        createRoleIfNotExists(session, realm, mapperModel);
        super.updateBrokeredUser(session, realm, user, mapperModel, context);
    }

    @Override
    public void updateBrokeredUserLegacy(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        this.userLookupService = createUserLookupService(session);
        updateDynamicOrgaMembership(mapperModel, context);
        createRoleIfNotExists(session, realm, mapperModel);
        super.updateBrokeredUserLegacy(session, realm, user, mapperModel, context);
    }

    /**
     * Create the role in Keycloak if it does not exist.
     * This is useful for dynamic roles that are created based on the organization id.
     * @param realm the Keycloak realm
     * @param mapperModel the IdentityProviderMapperModel containing the role information
     */
    private void createRoleIfNotExists(KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel) {
        String roleName = mapperModel.getConfig().get(TARGET_ROLE_PROPERTY_NAME);
        RoleModel role = KeycloakModelUtils.getRoleFromString(session, realm,roleName);
        if (role == null) {
            log.info("Creating role: {}", roleName);
            realm.addRole(roleName);
        }
    }

    /**
     * Update the role name in the mapper model if it contains the {orgaId} placeholder.
     * @param mapperModel the IdentityProviderMapperModel containing the role information
     * @param context the BrokeredIdentityContext containing the user information
     */
    private void updateDynamicOrgaMembership(IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        String roleName = mapperModel.getConfig().get(TARGET_ROLE_PROPERTY_NAME);
        if (roleName.contains("{orgaId}")) {
            try {
                String organizationId = userLookupService.getUserByEmail(context.getEmail(), keycloakIdentity).getOrganizationId();
                if (organizationId == null) {
                    String organizationName = getOrganizationName(context, mapperModel);
                    organizationId = transformNameToKeyword(organizationName);
                }

                // Replace the {orgaId} placeholder with the organization id of the user
                roleName = roleName.replace("{orgaId}", organizationId);
                mapperModel.getConfig().put(TARGET_ROLE_PROPERTY_NAME, roleName);
            } catch (NoSuchElementException _) {
                log.warn("No organization found for user with email: {}", context.getEmail());
            }
        }
    }


    /**
     * Transform the name to a keyword suitable for use in Keycloak roles.
     * /!\ duplicated from ClaimToGroupMapper.java
     * @param name the name to transform
     * @return the transformed keyword
     */
    protected String transformNameToKeyword(String name) {
        // Remove white space.
        String keyword = name.replace(" ", "_");
        // Truncate if name > 32.
        if (keyword.length() > 32) keyword = keyword.substring(0, 32);
        // if the keyword ends with '_', remove the last character.
        if (keyword.endsWith("_")) keyword = keyword.replaceAll(".$", "");
        return keyword;
    }

    /**
     * Get the organization name from the claim in the context.
     * /!\ duplicated from ClaimToGroupMapper.java
     * @param context the BrokeredIdentityContext containing the user information
     * @param mapperModel the IdentityProviderMapperModel containing the organization name claim configuration
     * @return the organization name from the claim
     */
    protected String getOrganizationName(BrokeredIdentityContext context, IdentityProviderMapperModel mapperModel) {
        return (String) getClaimValue(context, mapperModel.getConfig().get(ORGANIZATION_NAME_CLAIM).trim());
    }
}
