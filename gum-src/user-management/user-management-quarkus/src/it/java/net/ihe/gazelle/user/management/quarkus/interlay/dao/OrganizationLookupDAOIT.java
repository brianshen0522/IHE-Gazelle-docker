package net.ihe.gazelle.user.management.quarkus.interlay.dao;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.Sort;
import net.ihe.gazelle.user.management.api.domain.organization.DelegatedOrganization;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationLookupDAO;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationSearchCriteria;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.DelegatedOrganizationEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.OrganizationEntity;
import net.ihe.gazelle.user.management.commons.interlay.utils.MockedGazelleIdentity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActivateRequestContext
@Transactional
class OrganizationLookupDAOIT {

    private static final String PREFIX = "OrganizationLookupDAOIT";

    // IDs for the test organizations
    private static final String ORG_A_ID         = PREFIX + "_OrgA";
    private static final String ORG_B_ID         = PREFIX + "_OrgB";
    private static final String DELEGATED_ORG_ID = PREFIX + "_DelegatedOrg";

    private static final MockedGazelleIdentity ADMIN_IDENTITY =
            new MockedGazelleIdentity(Set.of("admin"));

    @Inject
    OrganizationLookupDAO organizationLookupDAO;

    @Inject
    EntityManager entityManager;

    // -------------------------------------------------------------------------
    // Test data setup
    // -------------------------------------------------------------------------

    @BeforeAll
    void setup() {
        entityManager.persist(new OrganizationEntity(
                ORG_B_ID,
                PREFIX + " Beta Organization",
                "Beta Inc"
        ));
        entityManager.persist(new OrganizationEntity(
                ORG_A_ID,
                PREFIX + " Alpha Organization",
                "Alpha Corp"

        ));
        entityManager.persist(new DelegatedOrganizationEntity(
                new Organization(DELEGATED_ORG_ID, "DeltaShort", PREFIX + " Delta Delegated"),
                "ext-delta-001",
                "idp-test"
        ));
        entityManager.flush();
    }

    // -------------------------------------------------------------------------
    // search() tests
    // -------------------------------------------------------------------------

    @Test
    void searchNoCriteria_returnsAllCreatedOrganizationsOrdered() {
        Range range = new Range(0, 100);
        SearchResult<Organization> result = organizationLookupDAO.search(
                new OrganizationSearchCriteria(), range, List.of(), ADMIN_IDENTITY);

        assertNotNull(result);
        assertThat(result.totalObjects(), greaterThanOrEqualTo(3));
        List<String> ids = result.objects().stream().map(Organization::getId).toList();
        assertTrue(ids.contains(ORG_A_ID));
        assertTrue(ids.contains(ORG_B_ID));
        assertTrue(ids.contains(DELEGATED_ORG_ID));
    }

    @Test
    void searchByName_returnsMatchingOrganizations() {
        Range range = new Range(0, 100);
        OrganizationSearchCriteria criteria = new OrganizationSearchCriteria()
                .setNameParam("Alpha");

        SearchResult<Organization> result = organizationLookupDAO.search(
                criteria, range, List.of(), ADMIN_IDENTITY);

        assertNotNull(result);
        assertThat(result.totalObjects(), greaterThanOrEqualTo(1));
        result.objects().forEach(org ->
                assertTrue(org.getName().toLowerCase().contains("alpha"),
                        "Expected name to contain 'alpha' but was: " + org.getName()));
    }

    @Test
    void searchByName_noMatch_returnsEmpty() {
        Range range = new Range(0, 100);
        OrganizationSearchCriteria criteria = new OrganizationSearchCriteria()
                .setNameParam(PREFIX + "_ImpossibleNameThatNeverExists_XYZ");

        SearchResult<Organization> result = organizationLookupDAO.search(
                criteria, range, List.of(), ADMIN_IDENTITY);

        assertNotNull(result);
        assertEquals(0, result.totalObjects());
        assertTrue(result.objects().isEmpty());
    }

    @Test
    void searchByShortname_returnsMatchingOrganizations() {
        Range range = new Range(0, 100);
        OrganizationSearchCriteria criteria = new OrganizationSearchCriteria()
                .setShortnameParam("Beta");

        SearchResult<Organization> result = organizationLookupDAO.search(
                criteria, range, List.of(), ADMIN_IDENTITY);

        assertNotNull(result);
        assertThat(result.totalObjects(), greaterThanOrEqualTo(1));
        result.objects().forEach(org ->
                assertTrue(org.getShortname().toLowerCase().contains("beta"),
                        "Expected shortname to contain 'beta' but was: " + org.getShortname()));
    }

    @Test
    void searchByDelegatedTrue_returnsOnlyDelegatedOrganizations() {
        Range range = new Range(0, 100);
        OrganizationSearchCriteria criteria = new OrganizationSearchCriteria()
                .setDelegatedParam(true);

        SearchResult<Organization> result = organizationLookupDAO.search(
                criteria, range, List.of(), ADMIN_IDENTITY);

        assertNotNull(result);
        assertThat(result.totalObjects(), greaterThanOrEqualTo(1));
        result.objects().forEach(org ->
                assertInstanceOf(DelegatedOrganization.class, org,
                        "Expected all results to be DelegatedOrganization but got: " + org.getClass().getSimpleName()));
    }

