package net.ihe.gazelle.keycloak.provider.interlay.mapper;

import jakarta.persistence.EntityManager;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationLookupService;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationLookupDAO;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationLookupServiceImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.organization.OrganizationLookupDAOImpl;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.*;
import org.keycloak.protocol.cas.mappers.AbstractCASProtocolMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrganizationKeywordProtocolMapper extends AbstractCASProtocolMapper {

    private static final String PROVIDER_ID = "organization-keyword-protocol-mapper";
    private static final Logger log = LoggerFactory.getLogger(OrganizationKeywordProtocolMapper.class);
    private static final String ATTR_INSTITUTION_KEYWORD = "institution_keyword";

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return new ArrayList<>();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Organization Keyword";
    }

    @Override
    public String getHelpText() {
        return "To keep CAS authentication we need organization Keyword in institution_keyword token";
    }

    @Override
    public void setAttribute(Map<String, Object> attributes, ProtocolMapperModel mappingModel, UserSessionModel userSession,
                             KeycloakSession session, ClientSessionContext clientSessionCt) {
        OrganizationLookupService organizationLookupService = createOrganizationLookupService(session);
        Optional<GroupModel> groupModel = userSession.getUser().getGroupsStream().findFirst();

        if (groupModel.isPresent()) {
            String organizationId = groupModel.get().getName();
            try {
                String keyword = getKeywordFromOrganizationId(organizationLookupService, organizationId);
                attributes.put(ATTR_INSTITUTION_KEYWORD, keyword);
            } catch (Exception e) {
                // Log the error but do not throw - the authenticator should have already validated this
                String logMessage = String.format("Failed to get organization keyword for id: %s, user: %s",
                        organizationId, userSession.getUser().getId());
                log.error(logMessage, e);
                // Set organization id as default institution_keyword if keyword retrieval fails
                attributes.put(ATTR_INSTITUTION_KEYWORD, organizationId);
            }
        } else {
            log.warn("User {} has no organization assigned but passed authentication", userSession.getUser().getId());
        }
    }

    private String getKeywordFromOrganizationId(OrganizationLookupService organizationLookupService, String organizationId) {
        return organizationLookupService.getOrganizationById(organizationId).getShortname();
    }

    private OrganizationLookupService createOrganizationLookupService(KeycloakSession keycloakSession) {
        EntityManager em = keycloakSession.getProvider(JpaConnectionProvider.class, "gum-store").getEntityManager();
        OrganizationLookupDAO organizationLookupDAO = new OrganizationLookupDAOImpl(em);
        return new OrganizationLookupServiceImpl(organizationLookupDAO);
    }
}