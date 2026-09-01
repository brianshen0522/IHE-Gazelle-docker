package net.ihe.gazelle.validation.gateway.evs.technical.ws;

import com.kereval.gazelle.datahouse.api.business.record.Item;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.RuntimeDelegate;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import net.ihe.gazelle.validation.gateway.evs.business.service.AsyncReportState;
import net.ihe.gazelle.validation.gateway.evs.business.service.ValidationLookupService;
import net.ihe.gazelle.validation.gateway.evs.business.service.ValidationReportService;
import net.ihe.gazelle.validation.gateway.evs.business.model.LocatedReportItem;
import org.glassfish.jersey.internal.RuntimeDelegateImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class EvsLegacyRedirectControllerTest {

    static {
        RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    }

    @Test
    @DisplayName("Legacy EVS URL redirects to Validation Portal report for public item")
    void redirectsPublicReport() {
        EvsLegacyRedirectController controller = new EvsLegacyRedirectController(
              lookupService(Optional.of(new LocatedReportItem("item-123", item("item-123")))),
              ""
        );

        Response response = controller.redirectLegacyReportLink("legacy-oid", null, null, baseUriInfo("https://gateway.example/"));

        assertThat(response.getStatus(), is(Response.Status.SEE_OTHER.getStatusCode()));
        assertThat(response.getHeaderString("Location"), is("https://gateway.example/validation-portal/reports/item-123"));
    }

    @Test
    @DisplayName("Legacy EVS URL uses configured Validation Portal base URL when provided")
    void redirectsUsingConfiguredPortalBaseUrl() {
        EvsLegacyRedirectController controller = new EvsLegacyRedirectController(
              lookupService(Optional.of(new LocatedReportItem("item-789", item("item-789")))),
              "https://portal.example/validation-portal"
        );

        Response response = controller.redirectLegacyReportLink("legacy-oid", null, null, baseUriInfo("https://gateway.example/"));

        assertThat(response.getStatus(), is(Response.Status.SEE_OTHER.getStatusCode()));
        assertThat(response.getHeaderString("Location"), is("https://portal.example/validation-portal/reports/item-789"));
    }

    @Test
    @DisplayName("Legacy EVS URL returns 404 when validation does not exist")
    void returnsNotFoundWhenUnknownOid() {
        EvsLegacyRedirectController controller = new EvsLegacyRedirectController(
              lookupService(Optional.empty()),
              ""
        );

        Response response = controller.redirectLegacyReportLink("unknown-oid", null, null, baseUriInfo("https://gateway.example/"));

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    @DisplayName("Legacy EVS URL redirects private report without checking access")
    void redirectsPrivateReportWithoutCheckingAccess() {
        EvsLegacyRedirectController controller = new EvsLegacyRedirectController(
              lookupService(Optional.of(new LocatedReportItem("item-1", privateItem("item-1")))),
              ""
        );

        Response response = controller.redirectLegacyReportLink("legacy-oid", null, null, baseUriInfo("https://gateway.example/"));

        assertThat(response.getStatus(), is(Response.Status.SEE_OTHER.getStatusCode()));
        assertThat(response.getHeaderString("Location"), is("https://gateway.example/validation-portal/reports/item-1"));
    }

    @Test
    @DisplayName("Legacy EVS URL translates privacyKey to readAccessKey on redirect")
    void redirectsLegacyPrivacyKeyAsReadAccessKey() {
        EvsLegacyRedirectController controller = new EvsLegacyRedirectController(
              lookupService(Optional.of(new LocatedReportItem("item-123", item("item-123")))),
              "https://portal.example/validation-portal"
        );

        Response response = controller.redirectLegacyReportLink("legacy-oid", "legacy-key", null, baseUriInfo("https://gateway.example/"));

        assertThat(response.getStatus(), is(Response.Status.SEE_OTHER.getStatusCode()));
        assertThat(response.getHeaderString("Location"),
              is("https://portal.example/validation-portal/reports/item-123?readAccessKey=legacy-key"));
    }

    private static ValidationLookupService lookupService(Optional<LocatedReportItem> locatedItem) {
        return new ValidationLookupService(new AsyncReportState(), new StaticReportService(locatedItem));
    }

    private static Item item(String itemId) {
        AccessControlList acl = new AccessControlList();
        acl.setPublic(true);
        acl.setOwners(Set.of("owner"));
        Item item = new Item();
        item.setId(itemId);
        item.setAccessControlList(acl);
        return item;
    }

    private static Item privateItem(String itemId) {
        AccessControlList acl = new AccessControlList();
        acl.setPublic(false);
        acl.setOwners(Set.of("owner"));
        Item item = new Item();
        item.setId(itemId);
        item.setAccessControlList(acl);
        return item;
    }

    private static UriInfo baseUriInfo(String baseUri) {
        URI uri = URI.create(baseUri);
        return new UriInfo() {
            @Override
            public String getPath() {
                return uri.getPath();
            }

            @Override
            public String getPath(boolean decode) {
                return uri.getPath();
            }

            @Override
            public List<PathSegment> getPathSegments() {
                return List.of();
            }

            @Override
            public List<PathSegment> getPathSegments(boolean decode) {
                return List.of();
            }

            @Override
            public URI getRequestUri() {
                return uri;
            }

            @Override
            public UriBuilder getRequestUriBuilder() {
                return UriBuilder.fromUri(uri);
            }

            @Override
            public URI getAbsolutePath() {
                return uri;
            }

            @Override
            public UriBuilder getAbsolutePathBuilder() {
                return UriBuilder.fromUri(uri);
            }

            @Override
            public URI getBaseUri() {
                return uri;
            }

            @Override
            public UriBuilder getBaseUriBuilder() {
                return UriBuilder.fromUri(uri);
            }

            @Override
            public MultivaluedHashMap<String, String> getPathParameters() {
                return new MultivaluedHashMap<>();
            }

            @Override
            public MultivaluedHashMap<String, String> getPathParameters(boolean decode) {
                return new MultivaluedHashMap<>();
            }

            @Override
            public MultivaluedHashMap<String, String> getQueryParameters() {
                return new MultivaluedHashMap<>();
            }

            @Override
            public MultivaluedHashMap<String, String> getQueryParameters(boolean decode) {
                return new MultivaluedHashMap<>();
            }

            @Override
            public List<String> getMatchedURIs() {
                return List.of();
            }

            @Override
            public List<String> getMatchedURIs(boolean decode) {
                return List.of();
            }

            @Override
            public List<Object> getMatchedResources() {
                return List.of();
            }

            @Override
            public URI resolve(URI uri) {
                return UriBuilder.fromUri(baseUri).build(uri);
            }

            @Override
            public URI relativize(URI uri) {
                return URI.create(baseUri).relativize(uri);
            }
        };
    }

    private static class StaticReportService implements ValidationReportService {

        private final Optional<LocatedReportItem> locatedItem;

        StaticReportService(Optional<LocatedReportItem> locatedItem) {
            this.locatedItem = locatedItem;
        }

        @Override
        public Item readReportItem(String oid) {
            throw new UnsupportedOperationException("Not used by this test");
        }

        @Override
        public Optional<LocatedReportItem> findReportByLegacyOid(String legacyOid) {
            return locatedItem;
        }

        @Override
        public String extractValidationReportLocation(net.ihe.gazelle.maestro.api.business.testreport.TestReport report) {
            return null;
        }

        @Override
        public String extractReportId(String reportLocation) {
            return null;
        }

        @Override
        public String resolveExecutionFailureMessage(net.ihe.gazelle.maestro.api.business.testreport.TestReport report) {
            return null;
        }
    }

}
