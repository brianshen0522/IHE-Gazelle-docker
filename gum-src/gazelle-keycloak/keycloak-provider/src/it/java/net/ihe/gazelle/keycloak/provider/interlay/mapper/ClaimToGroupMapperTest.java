package net.ihe.gazelle.keycloak.provider.interlay.mapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.protocol.cas.mappers.FullNameMapper;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClaimToGroupMapperTest {

    public static final String ORGANIZATION_ID_CLAIM = "organizationId";
    public static final String ORGANIZATION_NAME_CLAIM = "organizationName";

    static ClaimToGroupMapper claimToGroupMapper;

    @BeforeAll
    static void init() {
        claimToGroupMapper = new ClaimToGroupMapper();
    }
    @Test
    void shouldTokenMapperDisplayCategory() {
        final String tokenMapperDisplayCategory = new FullNameMapper().getDisplayCategory();
        assertEquals("Map the claim containing the organization id and the organization name from the token to a group", claimToGroupMapper.getHelpText());
    }

    @Test
    void shouldHaveDisplayType() {
        assertFalse(new ClaimToGroupMapper().getDisplayType().isBlank());
    }

    @Test
    void shouldHaveHelpText() {
        assertFalse(new ClaimToGroupMapper().getHelpText().isBlank());
    }

    @Test
    void shouldHaveIdId() {
        assertFalse(new ClaimToGroupMapper().getId().isBlank());
    }

    @Test
    void shouldHaveProperties() {
        final List<String> configPropertyNames = new ClaimToGroupMapper().getConfigProperties().stream()
                .map(ProviderConfigProperty::getName)
                .toList();
        assertTrue(configPropertyNames.contains(ORGANIZATION_ID_CLAIM));
        assertTrue(configPropertyNames.contains(ORGANIZATION_NAME_CLAIM));
    }

}