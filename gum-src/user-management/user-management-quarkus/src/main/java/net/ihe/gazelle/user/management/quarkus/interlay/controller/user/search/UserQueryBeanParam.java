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

package net.ihe.gazelle.user.management.quarkus.interlay.controller.user.search;

import jakarta.ws.rs.QueryParam;
import net.ihe.gazelle.search.jaxrs.api.QueryBeanParam;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.util.Date;
import java.util.Objects;

import static net.ihe.gazelle.user.management.api.application.user.search.UserSearchIndexServiceImpl.*;


/**
 * User Query Bean Parameter for search API integration
 */
public class UserQueryBeanParam extends QueryBeanParam {

    @Parameter(
            name = EMAIL,
            in = ParameterIn.QUERY,
            description = "Criterion to filter on the email of the user."
    )
    @QueryParam(EMAIL)
    private String email;

    @Parameter(
            name = FIRSTNAME,
            in = ParameterIn.QUERY,
            description = "Criterion to filter on the first name of the user."
    )
    @QueryParam(FIRSTNAME)
    private String firstName;

    @Parameter(
            name = LASTNAME,
            in = ParameterIn.QUERY,
            description = "Criterion to filter on the last name of the user."
    )
    @QueryParam(LASTNAME)
    private String lastName;

    @Parameter(
            name = GROUP,
            in = ParameterIn.QUERY,
            description = "Criterion to filter on the email of the user."
    )
    @QueryParam(GROUP)
    private String group;

    @Parameter(
            name = ACTIVATED,
            in = ParameterIn.QUERY,
            description = "Criterion to filter on the delegated status of the user."
    )
    @QueryParam(ACTIVATED)
    private Boolean activated;


    @Parameter(
            name = DELEGATED,
            in = ParameterIn.QUERY,
            description = "Criterion to filter on the delegated status of the user."
    )
    @QueryParam(DELEGATED)
    private Boolean delegated;

    @Parameter(
            name = ORGANIZATION_NAME,
            in = ParameterIn.QUERY,
            description = "Criterion to filter on the organization name of the user."
    )
    @QueryParam(ORGANIZATION_NAME)
    private String organizationName;

    @Parameter(
            name = ORGANIZATION_ID,
            in = ParameterIn.QUERY,
            description = "Criterion to filter on the organization id of the user."
    )
    @QueryParam(ORGANIZATION_ID)
    private String organizationId;

    @Parameter(
            name = SEARCH,
            in = ParameterIn.QUERY,
            description = "Criterion to filter simultaneously on the first name, last name and email of the user."
    )
    @QueryParam(SEARCH)
    private String search;

    @Parameter(
            name = LAST_LOGIN_TIMESTAMP,
            in = ParameterIn.QUERY,
            description = "Criterion to filter on the last login timestamp"
    )
    @QueryParam(LAST_LOGIN_TIMESTAMP)
    private Date lastLoginTimestamp;

    /**
     * Default constructor for UserQueryBeanParam.
     */
    public UserQueryBeanParam() {
        super(); // Default constructor
    }

    /**
     * Gets the email criterion for filtering user.
     *
     * @return the email criterion
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email criterion for filtering user.
     *
     * @param email the email criterion
     * @return this {@link UserQueryBeanParam} instance for method chaining
     */
    public UserQueryBeanParam setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Get the first name of the user.
     *
     * @return the first name of the user
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Set the first name of the user.
     *
     * @param firstName the first name of the user
     * @return this {@link UserQueryBeanParam} instance for method chaining
     */
    public UserQueryBeanParam setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * Get the last name of the user.
     *
     * @return the last name of the user
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Set the last name of the user.
     *
     * @param lastName the last name of the user
     * @return this {@link UserQueryBeanParam} instance for method chaining
     */
    public UserQueryBeanParam setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    /**
     * Get the activated status of the user.
     *
     * @return the activated status of the user
     */
    public Boolean getActivated() {
        return activated;
    }

    /**
     * Set the activated status of the user.
     *
     * @param activated the activated status of the user
     * @return this {@link UserQueryBeanParam} instance for method chaining
     */
    public UserQueryBeanParam setActivated(Boolean activated) {
        this.activated = activated;
        return this;
    }

    /**
     * Get the group of the user.
     *
     * @return the group of the user
     */
    public String getGroup() {
        return group;
    }

    /**
     * Set the group of the user.
     *
     * @param group the group of the user
     * @return this {@link UserQueryBeanParam} instance for method chaining
     */
    public UserQueryBeanParam setGroup(String group) {
        this.group = group;
        return this;
    }

    /**
     * Get the delegated status of the user.
     *
     * @return true if user is delegate, false otherwise
     */
    public Boolean getDelegated() {
        return delegated;
    }

    /**
     * Set the delegated status of the user.
     *
     * @param delegated true if user is delegate, false otherwise
     * @return this {@link UserQueryBeanParam} instance for method chaining
     */
    public UserQueryBeanParam setDelegated(Boolean delegated) {
        this.delegated = delegated;
        return this;
    }

    /**
     * Get the organization name of the user.
     *
     * @return the organization name of the user
     */
    public String getOrganizationName() {
        return organizationName;
    }

    /**
     * Set the organization name of the user.
     *
     * @param organizationName the organization name of the user
     * @return this {@link UserQueryBeanParam} instance for method chaining
     */
    public UserQueryBeanParam setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
        return this;
    }


    /**
     * Get the organization id of the user.
     *
     * @return the organization id of the user
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Set the organization id of the user.
     *
     * @param organizationId the organization id of the user
     * @return this {@link UserQueryBeanParam} instance for method chaining
     */
    public UserQueryBeanParam setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }


    /**
     * Get the search criterion.
     *
     * @return the search criterion
     */
    public String getSearch() {
        return search;
    }

    /**
     * Set the search criterion.
     *
     * @param search the search criterion
     * @return this {@link UserQueryBeanParam} instance for method chaining
     */
    public UserQueryBeanParam setSearch(String search) {
        this.search = search;
        return this;
    }

    /**
     * Get the last login timestamp.
     *
     * @return the last login timestamp
     */
    public Date getLastLoginTimestamp() {
        return lastLoginTimestamp;
    }

    /**
     * Set the last login timestamp.
     *
     * @param lastLoginTimestamp the last login timestamp
     * @return this {@link UserQueryBeanParam} instance for method chaining
     */
    public UserQueryBeanParam setLastLoginTimestamp(Date lastLoginTimestamp) {
        this.lastLoginTimestamp = lastLoginTimestamp;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserQueryBeanParam that = (UserQueryBeanParam) o;
        return Objects.equals(email, that.email)
                && Objects.equals(firstName, that.firstName)
                && Objects.equals(lastName, that.lastName)
                && Objects.equals(group, that.group)
                && Objects.equals(activated, that.activated)
                && Objects.equals(delegated, that.delegated)
                && Objects.equals(organizationName, that.organizationName)
                && Objects.equals(organizationId, that.organizationId)
                && Objects.equals(search, that.search)
                && Objects.equals(lastLoginTimestamp, that.lastLoginTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email,
                firstName,
                lastName,
                group,
                activated,
                delegated,
                organizationName,
                organizationId,
                search,
                lastLoginTimestamp);
    }
}
