package net.ihe.gazelle.keycloak.provider.interlay.mapper;

import net.ihe.gazelle.keycloak.core.application.error.ErrorServiceGUI;
import net.ihe.gazelle.keycloak.core.interlay.KeycloakUtils;
import net.ihe.gazelle.keycloak.core.interlay.error.ErrorServiceGUIImpl;
import net.ihe.gazelle.keycloak.provider.interlay.factory.DelegatedOrganizationServiceFactory;
import net.ihe.gazelle.user.management.api.application.ConflictException;
import net.ihe.gazelle.user.management.api.application.organization.DelegatedOrganizationService;
import net.ihe.gazelle.user.management.api.domain.organization.DelegatedOrganization;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;
import org.keycloak.broker.oidc.KeycloakOIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.OIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.mappers.AbstractClaimMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

public class ClaimToGroupMapper extends AbstractClaimMapper {
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();
    protected final ErrorServiceGUI errorService = new ErrorServiceGUIImpl();
    private final Logger log = LoggerFactory.getLogger(ClaimToGroupMapper.class);

    private static final String DELEGATED_ORGANIZATION_CONFLICT = "delegatedOrganizationConflict";
    private static final String DELEGATED_ORGANIZATION_UPDATE_FAIL = "delegatedOrganizationUpdateFail";
    public static final String PROVIDER_ID = "gazelle-group-idp-mapper";
    public static final String ORGANIZATION_ID_CLAIM = "organizationId";
    public static final String ORGANIZATION_NAME_CLAIM = "organizationName";
    protected static final String[] COMPATIBLE_PROVIDERS = {KeycloakOIDCIdentityProviderFactory.PROVIDER_ID, OIDCIdentityProviderFactory.PROVIDER_ID};


    //Properties shown in Keycloak GUI
    static {

        ProviderConfigProperty propertyId = new ProviderConfigProperty();
        propertyId.setName(ORGANIZATION_ID_CLAIM);
        propertyId.setLabel("Organization Id");
        propertyId.setDefaultValue(ORGANIZATION_ID_CLAIM);
        propertyId.setType(ProviderConfigProperty.STRING_TYPE);
        propertyId.setHelpText("Name of claim to search organisation id in token.");

        ProviderConfigProperty propertyName = new ProviderConfigProperty();
        propertyName.setName(ORGANIZATION_NAME_CLAIM);
        propertyName.setLabel("Organization Name");
        propertyName.setDefaultValue(ORGANIZATION_NAME_CLAIM);
        propertyName.setType(ProviderConfigProperty.STRING_TYPE);
        propertyName.setHelpText("Name of claim to search organisation name in token.");

        configProperties.add(propertyId);
        configProperties.add(propertyName);
    }

    private DelegatedOrganizationService service;

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getDisplayCategory() {
        return "Group importer";
    }

    @Override
    public String getDisplayType() {
        return "Claim to group (Gazelle)";
    }

    @Override
    public String getHelpText() {
        return "Map the claim containing the organization id and the organization name from the token to a group";
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
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        log.trace("updateBrokeredUser");

        String externalId = getExternalId(context, mapperModel);
        String idpId = getIssuer(context);
        String organizationName = getOrganizationName(context, mapperModel);
        String keyword = transformNameToKeyword(organizationName);

        DelegatedOrganizationServiceFactory delegatedOrganizationServiceFactory = new DelegatedOrganizationServiceFactory(session);
        service = delegatedOrganizationServiceFactory.getOrganizationDelegationService();
        DelegatedOrganization delegatedOrganization = new DelegatedOrganization(keyword, keyword, organizationName, externalId, idpId);

        try {
            service.upsertDelegatedOrganizationForUser(delegatedOrganization, user.getUsername(), localOrganizationMatcher());
        } catch (GazelleDAOException e) {
            String logMessage = String.format("Organization update fail with message : %s", e.getMessage());
            throw errorService.generateIdentityProviderCustomErrorPage(logMessage, this.getClass(), DELEGATED_ORGANIZATION_UPDATE_FAIL);
        } catch (ConflictException e) {
            String logMessage = String.format("Organization conflict : the generated keyword (%s) already exists in TM", keyword);
            throw errorService.generateIdentityProviderCustomErrorPage(logMessage, this.getClass(), DELEGATED_ORGANIZATION_CONFLICT);
        }
    }

    protected Function<DelegatedOrganization, Organization> localOrganizationMatcher() {
        return service.getDefaultLocalOrganizationMatcher();
    }

    @Override
    public boolean supportsSyncMode(IdentityProviderSyncMode syncMode) {
        Set<IdentityProviderSyncMode> identityProviderMapperSyncNodes = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.LEGACY, IdentityProviderSyncMode.IMPORT, IdentityProviderSyncMode.FORCE));
        return identityProviderMapperSyncNodes.contains(syncMode);
    }

    protected String getOrganizationName(BrokeredIdentityContext context, IdentityProviderMapperModel mapperModel) {
        return (String) getClaimValue(context, mapperModel.getConfig().get(ORGANIZATION_NAME_CLAIM).trim());
    }

    protected String getExternalId(BrokeredIdentityContext context, IdentityProviderMapperModel mapperModel) {
        return (String) getClaimValue(context, mapperModel.getConfig().get(ORGANIZATION_ID_CLAIM));
    }

    protected String transformNameToKeyword(String name) {
        // Remove white space.
        String keyword = name.replace(" ", "_");
        // Truncate if name > 32.
        if (keyword.length() > 32) keyword = keyword.substring(0, 32);
        // if the keyword ends with '_', remove the last character.
        if (keyword.endsWith("_")) keyword = keyword.replaceAll(".$", "");
        return keyword;
    }

    protected String getIssuer(BrokeredIdentityContext brokerContext) {
        JsonWebToken idpId = KeycloakUtils.getJsonWebTokenFromBrokerContext(brokerContext);
        return idpId != null ? idpId.getIssuer() : null;
    }
}