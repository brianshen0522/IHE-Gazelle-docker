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

import net.ihe.gazelle.search.api.IndexService;
import net.ihe.gazelle.search.jaxrs.api.AbstractQueryMapper;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchCriteria;

import java.util.Date;

import static net.ihe.gazelle.user.management.api.application.user.search.UserSearchIndexServiceImpl.*;

/**
 * Mapper class responsible for converting {@link UserQueryBeanParam} instances into {@link UserSearchCriteria} instances.
 */
public class UserQueryMapper extends AbstractQueryMapper<UserQueryBeanParam, UserSearchCriteria> {

    public UserQueryMapper(IndexService indexService) {
        super(indexService);
    }

    @Override
    protected UserSearchCriteria instantiateSearchCriteria() {
        return new UserSearchCriteria();
    }

    @Override
    protected MappingAction<UserQueryBeanParam, UserSearchCriteria>[] getMappingActions() {
        return new MappingAction[]{
                new MappingAction<UserQueryBeanParam, UserSearchCriteria>(
                        FIRSTNAME, UserQueryBeanParam::getFirstName,
                        (criteria, param) -> criteria.setFirstNameParam(param.getValues().toArray(String[]::new))
                ),
                new MappingAction<UserQueryBeanParam, UserSearchCriteria>(
                        LASTNAME, UserQueryBeanParam::getLastName,
                        (criteria, param) -> criteria.setLastNameParam(param.getValues().toArray(String[]::new))
                ),
                new MappingAction<UserQueryBeanParam, UserSearchCriteria>(
                        GROUP, UserQueryBeanParam::getGroup,
                        (criteria, param) -> criteria.setGroupParam(param.getValues().toArray(String[]::new))
                ),
                new MappingAction<UserQueryBeanParam, UserSearchCriteria>(
                        EMAIL, UserQueryBeanParam::getEmail,
                        (criteria, param) -> criteria.setEmailParam(param.getValues().toArray(String[]::new))
                ),
                new MappingAction<UserQueryBeanParam, UserSearchCriteria>(
                        ACTIVATED, UserQueryBeanParam::getActivated,
                        (criteria, param) -> criteria.setActivatedParam((Boolean) param.getFirstValue())
                ),
                new MappingAction<UserQueryBeanParam, UserSearchCriteria>(
                        DELEGATED, UserQueryBeanParam::getDelegated,
                        (criteria, param) -> criteria.setDelegatedParam((Boolean) param.getFirstValue())
                ),
                new MappingAction<UserQueryBeanParam, UserSearchCriteria>(
                        ORGANIZATION_NAME, UserQueryBeanParam::getOrganizationName,
                        (criteria, param) -> criteria.setOrganizationNameParam(param.getValues().toArray(String[]::new))
                ),
                new MappingAction<UserQueryBeanParam, UserSearchCriteria>(
                        ORGANIZATION_ID, UserQueryBeanParam::getOrganizationId,
                        (criteria, param) -> criteria.setOrganizationIdParam(param.getValues().toArray(String[]::new))
                ),
                new MappingAction<UserQueryBeanParam, UserSearchCriteria>(
                        SEARCH, UserQueryBeanParam::getSearch,
                        (criteria, param) -> criteria.setSearchParam(param.getValues().toArray(String[]::new))
                ),
                new MappingAction<UserQueryBeanParam, UserSearchCriteria>(LAST_LOGIN_TIMESTAMP, UserQueryBeanParam::getLastLoginTimestamp,
                        (criteria, param)-> criteria.setLastLoginTimestampParam(param.getValues().toArray(Date[]::new))
                )
        };
    }
}
