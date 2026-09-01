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

import net.ihe.gazelle.search.api.IndexService;
import net.ihe.gazelle.search.jaxrs.api.AbstractQueryMapper;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationSearchCriteria;

import java.util.Date;

import static net.ihe.gazelle.user.management.api.application.organization.OrganizationIndexService.*;

public class OrganizationQueryMapper extends AbstractQueryMapper<OrganizationQueryBeanParam, OrganizationSearchCriteria> {

    public OrganizationQueryMapper(IndexService indexService) {
        super(indexService);
    }

    @Override
    protected OrganizationSearchCriteria instantiateSearchCriteria() {
        return new OrganizationSearchCriteria();
    }

    @Override
    protected MappingAction<OrganizationQueryBeanParam, OrganizationSearchCriteria>[] getMappingActions() {
        return new MappingAction[]{
                new MappingAction<OrganizationQueryBeanParam, OrganizationSearchCriteria>(
                        SEARCH, OrganizationQueryBeanParam::getSearch,
                        (criteria, param) -> criteria.setSearchParam(param.getValues().toArray(String[]::new))
                ),
                new MappingAction<OrganizationQueryBeanParam, OrganizationSearchCriteria>(
                        SHORTNAME, OrganizationQueryBeanParam::getShortname,
                        (criteria, param) -> criteria.setShortnameParam(param.getValues().toArray(String[]::new))
                ),
                new MappingAction<OrganizationQueryBeanParam, OrganizationSearchCriteria>(
                        NAME, OrganizationQueryBeanParam::getName,
                        (criteria, param) -> criteria.setNameParam(param.getValues().toArray(String[]::new))
                ),
                new MappingAction<OrganizationQueryBeanParam, OrganizationSearchCriteria>(
                        DELEGATED, OrganizationQueryBeanParam::getDelegated,
                        (criteria, param) -> criteria.setDelegatedParam((Boolean) param.getFirstValue())
                ),
                new MappingAction<OrganizationQueryBeanParam, OrganizationSearchCriteria>(
                        ARCHIVED, OrganizationQueryBeanParam::getArchived,
                        (criteria, param) -> criteria.setArchivedParam((Boolean) param.getFirstValue())
                ),
                new MappingAction<OrganizationQueryBeanParam, OrganizationSearchCriteria>(LAST_UPDATE_TIMESTAMP, OrganizationQueryBeanParam::getLastUpdateTimestamp,
                        (criteria, param) -> criteria.setLastUpdateTimestampParam(param.getValues().toArray(Date[]::new))
                )
        };
    }
}