    @Test
    void searchByDelegatedFalse_returnsOnlyRegularOrganizations() {
        Range range = new Range(0, 100);
        OrganizationSearchCriteria criteria = new OrganizationSearchCriteria()
                .setDelegatedParam(false);

        SearchResult<Organization> result = organizationLookupDAO.search(
                criteria, range, List.of(), ADMIN_IDENTITY);

        assertNotNull(result);
        assertThat(result.totalObjects(), greaterThanOrEqualTo(2));
        result.objects().forEach(org ->
                assertFalse(org instanceof DelegatedOrganization,
                        "Expected no DelegatedOrganization, but got one with id: " + org.getId()));
    }

    @Test
    void searchWithSortByNameAscending_returnsResultsOrdered() {
        Range range = new Range(0, 100);
        OrganizationSearchCriteria criteria = new OrganizationSearchCriteria()
                .setNameParam(PREFIX);

        SearchResult<Organization> result = organizationLookupDAO.search(
                criteria, range, List.of(Sort.ascending("name")), ADMIN_IDENTITY);

        assertNotNull(result);
        assertThat(result.totalObjects(), greaterThanOrEqualTo(3));

        List<String> names = result.objects().stream().map(Organization::getName).toList();
        for (int i = 0; i < names.size() - 1; i++) {
            assertTrue(names.get(i).compareToIgnoreCase(names.get(i + 1)) <= 0,
                    "Results should be sorted ascending by name");
        }

        // Ensure our seeded organizations are in the expected relative order by name.
        List<String> ids = result.objects().stream().map(Organization::getId).toList();
        int indexOrgA = ids.indexOf(ORG_A_ID);
        int indexOrgB = ids.indexOf(ORG_B_ID);
        int indexDelegated = ids.indexOf(DELEGATED_ORG_ID);

        assertTrue(indexOrgA >= 0, "Expected OrgA to be present in results");
        assertTrue(indexOrgB >= 0, "Expected OrgB to be present in results");
        assertTrue(indexDelegated >= 0, "Expected delegated org to be present in results");
        assertTrue(indexOrgA < indexOrgB,
                "Expected OrgA to appear before OrgB in name ascending order");
        assertTrue(indexOrgB < indexDelegated,
                "Expected OrgB to appear before delegated org in name ascending order");
    }

    @Test
    void searchPagination_respectsOffsetAndLimit() {
        Range pageOne = new Range(0, 2);
        OrganizationSearchCriteria criteria = new OrganizationSearchCriteria()
                .setNameParam(PREFIX);

        SearchResult<Organization> result = organizationLookupDAO.search(
                criteria, pageOne, List.of(), ADMIN_IDENTITY);

        assertNotNull(result);
        assertThat(result.totalObjects(), greaterThanOrEqualTo(3));
        assertEquals(2, result.objects().size());
        assertEquals(0, result.offset());
        assertEquals(2, result.limit());
    }

    // -------------------------------------------------------------------------
    // getSuggestions() tests
    // -------------------------------------------------------------------------

    @Test
    void getSuggestionsForName_returnsMatchingSuggestions() {
        OrganizationSearchCriteria criteria = new OrganizationSearchCriteria();
        List<String> suggestions = organizationLookupDAO.getSuggestions("name", criteria);

        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
    }

    @Test
    void getSuggestionsForShortname_returnsMatchingSuggestions() {
        OrganizationSearchCriteria criteria = new OrganizationSearchCriteria()
                .setShortnameParam("Alpha");

        List<String> suggestions = organizationLookupDAO.getSuggestions("shortname", criteria);

        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        suggestions.forEach(s ->
                assertTrue(s.toLowerCase().contains("alpha"),
                        "Expected suggestion to contain 'alpha' but was: " + s));
    }

    // -------------------------------------------------------------------------
    // getOrganizationById() tests
    // -------------------------------------------------------------------------

    @Test
    void getOrganizationById_found_returnsOrganization() {
        Organization organization = organizationLookupDAO.getOrganizationById(ORG_A_ID);

        assertNotNull(organization);
        assertEquals(ORG_A_ID, organization.getId());
        assertEquals("Alpha Corp", organization.getShortname());
        assertEquals(PREFIX + " Alpha Organization", organization.getName());
    }

    @Test
    void getOrganizationById_delegated_returnsDelegatedOrganization() {
        Organization organization = organizationLookupDAO.getOrganizationById(DELEGATED_ORG_ID);

        assertNotNull(organization);
        assertInstanceOf(DelegatedOrganization.class, organization);
        DelegatedOrganization delegated = (DelegatedOrganization) organization;
        assertEquals("ext-delta-001", delegated.getExternalId());
        assertEquals("idp-test", delegated.getIdpId());
    }

    @Test
    void getOrganizationById_notFound_throwsNoSuchElementException() {
        assertThrows(NoSuchElementException.class,
                () -> organizationLookupDAO.getOrganizationById("nonExistingOrgId_XYZ"));
    }
}
