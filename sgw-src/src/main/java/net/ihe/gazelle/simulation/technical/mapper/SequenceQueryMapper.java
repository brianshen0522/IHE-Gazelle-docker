/*
 * Copyright 2025-2026 IHE International.
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

package net.ihe.gazelle.simulation.technical.mapper;

import net.ihe.gazelle.search.api.IndexService;
import net.ihe.gazelle.search.jaxrs.api.AbstractQueryMapper;
import net.ihe.gazelle.simulation.business.search.SequenceSearchCriteria;
import net.ihe.gazelle.simulation.technical.ws.SequenceQueryBeanParam;

import static net.ihe.gazelle.simulation.business.search.SequenceIndexService.*;

/**
 * Mapper class for mapping query parameters to {@link SequenceSearchCriteria}.
 */
public class SequenceQueryMapper extends AbstractQueryMapper<SequenceQueryBeanParam, SequenceSearchCriteria> {

   /**
    * Constructs an instance of {@code SequenceQueryMapper} with the provided {@code IndexService}.
    *
    * @param indexService the service used for accessing indexed data for mapping
    *                     query parameters to search criteria
    */
   public SequenceQueryMapper(IndexService indexService) {
      super(indexService);
   }

   @Override
   protected SequenceSearchCriteria instantiateSearchCriteria() {
      return new SequenceSearchCriteria();
   }

   @Override
   protected MappingAction<SequenceQueryBeanParam, SequenceSearchCriteria>[] getMappingActions() {
      return new MappingAction[]{
            new MappingAction<>(SERVICE_NAME, SequenceQueryBeanParam::getServiceName, SequenceSearchCriteria::setServiceName),
            new MappingAction<>(ID, SequenceQueryBeanParam::getId, SequenceSearchCriteria::setId),
            new MappingAction<>(TRANSACTION, SequenceQueryBeanParam::getTransaction, SequenceSearchCriteria::setTransaction),
            new MappingAction<>(STANDARD, SequenceQueryBeanParam::getStandard, SequenceSearchCriteria::setStandard),
            new MappingAction<>(SIMULATED_ROLE, SequenceQueryBeanParam::getSimulatedRole, SequenceSearchCriteria::setSimulatedRole),
            new MappingAction<>(TESTED_ROLE, SequenceQueryBeanParam::getTestedRole, SequenceSearchCriteria::setTestedRole),
            new MappingAction<>(SHORT_DESCRIPTION, SequenceQueryBeanParam::getShortDescription, SequenceSearchCriteria::setShortDescription),
            new MappingAction<>(RUNNABLE, SequenceQueryBeanParam::getRunnable, SequenceSearchCriteria::setRunnable),
            new MappingAction<>(VALID, SequenceQueryBeanParam::getValid, SequenceSearchCriteria::setValid)
      };
   }

}
