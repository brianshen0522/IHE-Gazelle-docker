/*
 * Copyright 2025 IHE International.
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

package net.ihe.gazelle.serviceregistry.technical.rest;

import net.ihe.gazelle.search.api.IndexService;
import net.ihe.gazelle.search.jaxrs.api.AbstractQueryMapper;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;

import static net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceIndexService.*;

/**
 * ServiceQueryMapper is responsible for mapping query parameters from the ServiceQueryBeanParam to the
 * ServiceSearchCriteria used for searching services.
 */
public class ServiceQueryMapper extends AbstractQueryMapper<ServiceQueryBeanParam, ServiceSearchCriteria> {

   /**
    * Constructs a ServiceQueryMapper with the specified IndexService.
    *
    * @param indexService the IndexService to be used to perform criterion validity checks.
    */
   public ServiceQueryMapper(IndexService indexService) {
      super(indexService);
   }

   @Override
   protected ServiceSearchCriteria instantiateSearchCriteria() {
      return new ServiceSearchCriteria();
   }

   @Override
   protected MappingAction<ServiceQueryBeanParam, ServiceSearchCriteria>[] getMappingActions() {
      return new MappingAction[]{
            new MappingAction<ServiceQueryBeanParam, ServiceSearchCriteria>(
                  NAME,
                  ServiceQueryBeanParam::getName,
                  (criteria, param) -> criteria.setName(param.getValues().toArray(String[]::new))
            ),
            new MappingAction<ServiceQueryBeanParam, ServiceSearchCriteria>(
                  INSTANCE_ID,
                  ServiceQueryBeanParam::getInstanceId,
                  (criteria, param) -> criteria.setInstanceId(param.getValues().toArray(String[]::new))
            ),
            new MappingAction<ServiceQueryBeanParam, ServiceSearchCriteria>(
                  STATUS,
                  ServiceQueryBeanParam::getStatus,
                  (criteria, param) -> criteria.setStatus(
                        param.getValues().stream().map(name -> Status.valueOf((String) name)).toArray(Status[]::new)
                  )
            ),
            new MappingAction<ServiceQueryBeanParam, ServiceSearchCriteria>(
                  SELF_REGISTERED,
                  ServiceQueryBeanParam::getSelfRegistered,
                  (criteria, param) -> criteria.setSelfRegistered((Boolean) param.getFirstValue())
            ),
            new MappingAction<ServiceQueryBeanParam, ServiceSearchCriteria>(
                  PROVIDED_INTERFACE,
                  ServiceQueryBeanParam::getProvidedInterface,
                  (criteria, param) -> criteria.setProvidedInterface(param.getValues().toArray(String[]::new))
            ),
            new MappingAction<ServiceQueryBeanParam, ServiceSearchCriteria>(
                 CONSUMED_INTERFACE,
                 ServiceQueryBeanParam::getConsumedInterface,
                 (criteria, param) -> criteria.setConsumedInterface(param.getValues().toArray(String[]::new))
            )
      };
   }

}
