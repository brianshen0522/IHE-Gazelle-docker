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

package net.ihe.gazelle.user.management.commons.application.organization.lookup;

import net.ihe.gazelle.search.api.SearchCriteria;
import net.ihe.gazelle.search.api.SearchParameter;

import net.ihe.gazelle.user.management.api.application.organization.OrganizationIndexService;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class OrganizationSearchCriteria implements SearchCriteria {

    private SearchParameter searchParam = null;
    private SearchParameter shortnameParam = null;
    private SearchParameter nameParam = null;
    private SearchParameter delegatedParam = null;
    private SearchParameter archivedParam = null;
    private SearchParameter lastUpdateTimestampParam = null;

    public OrganizationSearchCriteria() {
        // Default constructor
    }

    public SearchParameter getSearchParam() {
        return searchParam;
    }

    public OrganizationSearchCriteria setSearchParam(String... search) {
        this.searchParam = new SearchParameter()
                .setName(OrganizationIndexService.SEARCH)
                .setValues(Arrays.asList((Object[]) search));
        return this;
    }

    public SearchParameter getShortnameParam() {
        return shortnameParam;
    }

    public OrganizationSearchCriteria setShortnameParam(String... names) {
        this.shortnameParam = new SearchParameter()
                .setName(OrganizationIndexService.SHORTNAME)
                .setValues(Arrays.asList((Object[]) names));
        return this;
    }

    public SearchParameter getNameParam() {
        return nameParam;
    }

    public OrganizationSearchCriteria setNameParam(String... names) {
        this.nameParam = new SearchParameter()
                .setName(OrganizationIndexService.NAME)
                .setValues(Arrays.asList((Object[]) names));
        return this;
    }

    public SearchParameter getDelegatedParam() {
        return delegatedParam;
    }

    public OrganizationSearchCriteria setDelegatedParam(Boolean deprecated) {
        this.delegatedParam = new SearchParameter()
                .setName(OrganizationIndexService.DELEGATED)
                .setValue(deprecated);
        return this;
    }

    public SearchParameter getArchivedParam() {
        return archivedParam;
    }

    public OrganizationSearchCriteria setArchivedParam(Boolean archived) {
        this.archivedParam = new SearchParameter()
                .setName(OrganizationIndexService.ARCHIVED)
                .setValue(archived);
        return this;
    }

    public SearchParameter getLastUpdateTimestampParam() {
        return lastUpdateTimestampParam;
    }

    public OrganizationSearchCriteria setLastUpdateTimestampParam(Date... timestamps) {
        this.lastUpdateTimestampParam = new SearchParameter()
                .setName(OrganizationIndexService.LAST_UPDATE_TIMESTAMP)
                .setValues(Arrays.asList((Object[]) timestamps));
        return this;
    }

    @Override
    public List<SearchParameter> getSearchParameters() {
        return Stream.of(searchParam, shortnameParam, nameParam, delegatedParam, archivedParam, lastUpdateTimestampParam).filter(Objects::nonNull).toList();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationSearchCriteria that = (OrganizationSearchCriteria) o;
        return Objects.equals(searchParam, that.searchParam) && Objects.equals(shortnameParam, that.shortnameParam) && Objects.equals(nameParam, that.nameParam) && Objects.equals(delegatedParam, that.delegatedParam) && Objects.equals(archivedParam, that.archivedParam) && Objects.equals(lastUpdateTimestampParam, that.lastUpdateTimestampParam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchParam, shortnameParam, nameParam, delegatedParam, archivedParam, lastUpdateTimestampParam);
    }
}
