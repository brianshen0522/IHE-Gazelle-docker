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

package net.ihe.gazelle.serviceregistry.client.business;

import net.ihe.gazelle.search.client.business.SearchClient;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;

/**
 * ServiceLookupClient is an interface that extends SearchClient to provide methods for searching deployed services
 * in the Service Registry. It uses the DeployedService domain object and ServiceSearchCriteria for search criteria.
 */
public interface ServiceLookupClient extends SearchClient<DeployedService, ServiceSearchCriteria> {

}
