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

package net.ihe.gazelle.user.management.api.application.user.search;

import net.ihe.gazelle.search.api.SearchCriteria;
import net.ihe.gazelle.search.api.SearchParameter;


import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This class defines the search parameters for querying users in the User Search.
 *
 * @see SearchCriteria
 */
public class UserSearchCriteria implements SearchCriteria {

    private SearchParameter firstNameParam = null;
    private SearchParameter lastNameParam = null;
    private SearchParameter emailParam = null;
    private SearchParameter groupParam = null;
    private SearchParameter activatedParam = null;
    private SearchParameter delegatedParam = null;
    private SearchParameter organizationNameParam = null;
    private SearchParameter organizationIdParam = null;
    private SearchParameter searchParam = null;
    private SearchParameter lastLoginTimestampParam = null;

    /**
     * Get the first name parameter.
     *
     * @return the first name parameter
     */
    public SearchParameter getFirstNameParam() {
        return firstNameParam;
    }

    /**
     * Set the first name parameter.
     *
     * @param names the first name parameter (can be multiple)
     * @return this {@link UserSearchCriteria} instance for method chaining
     */
    public UserSearchCriteria setFirstNameParam(String... names) {
        this.firstNameParam = new SearchParameter()
                .setName(UserSearchIndexServiceImpl.FIRSTNAME)
                .setValues(Arrays.asList(names));
        return this;
    }

    /**
     * Get the last name parameter.
     *
     * @return the last name parameter
     */
    public SearchParameter getLastNameParam() {
        return lastNameParam;
    }

    /**
     * Set the last name parameter.
     *
     * @param names the last name parameter (can be multiple)
     * @return this {@link UserSearchCriteria} instance for method chaining
     */
    public UserSearchCriteria setLastNameParam(String... names) {
        this.lastNameParam = new SearchParameter()
                .setName(UserSearchIndexServiceImpl.LASTNAME)
                .setValues(Arrays.asList(names));
        return this;
    }

    /**
     * Get the email parameter.
     *
     * @return the email parameter
     */
    public SearchParameter getEmailParam() {
        return emailParam;
    }

    /**
     * Set the email parameter.
     *
     * @param names the email parameter (can be multiple)
     * @return this {@link UserSearchCriteria} instance for method chaining
     */
    public UserSearchCriteria setEmailParam(String... names) {
        this.emailParam = new SearchParameter()
                .setName(UserSearchIndexServiceImpl.EMAIL)
                .setValues(Arrays.asList(names));
        return this;
    }

    /**
     * Get the group parameter.
     *
     * @return the group parameter
     */
    public SearchParameter getGroupParam() {
        return groupParam;
    }

    /**
     * Set the group parameter.
     *
     * @param groups the group parameter (can be multiple)
     * @return this {@link UserSearchCriteria} instance for method chaining
     */
    public UserSearchCriteria setGroupParam(String... groups) {
        this.groupParam = new SearchParameter()
                .setName(UserSearchIndexServiceImpl.GROUP)
                .setValues(Arrays.asList(groups));
        return this;
    }

    /**
     * Get the activated parameter.
     *
     * @return the activated parameter
     */
    public SearchParameter getActivatedParam() {
        return activatedParam;
    }

    /**
     * Set the activated parameter.
     *
     * @param activated the activated parameter
     * @return this {@link UserSearchCriteria} instance for method chaining
     */
    public UserSearchCriteria setActivatedParam(Boolean activated) {
        this.activatedParam = new SearchParameter()
                .setName(UserSearchIndexServiceImpl.ACTIVATED)
                .setValue(activated);
        return this;
    }

    /**
     * Get the delegated parameter.
     *
     * @return the delegated parameter
     */
    public SearchParameter getDelegatedParam() {
        return delegatedParam;
    }

    /**
     * Set the delegated parameter.
     *
     * @param delegated the delegated parameter
     * @return this {@link UserSearchCriteria} instance for method chaining
     */
    public UserSearchCriteria setDelegatedParam(Boolean delegated) {
        this.delegatedParam = new SearchParameter()
                .setName(UserSearchIndexServiceImpl.DELEGATED)
                .setValue(delegated);
        return this;
    }

