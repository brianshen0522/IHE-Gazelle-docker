package net.ihe.gazelle.keycloak.provider.interlay.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.keycloak.broker.provider.ConfigConstants;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ClaimToRoleDependingOfOrganizationMapper extends AbstractClaimToRoleMapper {

    private final Logger log = LoggerFactory.getLogger(ClaimToRoleDependingOfOrganizationMapper.class);
    public static final String CLAIM_KEY_PROPERTY_NAME = "claimKey";
    public static final String CLAIM_VALUE_PROPERTY_NAME = "claimValue";
    private static final String[] COMPATIBLE_PROVIDERS = {KeycloakOIDCIdentityProviderFactory.PROVIDER_ID, OIDCIdentityProviderFactory.PROVIDER_ID};
    private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();
    private final GazelleIdentity keycloakIdentity = new KeycloakIdentity();
    private UserLookupService userLookupService;

    static {
        ProviderConfigProperty claimKeyProperty = new ProviderConfigProperty();
        claimKeyProperty.setName(CLAIM_KEY_PROPERTY_NAME);
        claimKeyProperty.setLabel("Claim Key");
        claimKeyProperty.setHelpText("Key of the claim to match");
        claimKeyProperty.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(claimKeyProperty);

        ProviderConfigProperty claimValueProperty = new ProviderConfigProperty();
        claimValueProperty.setName(CLAIM_VALUE_PROPERTY_NAME);
        claimValueProperty.setLabel("Claim Value");
        claimValueProperty.setHelpText("Value of the claim to match");
        claimValueProperty.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(claimValueProperty);

        ProviderConfigProperty roleProperty = new ProviderConfigProperty();
        roleProperty.setName(ConfigConstants.ROLE);
        roleProperty.setLabel("Role");
        roleProperty.setHelpText("Role to grant to user if claim is present. Click 'Select Role' button to browse roles, or just type it in the textbox. To reference a client group the syntax is clientname.clientrole, i.e. myclient.myrole");
        roleProperty.setType(ProviderConfigProperty.ROLE_TYPE);
        configProperties.add(roleProperty);
    }

    public static final String PROVIDER_ID = "gazelle-advanced-group-orga-idp-mapper";

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        this.userLookupService = createUserLookupService(session);
        super.updateBrokeredUser(session, realm, user, mapperModel, context);
    }

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
        return "Role Importer depending of organization";
    }

    @Override
    public String getDisplayType() {
        return "Advanced Claim to Role Mapper depending of organization";
    }

    @Override
    public String getHelpText() {
        return "If at least one claim exists and if the corresponding organization match the current organization of the user, grant the user the specified realm or client group.";
    }

    @Override
    protected boolean applies(IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        String claimKey = mapperModel.getConfig().get(CLAIM_KEY_PROPERTY_NAME);
        String claimValueExpected = mapperModel.getConfig().get(CLAIM_VALUE_PROPERTY_NAME);
        Object claimValue = getClaimValue(context, claimKey);
        List<HashMap<String,Object>> listOfRoles = parseStringIntoList(claimValue);

        for (HashMap<String,Object> hashMap : listOfRoles) {
            String value = (String) hashMap.get("name");
            if (claimValueExpected.equals(value)) {
                List organizations = (List) hashMap.get("organizations");
                // If there is no organization requirement, we grant the group
                if (organizations == null || organizations.isEmpty())
                    return true;

                try {
                    // Get the current organization of the user
                    String organizationId = userLookupService.getUserByEmail(context.getEmail(), keycloakIdentity).getOrganizationId();
                    if (organizations.contains(organizationId))
                        return true;
                } catch (NoSuchElementException _) {
                    // Do nothing
                }
            }
        }
        return false;
    }

    /**
     * Transform the string of type [{ "name":"value1","organization": ["1","2"]},{ "name": "value1"}] into a list.
     * Thanks to jackson databind, we can use the ObjectMapper to parse the string into a map.
     * @param claimValue the string to parse
     */
    private List<HashMap<String,Object>> parseStringIntoList(Object claimValue) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue((String) claimValue, List.class);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return List.of();
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

}
