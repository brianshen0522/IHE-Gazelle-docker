/*
 * Copyright 2026 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.user.management.quarkus.interlay.controller.organization.search;

import jakarta.ws.rs.QueryParam;
import net.ihe.gazelle.search.jaxrs.api.QueryBeanParam;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.util.Date;

import static net.ihe.gazelle.user.management.api.application.organization.OrganizationIndexService.*;


/**
 * Test Run Execution Query Bean Parameter for search API integration
 */
public class OrganizationQueryBeanParam extends QueryBeanParam {

    @Parameter(
            name = SEARCH,
            in = ParameterIn.QUERY,
            description = "Criterion to filter default organization attributes (name or shortname)"
    )
    @QueryParam(SEARCH)
    private String search;

    @Parameter(
            name = SHORTNAME,
            in = ParameterIn.QUERY,
            description = "Criterion to filter on the shortname of the organization."
    )
    @QueryParam(SHORTNAME)
    private String shortname;

    @Parameter(
            name = NAME,
            in = ParameterIn.QUERY,
            description = "Criterion to filter on the name of the organization."
    )
    @QueryParam(NAME)
    private String name;

    @Parameter(
            name = DELEGATED,
            in = ParameterIn.QUERY,
            description = "Criterion to filter on the delegated status of the organization."
    )
    @QueryParam(DELEGATED)
    private Boolean delegated;

    @Parameter(
            name = ARCHIVED,
            in = ParameterIn.QUERY,
            description = "Criterion to filter on the archived status of the organization."
    )
    @QueryParam(ARCHIVED)
    private Boolean archived;

    @Parameter(
            name = LAST_UPDATE_TIMESTAMP,
            in = ParameterIn.QUERY,
            description = "Criterion to filter on the last update timestamp"
    )
    @QueryParam(LAST_UPDATE_TIMESTAMP)
    private Date lastUpdateTimestamp;

    /**
     * Default constructor for ServiceQueryBeanParam.
     */
    public OrganizationQueryBeanParam() {
        super(); // Default constructor
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * Gets the shortname criterion for filtering organizations.
     * @return the shortname criterion
     */
    public String getShortname() {
        return shortname;
    }


    public OrganizationQueryBeanParam setShortname(String shortname) {
        this.shortname = shortname;
        return this;
    }

    public String getName() {
        return name;
    }

    public OrganizationQueryBeanParam setName(String name) {
        this.name = name;
        return this;
    }

    public Boolean getDelegated() {
        return delegated;
    }

    public OrganizationQueryBeanParam setDelegated(Boolean delegated) {
        this.delegated = delegated;
        return this;
    }

    public Boolean getArchived() {
        return archived;
    }

    public OrganizationQueryBeanParam setArchived(Boolean archived) {
        this.archived = archived;
        return this;
    }

    public Date getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public OrganizationQueryBeanParam setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        return this;
    }
}