    /**
     * Get the organization name parameter.
     *
     * @return the organization name parameter
     */
    public SearchParameter getOrganizationNameParam() {
        return organizationNameParam;
    }

    /**
     * Set the organization name parameter.
     *
     * @param organizationNames the organization name parameter (can be multiple)
     * @return this {@link UserSearchCriteria} instance for method chaining
     */
    public UserSearchCriteria setOrganizationNameParam(String... organizationNames) {
        this.organizationNameParam = new SearchParameter()
                .setName(UserSearchIndexServiceImpl.ORGANIZATION_NAME)
                .setValues(Arrays.asList(organizationNames));
        return this;
    }

    /**
     * Get the organization id parameter.
     *
     * @return the organization id parameter
     */
    public SearchParameter getOrganizationIdParam() {
        return organizationIdParam;
    }

    /**
     * Set the organization id parameter.
     *
     * @param organizationIds the organization id parameter (can be multiple)
     * @return this {@link UserSearchCriteria} instance for method chaining
     */
    public UserSearchCriteria setOrganizationIdParam(String... organizationIds) {
        this.organizationIdParam = new SearchParameter()
                .setName(UserSearchIndexServiceImpl.ORGANIZATION_ID)
                .setValues(Arrays.asList(organizationIds));
        return this;
    }

    /**
     * Get the search parameter.
     *
     * @return the search parameter
     */
    public SearchParameter getSearchParam() {
        return searchParam;
    }

    /**
     * Set the search parameter.
     *
     * @param searchParam the search parameter
     * @return this {@link UserSearchCriteria} instance for method chaining
     */
    public UserSearchCriteria setSearchParam(String... searchParam) {
        this.searchParam = new SearchParameter()
                .setName(UserSearchIndexServiceImpl.SEARCH)
                .setValues(Arrays.asList(searchParam));
        return this;
    }

    @Override
    public List<SearchParameter> getSearchParameters() {
        return Stream.of(firstNameParam, lastNameParam, emailParam,
                        groupParam, activatedParam, delegatedParam,
                        organizationNameParam, searchParam)
                .filter(Objects::nonNull).toList();
    }

    /**
     * Get the last login timestamp parameter.
     *
     * @return the last login timestamp parameter
     */
    public SearchParameter getLastLoginTimestampParam() {
        return lastLoginTimestampParam;
    }

    /**
     * Set the last login timestamp parameter.
     *
     * @param lastLoginTimestampParam the last login timestamp parameter
     * @return this {@link UserSearchCriteria} instance for method chaining
     */
    public UserSearchCriteria setLastLoginTimestampParam(Date... lastLoginTimestampParam) {
        this.lastLoginTimestampParam = new SearchParameter()
                .setName(UserSearchIndexServiceImpl.LAST_LOGIN_TIMESTAMP)
                .setValue(lastLoginTimestampParam);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserSearchCriteria that = (UserSearchCriteria) o;
        return Objects.equals(firstNameParam, that.firstNameParam)
                && Objects.equals(lastNameParam, that.lastNameParam)
                && Objects.equals(emailParam, that.emailParam)
                && Objects.equals(groupParam, that.groupParam)
                && Objects.equals(activatedParam, that.activatedParam)
                && Objects.equals(delegatedParam, that.delegatedParam)
                && Objects.equals(organizationNameParam, that.organizationNameParam)
                && Objects.equals(organizationIdParam, that.organizationIdParam)
                && Objects.equals(searchParam, that.searchParam)
                && Objects.equals(lastLoginTimestampParam, that.lastLoginTimestampParam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstNameParam,
                lastNameParam,
                emailParam,
                groupParam,
                activatedParam,
                delegatedParam,
                organizationNameParam,
                organizationIdParam,
                searchParam,
                lastLoginTimestampParam);
    }
}
