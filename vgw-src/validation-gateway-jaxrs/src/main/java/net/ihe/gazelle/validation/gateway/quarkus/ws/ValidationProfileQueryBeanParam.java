package net.ihe.gazelle.validation.gateway.quarkus.ws;

import jakarta.ws.rs.QueryParam;
import net.ihe.gazelle.search.jaxrs.api.QueryBeanParam;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.util.List;

public class ValidationProfileQueryBeanParam extends QueryBeanParam {

    @Parameter(in = ParameterIn.QUERY, description = "Filter by validation service name.")
    @QueryParam("validationService")
    private String validationService;


    @Parameter(in = ParameterIn.QUERY, description = "Filter by profile ID.")
    @QueryParam("profileID")
    private String profileID;


    @Parameter(in = ParameterIn.QUERY, description = "Filter by profile name.")
    @QueryParam("profileName")
    private String profileName;

    @Parameter(in = ParameterIn.QUERY, description = "Filter by profile version.")
    @QueryParam("version")
    private String version;


    @Parameter(in = ParameterIn.QUERY, description = "Filter by profile domain.")
    @QueryParam("domain")
    private String domain;

    @Parameter(in = ParameterIn.QUERY, description = "Filter by covered items.")
    @QueryParam("coveredItems")
    private String coveredItems;


    @Parameter(in = ParameterIn.QUERY, description = "Filter by standards.")
    @QueryParam("standards")
    private String standards;


    @Parameter(in = ParameterIn.QUERY, description = "Filter by tags.")
    @QueryParam("tags")
    private String tags;


    @Parameter(in = ParameterIn.QUERY, name = "_sort_order", hidden = true,
            description = "Deprecated. Use `_sort` with a '-' prefix for descending order.")
    @QueryParam("_sort_order")
    private String sortOrder;


    public String getValidationService() {
        return validationService;
    }

    public String getProfileID() {
        return profileID;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getVersion() {
        return version;
    }

    public String getDomain() {
        return domain;
    }

    public String getCoveredItems() {
        return coveredItems;
    }

    public String getStandards() {
        return standards;
    }

    public String getTags() {
        return tags;
    }

    public String getSortOrder() {
        return sortOrder;
    }

}
